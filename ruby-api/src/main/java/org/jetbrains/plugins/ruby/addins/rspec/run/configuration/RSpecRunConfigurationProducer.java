package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.Location;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;

/**
 * Runs a spec folder, a spec file, or the example or group at the caret.
 */
@ExtensionImpl
public class RSpecRunConfigurationProducer extends RunConfigurationProducer<RSpecRunConfiguration>
{
	public RSpecRunConfigurationProducer()
	{
		super(RSpecRunConfigurationType.getInstance().getFactory());
	}

	@RequiredReadAction
	@Override
	protected boolean setupConfigurationFromContext(RSpecRunConfiguration configuration, ConfigurationContext context, SimpleReference<PsiElement> sourceElement)
	{
		final Location location = context.getLocation();
		final PsiElement element = location == null ? null : location.getPsiElement();
		if(element == null)
		{
			return false;
		}

		final Project project = context.getProject();
		final VirtualFile file;
		final String name;
		if(element instanceof PsiDirectory directory)
		{
			file = directory.getVirtualFile();
			if(!RSpecUtil.containsSpecFiles(file))
			{
				return false;
			}
			configuration.setTestType(TestType.ALL_IN_FOLDER);
			configuration.setTestsFolderPath(file.getPath());
			if(TextUtil.isEmptyOrWhitespaces(configuration.getTestFileMask()))
			{
				configuration.setTestFileMask(RSpecRunConfiguration.DEFAULT_TESTS_SEARCH_MASK);
			}
			name = RBundle.message("rspec.run.configuration.test.default.name", file.getName());
		}
		else
		{
			final PsiFile psiFile = element.getContainingFile();
			file = psiFile == null ? null : psiFile.getVirtualFile();
			if(!(psiFile instanceof RFile) || !RSpecUtil.isRSpecTestFile(file))
			{
				return false;
			}
			configuration.setTestScriptPath(file.getPath());

			final RBlockCall exampleOrGroup = RSpecUtil.findExampleOrGroup(element);
			final int line = exampleOrGroup == null ? 0 : getLine(exampleOrGroup);
			if(line > 0)
			{
				configuration.setTestType(TestType.TEST_METHOD);
				configuration.setExampleLine(line);
				name = file.getNameWithoutExtension() + ":" + line + " " + getDescription(exampleOrGroup);
				sourceElement.set(exampleOrGroup);
			}
			else
			{
				configuration.setTestType(TestType.TEST_SCRIPT);
				name = file.getNameWithoutExtension();
				sourceElement.set(psiFile);
			}
		}

		final Module module = RubyRunConfigurationUtil.getCorrespondingModule(project, file, configuration);
		if(module == null)
		{
			return false;
		}
		configuration.setModule(module);
		configuration.setName(name.trim());
		if(TextUtil.isEmptyOrWhitespaces(configuration.getWorkingDirectory()))
		{
			// specs run in the project root, where .rspec and spec_helper are found
			final VirtualFile contentRoot = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(file);
			configuration.setWorkingDirectory((contentRoot != null ? contentRoot : file.isDirectory() ? file : file.getParent()).getPath());
		}
		return true;
	}

	@RequiredReadAction
	@Override
	public boolean isConfigurationFromContext(RSpecRunConfiguration configuration, ConfigurationContext context)
	{
		final Location location = context.getLocation();
		final PsiElement element = location == null ? null : location.getPsiElement();
		if(element instanceof PsiDirectory directory)
		{
			return configuration.getTestType() == TestType.ALL_IN_FOLDER && directory.getVirtualFile().getPath().equals(configuration.getTestsFolderPath());
		}

		final PsiFile psiFile = element == null ? null : element.getContainingFile();
		final VirtualFile file = psiFile == null ? null : psiFile.getVirtualFile();
		if(file == null || !file.getPath().equals(configuration.getTestScriptPath()))
		{
			return false;
		}
		final RBlockCall exampleOrGroup = RSpecUtil.findExampleOrGroup(element);
		final int line = exampleOrGroup == null ? 0 : getLine(exampleOrGroup);
		return line > 0
			? configuration.getTestType() == TestType.TEST_METHOD && configuration.getExampleLine() == line
			: configuration.getTestType() == TestType.TEST_SCRIPT;
	}

	@RequiredReadAction
	private static int getLine(PsiElement element)
	{
		final PsiFile file = element.getContainingFile();
		final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
		return document == null ? 0 : document.getLineNumber(element.getTextOffset()) + 1;
	}

	@RequiredReadAction
	private static String getDescription(RBlockCall exampleOrGroup)
	{
		if(exampleOrGroup.getCall() instanceof RCall call && !call.getArguments().isEmpty())
		{
			final String text = call.getArguments().get(0).getText();
			return text.length() > 40 ? text.substring(0, 40) + "…" : text;
		}
		return "";
	}
}
