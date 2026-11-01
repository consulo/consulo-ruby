package consulo.ruby.impl.bundler.task;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.Location;
import consulo.execution.action.RunConfigurationProducer;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import consulo.externalSystem.service.execution.ExternalSystemRunConfiguration;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.util.lang.ref.SimpleReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Runs the Rake task at the caret through Bundler.
 */
@ExtensionImpl
public class RakeTaskRunConfigurationProducer extends RunConfigurationProducer<ExternalSystemRunConfiguration> {
    public RakeTaskRunConfigurationProducer() {
        super(ConfigurationTypeUtil.findConfigurationType(BundlerExternalTaskConfigurationType.class));
    }

    @RequiredReadAction
    @Override
    protected boolean setupConfigurationFromContext(ExternalSystemRunConfiguration configuration,
                                                    ConfigurationContext context,
                                                    SimpleReference<PsiElement> sourceElement) {
        Location location = context.getLocation();
        RCall taskCall = location == null ? null : RakeTaskUtil.findTaskCall(location.getPsiElement());
        String taskName = taskCall == null ? null : RakeTaskUtil.getTaskName(taskCall);
        String projectPath = getProjectPath(context.getModule());
        if (taskName == null || projectPath == null) {
            return false;
        }

        ExternalSystemTaskExecutionSettings settings = configuration.getSettingsCopy();
        settings.setExternalProjectPath(projectPath);
        settings.setTaskNames(List.of(taskName));
        configuration.setSettings(settings);
        configuration.setName("rake " + taskName);
        sourceElement.set(taskCall);
        return true;
    }

    @RequiredReadAction
    @Override
    public boolean isConfigurationFromContext(ExternalSystemRunConfiguration configuration, ConfigurationContext context) {
        Location location = context.getLocation();
        RCall taskCall = location == null ? null : RakeTaskUtil.findTaskCall(location.getPsiElement());
        String taskName = taskCall == null ? null : RakeTaskUtil.getTaskName(taskCall);
        String projectPath = getProjectPath(context.getModule());
        ExternalSystemTaskExecutionSettings settings = configuration.getSettingsCopy();
        return taskName != null
            && projectPath != null
            && projectPath.equals(settings.getExternalProjectPath())
            && List.of(taskName).equals(settings.getTaskNames());
    }

    private static @Nullable String getProjectPath(@Nullable Module module) {
        if (!ExternalSystemApiUtil.isExternalSystemAwareModule(BundlerConstants.SYSTEM_ID, module)) {
            return null;
        }
        return ExternalSystemApiUtil.getExternalProjectPath(module);
    }
}
