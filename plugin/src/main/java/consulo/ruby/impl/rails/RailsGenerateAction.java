package consulo.ruby.impl.rails;

import consulo.application.progress.ProgressBuilderFactory;
import consulo.document.FileDocumentManager;
import consulo.execution.RunContentExecutor;
import consulo.language.editor.LangDataKeys;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.InputBoxBuilder;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.awt.Messages;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.rails.facet.RailsModuleSupport;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Runs a generator of the Rails application: <code>bin/rails generate model Post title:string</code>.
 */
public class RailsGenerateAction extends DumbAwareAction implements AnActionWithSyncUpdate {
    public RailsGenerateAction() {
        super(RubyApiLocalize.railsGenerateActionText(), RubyApiLocalize.railsGenerateActionDescription(), RubyApiIconGroup.railsRails());
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(Project.KEY) != null && RailsModuleSupport.isEnabled(e.getData(LangDataKeys.MODULE)));
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getRequiredData(Project.KEY);
        Module module = e.getData(LangDataKeys.MODULE);
        if (!RailsModuleSupport.isEnabled(module)) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        CompletableFuture<List<String>> generators = project.getApplication().getInstance(ProgressBuilderFactory.class).newProgressBuilder(project, RubyApiLocalize.railsGenerateProgress())
            .cancelable()
            .execute(uiAccess, () -> Coroutine.first(CodeExecution.supply(() -> {
                try {
                    return RailsGenerators.load(module);
                }
                catch (ExecutionException ex) {
                    throw new IllegalStateException(ex.getMessage(), ex);
                }
            })));

        generators.whenCompleteAsync((names, error) -> {
            if (error != null || names == null) {
                String message = error == null ? "" : error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
                Messages.showErrorDialog(project, RubyApiLocalize.railsGenerateErrorNoGenerators(message).get(), RubyApiLocalize.railsGenerateChooseTitle().get());
                return;
            }
            chooseGenerator(project, module, names);
        }, uiAccess);
    }

    @RequiredUIAccess
    private void chooseGenerator(Project project, Module module, List<String> generators) {
        UIAccess uiAccess = UIAccess.current();
        InputBoxBuilder.items(generators)
            .title(RubyApiLocalize.railsGenerateChooseTitle())
            .text(RubyApiLocalize.railsGenerateChooseText())
            .value(generators.contains("model") ? "model" : generators.get(0))
            .showAsync()
            .whenCompleteAsync((generator, error) -> {
                if (generator == null) {
                    return;
                }
                InputBoxBuilder.text()
                    .title(RubyApiLocalize.railsGenerateChooseTitle())
                    .text(RubyApiLocalize.railsGenerateArgumentsText(generator))
                    .showAsync()
                    .whenCompleteAsync((arguments, argumentsError) -> {
                        if (arguments != null) {
                            runGenerator(project, module, generator, ParametersListUtil.parse(arguments));
                        }
                    }, uiAccess);
            }, uiAccess);
    }

    @RequiredUIAccess
    private void runGenerator(Project project, Module module, String generator, List<String> arguments) {
        FileDocumentManager.getInstance().saveAllDocuments();
        try {
            GeneralCommandLine commandLine = RailsGenerators.createCommandLine(module);
            commandLine.addParameter(generator);
            commandLine.addParameters(arguments);
            ProcessHandler handler = project.getApplication().getInstance(ProcessHandlerBuilderFactory.class).newBuilder(commandLine).colored().build();

            String root = RailsGenerators.getApplicationRoot(module);
            new RunContentExecutor(project, handler)
                .withTitle(RubyApiLocalize.railsGenerateConsoleTitle(generator).get())
                .withRerun(() -> runGenerator(project, module, generator, arguments))
                .withAfterCompletion(() -> refresh(root))
                .run();
        }
        catch (ExecutionException ex) {
            Messages.showErrorDialog(project, ex.getMessage(), RubyApiLocalize.railsGenerateChooseTitle().get());
        }
    }

    private static void refresh(String root) {
        VirtualFile directory = root == null ? null : LocalFileSystem.getInstance().findFileByPath(root);
        if (directory != null) {
            directory.refresh(true, true);
        }
    }
}
