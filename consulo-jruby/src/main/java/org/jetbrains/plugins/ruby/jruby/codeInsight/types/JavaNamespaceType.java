package org.jetbrains.plugins.ruby.jruby.codeInsight.types;

import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.MessageImpl;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.RSymbolTypeImpl;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.language.psi.PsiElement;
import consulo.util.lang.Comparing;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.jruby.codeInsight.resolve.JavaResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Message;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author: oleg
 * @date: Aug 21, 2008
 */
public class JavaNamespaceType extends RSymbolTypeImpl
{
	public JavaNamespaceType(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol, final Context context, final boolean inReference)
	{
		super(fileSymbol, symbol, context, inReference);
	}

	@Nonnull
	@Override
	public Collection<Message> getMessages()
	{
		final Collection<Message> messages = new ArrayList<Message>(super.getMessages());
		for(PsiElement element : JavaResolveUtil.getTopLevelPackagesAndClasses(getSymbol().getProject()))
		{
			if(element instanceof PsiClass)
			{
				final String name = ((PsiClass) element).getName();
				if(name != null)
				{
					messages.add(new MessageImpl(name, 0, true, new ForeignSymbol(element, name, null, Type.FOREIGN_CLASS)));
				}
			}
			if(element instanceof PsiJavaPackage)
			{
				final String name = ((PsiJavaPackage) element).getName();
				if(name != null)
				{
					messages.add(new MessageImpl(name, 0, true, new ForeignSymbol(element, name, null, Type.FOREIGN_PACKAGE)));
				}
			}
		}
		return messages;
	}

	@Override
	public Collection<Message> getMessagesForName(@Nullable final String name)
	{
		if(name == null)
		{
			return getMessages();
		}
		final Collection<Message> messagesForName = new ArrayList<Message>(super.getMessagesForName(name));
		for(PsiElement element : JavaResolveUtil.getTopLevelPackagesAndClasses(getSymbol().getProject()))
		{
			if(element instanceof PsiClass)
			{
				if(Comparing.equal(name, ((PsiClass) element).getName()))
				{
					messagesForName.add(new MessageImpl(name, 0, true, new ForeignSymbol(element, name, null, Type.FOREIGN_CLASS)));
				}
			}
			if(element instanceof PsiJavaPackage)
			{
				if(Comparing.equal(name, ((PsiJavaPackage) element).getName()))
				{
					messagesForName.add(new MessageImpl(name, 0, true, new ForeignSymbol(element, name, null, Type.FOREIGN_CLASS)));
				}
			}
		}
		return messagesForName;
	}

	@Override
	public String toString()
	{
		return "Java module type: " + getSymbol();
	}
}
