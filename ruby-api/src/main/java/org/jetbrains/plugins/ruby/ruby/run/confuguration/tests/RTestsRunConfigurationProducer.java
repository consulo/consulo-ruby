package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.Location;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiElement;
import consulo.util.lang.ref.SimpleReference;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationType.FileSemantic;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunTarget;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class RTestsRunConfigurationProducer extends RunConfigurationProducer<AbstractRubyRunConfiguration>
{
	public RTestsRunConfigurationProducer()
	{
		super(RubyRunConfigurationType.getInstance().getTestsFactory());
	}

	@Override
	protected boolean setupConfigurationFromContext(AbstractRubyRunConfiguration configuration, ConfigurationContext context, SimpleReference<PsiElement> sourceElement)
	{
		final RubyRunTarget target = findTarget(context);
		if(target == null || !RubyRunConfigurationType.getInstance().setupConfiguration(configuration, target))
		{
			return false;
		}
		sourceElement.set(target.getSourceElement());
		return true;
	}

	@Override
	public boolean isConfigurationFromContext(AbstractRubyRunConfiguration configuration, ConfigurationContext context)
	{
		// the script and the tests configurations share the type: the platform offers this producer both kinds
		if(!(configuration instanceof RTestsRunConfiguration))
		{
			return false;
		}
		final RubyRunTarget target = findTarget(context);
		return target != null && RubyRunConfigurationType.getInstance().isConfigurationOfTarget(configuration, target);
	}

	@Nullable
	private static RubyRunTarget findTarget(ConfigurationContext context)
	{
		final Location location = context.getLocation();
		final RubyRunTarget target = location == null ? null : RubyRunConfigurationType.getInstance().findTarget(location);
		return target != null && target.getSemantic() == FileSemantic.TEST_UNIT_TEST ? target : null;
	}
}
