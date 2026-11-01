package consulo.ruby.impl.newProject;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressBuilderFactory;
import consulo.content.bundle.Sdk;
import consulo.fileEditor.FileEditorManager;
import consulo.logging.Logger;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ruby.impl.bundler.BundlerProjectLinker;
import consulo.ruby.impl.bundler.BundlerRubyLocator;
import consulo.ui.MessageBoxes;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.wizard.WizardStep;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyNewModuleBuilderProcessor implements NewModuleBuilderProcessor<RubyNewModuleWizardContext> {
    private static final Logger LOG = Logger.getInstance(RubyNewModuleBuilderProcessor.class);

    private final RubyProjectTemplate myTemplate;

    public RubyNewModuleBuilderProcessor(RubyProjectTemplate template) {
        myTemplate = template;
    }

    @Override
    public RubyNewModuleWizardContext createContext(boolean isNewProject) {
        return new RubyNewModuleWizardContext(isNewProject, myTemplate);
    }

    @Override
    public void buildSteps(Consumer<WizardStep<RubyNewModuleWizardContext>> consumer, RubyNewModuleWizardContext context) {
        consumer.accept(new RubyNewModuleSetupStep(context));
    }

    @RequiredReadAction
    @Override
    public void process(RubyNewModuleWizardContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        Sdk sdk = context.getSdk();

        MutableModuleExtensionWithSdk<?> extension = modifiableRootModel.getExtensionWithoutCheck(BundlerConstants.RUBY_EXTENSION_ID);
        if (extension != null) {
            extension.setEnabled(true);
            if (sdk != null) {
                extension.getInheritableSdk().set(null, sdk);
                modifiableRootModel.addModuleExtensionSdkEntry(extension);
            }
        }

        VirtualFile contentRoot = contentEntry.getFile();
        if (contentRoot == null) {
            return;
        }

        Path dir = Path.of(contentRoot.getPath());
        if (myTemplate == RubyProjectTemplate.APPLICATION) {
            try {
                RubyProjectGenerator.writeApplication(dir, context.getName());
            }
            catch (IOException e) {
                LOG.error("Cannot create the Ruby project in " + dir, e);
                return;
            }
        }

        String executable = sdk == null ? null : RubySdkUtil.getVMExecutablePath(sdk);
        String rubyExecutable = executable == null ? BundlerRubyLocator.PATH_RUBY : executable;
        String sdkName = sdk == null ? null : sdk.getName();

        // process() runs on the UI thread inside the module creation and the module is committed right after it
        Project project = modifiableRootModel.getProject();
        project.getApplication().invokeLater(() -> generateAndLink(project, UIAccess.current(), dir, rubyExecutable, sdkName), project.getDisposed());
    }

    @RequiredUIAccess
    private void generateAndLink(Project project, UIAccess uiAccess, Path dir, String rubyExecutable, @Nullable String sdkName) {
        if (myTemplate == RubyProjectTemplate.APPLICATION) {
            link(project, dir, sdkName);
            return;
        }

        project.getApplication().getInstance(ProgressBuilderFactory.class)
            .newProgressBuilder(project, RubyApiLocalize.newProjectProgress())
            .cancelable()
            .execute(uiAccess, () -> Coroutine.first(CodeExecution.<Object, String>supply(() -> RubyProjectGenerator.generate(myTemplate, dir, rubyExecutable))))
            .whenCompleteAsync((error, throwable) -> {
                if (project.isDisposed()) {
                    return;
                }
                String message = throwable != null ? throwable.getMessage() : error;
                if (message != null) {
                    MessageBoxes.okError(RubyApiLocalize.newProjectError(message)).showAsync();
                    return;
                }
                link(project, dir, sdkName);
            }, uiAccess);
    }

    @RequiredUIAccess
    private void link(Project project, Path dir, @Nullable String sdkName) {
        BundlerProjectLinker.link(project, dir, sdkName, () -> openEntryFile(project, RubyProjectGenerator.getEntryFile(myTemplate, dir)));
    }

    @RequiredUIAccess
    private static void openEntryFile(Project project, Path file) {
        VirtualFile entry = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file);
        if (entry != null && entry.isValid() && !project.isDisposed()) {
            FileEditorManager.getInstance(project).openFile(entry, true);
        }
    }
}
