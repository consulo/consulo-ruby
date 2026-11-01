package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationType.FileSemantic;

/**
 * What a run started from a location in the code runs.
 */
public final class RubyRunTarget
{
	private final FileSemantic mySemantic;
	private final VirtualFile myFile;
	private final PsiElement mySourceElement;
	private final String myName;
	@Nullable
	private final TestType myTestType;
	@Nullable
	private final String myClassName;
	@Nullable
	private final String myMethodName;

	public RubyRunTarget(@Nonnull FileSemantic semantic,
						 @Nonnull VirtualFile file,
						 @Nonnull PsiElement sourceElement,
						 @Nonnull String name,
						 @Nullable TestType testType,
						 @Nullable String className,
						 @Nullable String methodName)
	{
		mySemantic = semantic;
		myFile = file;
		mySourceElement = sourceElement;
		myName = name;
		myTestType = testType;
		myClassName = className;
		myMethodName = methodName;
	}

	@Nonnull
	public FileSemantic getSemantic()
	{
		return mySemantic;
	}

	/**
	 * @return the script, or the folder of an all-in-folder test run
	 */
	@Nonnull
	public VirtualFile getFile()
	{
		return myFile;
	}

	@Nonnull
	public PsiElement getSourceElement()
	{
		return mySourceElement;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@Nullable
	public TestType getTestType()
	{
		return myTestType;
	}

	@Nullable
	public String getClassName()
	{
		return myClassName;
	}

	@Nullable
	public String getMethodName()
	{
		return myMethodName;
	}
}
