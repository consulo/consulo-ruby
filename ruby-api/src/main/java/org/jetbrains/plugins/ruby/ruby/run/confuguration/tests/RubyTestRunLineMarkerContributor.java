package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.Document;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.execution.test.TestIconMapper;
import consulo.execution.test.TestStateInfo;
import consulo.execution.test.TestStateStorage;
import consulo.language.Language;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Run icons of Ruby tests, showing the last result the test reporters gave for their location.
 */
public abstract class RubyTestRunLineMarkerContributor extends RunLineMarkerContributor
{
	private static final Function<PsiElement, String> TOOLTIP_PROVIDER = element -> "Run Test";

	protected static boolean isFirstLeaf(PsiElement leaf, @Nullable PsiElement element)
	{
		return element != null && PsiTreeUtil.getDeepestFirst(element) == leaf;
	}

	@RequiredReadAction
	protected static Info createInfo(PsiElement testElement)
	{
		return new Info(getTestStateIcon(testElement), TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
	}

	@RequiredReadAction
	private static Image getTestStateIcon(PsiElement testElement)
	{
		final String url = getLocationUrl(testElement);
		final TestStateStorage.Record state = url == null ? null : TestStateStorage.getInstance(testElement.getProject()).getState(url);
		if(state != null)
		{
			final TestStateInfo.Magnitude magnitude = TestIconMapper.getMagnitude(state.magnitude);
			if(magnitude != null)
			{
				switch(magnitude)
				{
					case ERROR_INDEX:
					case FAILED_INDEX:
						return ExecutionIconGroup.gutterRunerror();
					case PASSED_INDEX:
					case COMPLETE_INDEX:
						return ExecutionIconGroup.gutterRunsuccess();
					default:
				}
			}
		}
		return ExecutionIconGroup.gutterRun();
	}

	/**
	 * The location the test reporter gives the test or its suite: <code>file://path:line</code>
	 */
	@RequiredReadAction
	@Nullable
	private static String getLocationUrl(PsiElement testElement)
	{
		final PsiFile file = testElement.getContainingFile();
		final VirtualFile virtualFile = file.getVirtualFile();
		final Document document = PsiDocumentManager.getInstance(testElement.getProject()).getDocument(file);
		if(virtualFile == null || document == null)
		{
			return null;
		}
		return "file://" + virtualFile.getPath() + ":" + (document.getLineNumber(testElement.getTextOffset()) + 1);
	}

	@Override
	public Language getLanguage()
	{
		return RubyLanguage.INSTANCE;
	}
}
