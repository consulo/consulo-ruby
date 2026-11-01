package consulo.ruby.impl.bundler.resolve;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.LibraryData;
import consulo.externalSystem.model.project.LibraryDependencyData;
import consulo.externalSystem.model.project.LibraryLevel;
import consulo.externalSystem.model.project.LibraryPathType;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ruby.impl.bundler.BundlerPaths;
import consulo.ruby.impl.bundler.BundlerProcessRunner;
import consulo.ruby.impl.bundler.BundlerRubyLocator;
import consulo.ruby.impl.bundler.setting.BundlerExecutionSettings;
import jakarta.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resolves a Gemfile into one module: the content root with the conventional source, test and excluded
 * folders, a project library per locked gem, and the Bundler and rake tasks.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerProjectResolver implements ExternalSystemProjectResolver<BundlerExecutionSettings> {
    private static final Logger LOG = Logger.getInstance(BundlerProjectResolver.class);

    private static final long PROBE_TIMEOUT_MS = 120_000;
    private static final long RAKE_TIMEOUT_MS = 60_000;

    private static final List<String> SOURCE_DIRS = List.of("lib");
    private static final List<String> RAILS_SOURCE_DIRS = List.of("app", "config", "db");
    private static final List<String> TEST_DIRS = List.of("test", "spec");
    private static final List<String> EXCLUDED_DIRS = List.of("vendor/bundle", ".bundle", "tmp", "log", "coverage", "storage", "node_modules", "public/assets", "pkg");

    private final BundlerProcessRunner myRunner = new BundlerProcessRunner();

    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                    String projectPath,
                                                    boolean isPreviewMode,
                                                    @Nullable BundlerExecutionSettings settings,
                                                    ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        Path rootDir = BundlerPaths.getRootDir(projectPath);
        if (!Files.isRegularFile(rootDir.resolve(BundlerConstants.GEMFILE))) {
            throw new IllegalArgumentException(RubyApiLocalize.bundlerErrorNoGemfile(rootDir.toString()).get());
        }

        String rubyExecutable = settings != null ? settings.getRubyExecutable() : BundlerRubyLocator.PATH_RUBY;
        String sdkName = settings != null ? settings.getSdkName() : null;

        String name = rootDir.getFileName().toString();
        ProjectData projectData = new ProjectData(BundlerConstants.SYSTEM_ID, name, rootDir.toString(), projectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        ModuleData moduleData = new ModuleData(name, BundlerConstants.SYSTEM_ID, name, rootDir.toString(), projectPath);
        DataNode<ModuleData> moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData);

        List<BundlerGem> gems;
        boolean gemsInstalled = false;
        if (isPreviewMode) {
            gems = BundlerGemfileLock.read(rootDir.resolve(BundlerConstants.GEMFILE_LOCK));
        }
        else {
            notify(listener, id, RubyApiLocalize.bundlerStatusResolving().get());
            BundlerProbeResult result = runProbe(id, rootDir, rubyExecutable, listener);
            if (result != null && result.errorMessage() == null && result.missingMessage() == null) {
                gems = result.gems();
                gemsInstalled = true;
            }
            else {
                if (result != null && result.missingMessage() != null) {
                    output(listener, id, RubyApiLocalize.bundlerErrorGemsMissing(result.missingMessage()).get());
                }
                else if (result != null && result.errorMessage() != null) {
                    output(listener, id, RubyApiLocalize.bundlerErrorProbeFailed(result.errorMessage()).get());
                }
                gems = BundlerGemfileLock.read(rootDir.resolve(BundlerConstants.GEMFILE_LOCK));
            }
        }

        boolean rails = gems.stream().anyMatch(gem -> BundlerConstants.RAILS_GEM.equals(gem.name()));

        moduleNode.createChild(ProjectKeys.CONTENT_ROOT, createContentRoot(rootDir, rails));
        moduleNode.createChild(BundlerModuleData.KEY, new BundlerModuleData(sdkName, rails));

        for (BundlerGem gem : gems) {
            if (gem.isInstalled() && Path.of(gem.path()).toAbsolutePath().normalize().startsWith(rootDir)) {
                // the gem of the project itself, from a 'gemspec' line
                continue;
            }
            addGemLibrary(projectNode, moduleNode, gem);
        }

        addTask(projectNode, projectPath, BundlerConstants.TASK_INSTALL, RubyApiLocalize.bundlerTaskInstall().get(), BundlerConstants.TASK_GROUP_BUNDLER);
        addTask(projectNode, projectPath, BundlerConstants.TASK_UPDATE, RubyApiLocalize.bundlerTaskUpdate().get(), BundlerConstants.TASK_GROUP_BUNDLER);

        if (gemsInstalled && Files.isRegularFile(rootDir.resolve("Rakefile"))) {
            notify(listener, id, RubyApiLocalize.bundlerStatusRakeTasks().get());
            for (Map.Entry<String, String> task : readRakeTasks(rootDir, rubyExecutable).entrySet()) {
                addTask(projectNode, projectPath, task.getKey(), task.getValue(), BundlerConstants.TASK_GROUP_RAKE);
            }
        }
        return projectNode;
    }

    private static ContentRootData createContentRoot(Path rootDir, boolean rails) {
        ContentRootData contentRoot = new ContentRootData(BundlerConstants.SYSTEM_ID, rootDir.toString());
        storeExisting(contentRoot, rootDir, ExternalSystemSourceType.SOURCE, SOURCE_DIRS);
        if (rails) {
            storeExisting(contentRoot, rootDir, ExternalSystemSourceType.SOURCE, RAILS_SOURCE_DIRS);
        }
        storeExisting(contentRoot, rootDir, ExternalSystemSourceType.TEST, TEST_DIRS);
        storeExisting(contentRoot, rootDir, ExternalSystemSourceType.EXCLUDED, EXCLUDED_DIRS);
        return contentRoot;
    }

    private static void storeExisting(ContentRootData contentRoot, Path rootDir, ExternalSystemSourceType type, List<String> relativePaths) {
        for (String relativePath : relativePaths) {
            Path dir = rootDir.resolve(relativePath);
            if (Files.isDirectory(dir)) {
                contentRoot.storePath(type, dir.toString());
            }
        }
    }

    private static void addGemLibrary(DataNode<ProjectData> projectNode, DataNode<ModuleData> moduleNode, BundlerGem gem) {
        LibraryData library = new LibraryData(BundlerConstants.SYSTEM_ID, "Gem: " + gem.name() + " (" + gem.version() + ")", !gem.isInstalled());
        for (String requirePath : gem.requirePaths()) {
            if (Files.isDirectory(Path.of(requirePath))) {
                library.addPath(LibraryPathType.BINARY, requirePath);
                library.addPath(LibraryPathType.SOURCE, requirePath);
            }
        }
        projectNode.createChild(ProjectKeys.LIBRARY, library);
        moduleNode.createChild(ProjectKeys.LIBRARY_DEPENDENCY, new LibraryDependencyData(moduleNode.getData(), library, LibraryLevel.PROJECT));
    }

    private static void addTask(DataNode<ProjectData> projectNode, String projectPath, String name, String description, String group) {
        TaskData task = new TaskData(BundlerConstants.SYSTEM_ID, name, projectPath, description.isEmpty() ? null : description);
        task.setGroup(group);
        projectNode.createChild(ProjectKeys.TASK, task);
    }

    @Nullable
    private BundlerProbeResult runProbe(ExternalSystemTaskId id, Path rootDir, String rubyExecutable, ExternalSystemTaskNotificationListener listener) {
        GeneralCommandLine commandLine = BundlerProcessRunner.createCommandLine(rubyExecutable, rootDir, List.of("-e", BundlerProbe.SCRIPT));
        StringBuilder stdout = new StringBuilder();
        int exitCode;
        try {
            exitCode = myRunner.run(commandLine, (text, isStdout) -> {
                if (isStdout) {
                    stdout.append(text);
                }
                else {
                    listener.onTaskOutput(id, text, false);
                }
            }, PROBE_TIMEOUT_MS);
        }
        catch (ExecutionException e) {
            output(listener, id, RubyApiLocalize.bundlerErrorNoRuby().get());
            LOG.warn("Cannot run " + rubyExecutable, e);
            return null;
        }

        if (exitCode == BundlerProcessRunner.CANCELLED_EXIT_CODE) {
            throw new ExternalSystemException(RubyApiLocalize.bundlerErrorCancelled().get());
        }
        return BundlerProbe.parse(stdout.toString());
    }

    private Map<String, String> readRakeTasks(Path rootDir, String rubyExecutable) {
        GeneralCommandLine commandLine = BundlerProcessRunner.createCommandLine(rubyExecutable, rootDir, concat(BundlerProcessRunner.bundledRake(), "-AT"));
        StringBuilder stdout = new StringBuilder();
        try {
            int exitCode = myRunner.run(commandLine, (text, isStdout) -> {
                if (isStdout) {
                    stdout.append(text);
                }
            }, RAKE_TIMEOUT_MS);
            if (exitCode != 0) {
                return Map.of();
            }
        }
        catch (ExecutionException e) {
            LOG.warn("Cannot read the rake tasks of " + rootDir, e);
            return Map.of();
        }
        return BundlerRakeTasks.parse(stdout.toString());
    }

    private static List<String> concat(List<String> head, String... tail) {
        List<String> result = new ArrayList<>(head);
        result.addAll(List.of(tail));
        return result;
    }

    private static void notify(ExternalSystemTaskNotificationListener listener, ExternalSystemTaskId id, String message) {
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, message));
    }

    private static void output(ExternalSystemTaskNotificationListener listener, ExternalSystemTaskId id, String message) {
        listener.onTaskOutput(id, message + "\n", false);
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        myRunner.cancel();
        return true;
    }
}
