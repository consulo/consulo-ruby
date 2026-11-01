package consulo.ruby.impl.rails;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import org.jetbrains.plugins.ruby.rails.RailsConstants;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The generators of a Rails application, as <code>bin/rails generate --help</code> lists them.
 */
public final class RailsGenerators {
    public static final String GENERATE_COMMAND = "generate";

    private static final int TIMEOUT_MS = 60_000;
    private static final Pattern SECTION = Pattern.compile("^(\\S.*):\\s*$");
    private static final Pattern GENERATOR = Pattern.compile("^ {2}([a-z][\\w:\\-]*)\\s*$");
    private static final List<String> NOT_GENERATOR_SECTIONS = List.of("Usage", "General options", "Options", "Runtime options");

    private RailsGenerators() {
    }

    /**
     * @return the generators in the order of the help: Rails' own first, then those of the gems
     */
    public static List<String> parse(String help) {
        List<String> generators = new ArrayList<>();
        boolean inGenerators = false;
        for (String line : help.split("\\R")) {
            Matcher section = SECTION.matcher(line);
            if (section.matches()) {
                inGenerators = !NOT_GENERATOR_SECTIONS.contains(section.group(1).trim());
                continue;
            }
            Matcher generator = GENERATOR.matcher(line);
            if (inGenerators && generator.matches()) {
                generators.add(generator.group(1));
            }
        }
        return generators;
    }

    /**
     * Runs <code>bin/rails generate --help</code> of the module's application.
     */
    public static List<String> load(Module module) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(module);
        commandLine.addParameter("--help");
        ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine, TIMEOUT_MS);
        List<String> generators = parse(output.getStdout());
        if (generators.isEmpty()) {
            String error = output.getStderr().isBlank() ? output.getStdout() : output.getStderr();
            throw new ExecutionException(error.isBlank() ? "exit code " + output.getExitCode() : error.trim());
        }
        return generators;
    }

    /**
     * @return <code>ruby bin/rails generate</code> in the application root - bin/rails loads the bundle itself
     */
    public static GeneralCommandLine createCommandLine(Module module) throws ExecutionException {
        String root = getApplicationRoot(module);
        Sdk sdk = RModuleUtil.getModuleSdk(module);
        if (root == null || sdk == null) {
            throw new ExecutionException("No Rails application or Ruby SDK in " + module.getName());
        }
        GeneralCommandLine commandLine = new GeneralCommandLine()
            .withExePath(RubySdkUtil.getVMExecutablePath(sdk))
            .withWorkDirectory(root)
            .withCharset(StandardCharsets.UTF_8);
        commandLine.addParameters(Path.of(root, RailsConstants.RAILS_SCRIPT).toString(), GENERATE_COMMAND);
        return commandLine;
    }

    public static @Nullable String getApplicationRoot(Module module) {
        return RailsFacetUtil.getRailsAppHomeDirPath(module);
    }
}
