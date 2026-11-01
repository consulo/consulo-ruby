/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.jruby.codeInsight.types;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 15, 2008
 * This class is used to extend Java class type with ruby methods
 * See jruby/lib/java/* for more details
 */
public class JRubyExtentionsUtil
{

	@NonNls
	public static final String JAVA_PROXY_METHODS = "JavaProxyMethods";
	@NonNls
	private static final String JAVA_UTIL_MAP = "java.util.Map";
	@NonNls
	private static final String JAVA_LANG_COMPARABLE = "java.lang.Comparable";
	@NonNls
	private static final String JAVA_UTIL_COLLECTION = "java.util.Collection";
	@NonNls
	private static final String JAVA_UTIL_ENUMERATION = "java.util.Enumeration";
	@NonNls
	private static final String JAVA_UTIL_ITERATOR = "java.util.Iterator";
	@NonNls
	private static final String JAVA_UTIL_LIST = "java.util.List";
	@NonNls
	private static final String JAVA_LANG_RUNNABLE = "java.lang.Runnable";

	@NonNls
	private static final String JAVA_UTIL_MAP_RB = "JavaUtilMap";
	@NonNls
	private static final String JAVA_LANG_COMPARABLE_RB = "JavaLangComparable";
	@NonNls
	private static final String JAVA_UTIL_COLLECTION_RB = "JavaUtilCollection";
	@NonNls
	private static final String JAVA_UTIL_ENUMERATION_RB = "JavaUtilEnumeration";
	@NonNls
	private static final String JAVA_UTIL_ITERATOR_RB = "JavaUtilIterator";
	@NonNls
	private static final String JAVA_UTIL_LIST_RB = "JavaUtilList";
	@NonNls
	private static final String JAVA_LANG_RUNNABLE_RB = "JavaLangRunnable";


	/*
	* Each proxy class is extended with JavaProxyMethods symbols
	*/
	public static void addJavaProxyMethods(@Nullable final FileSymbol fileSymbol, @Nonnull final Children children, @Nonnull final Context context)
	{
		// Here we add JavaProxyMethods to children
		SymbolUtil.includeTopLevelModuleSymbol(fileSymbol, children, context, JAVA_PROXY_METHODS);
	}

	public static void extendJavaClassWithStubs(@Nullable final FileSymbol fileSymbol, @Nonnull final Children children, @Nonnull final PsiClass clazzz, @Nonnull final Context context)
	{
		final Project project = clazzz.getProject();
		final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		final GlobalSearchScope scope = GlobalSearchScope.allScope(project);

		// java.util.Map
		final PsiClass javaUtilMapClass = javaPsiFacade.findClass(JAVA_UTIL_MAP, scope);
		if(javaUtilMapClass != null && clazzz.isInheritor(javaUtilMapClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_UTIL_MAP_RB);
		}

		// java.lang.Comparable
		final PsiClass javaLangComparableClass = javaPsiFacade.findClass(JAVA_LANG_COMPARABLE, scope);
		if(javaLangComparableClass != null && clazzz.isInheritor(javaLangComparableClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_LANG_COMPARABLE_RB);
		}

		// java.util.Collection
		final PsiClass javaUtilCollectionClass = javaPsiFacade.findClass(JAVA_UTIL_COLLECTION, scope);
		if(javaUtilCollectionClass != null && clazzz.isInheritor(javaUtilCollectionClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_UTIL_COLLECTION_RB);
		}

		// java.util.Enumeration
		final PsiClass javaUtilEnumerationClass = javaPsiFacade.findClass(JAVA_UTIL_ENUMERATION, scope);
		if(javaUtilEnumerationClass != null && clazzz.isInheritor(javaUtilEnumerationClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_UTIL_ENUMERATION_RB);
		}

		// java.util.Iterator
		final PsiClass javaUtilIteratorClass = javaPsiFacade.findClass(JAVA_UTIL_ITERATOR, scope);
		if(javaUtilIteratorClass != null && clazzz.isInheritor(javaUtilIteratorClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_UTIL_ITERATOR_RB);
		}

		// java.util.List
		final PsiClass javaUtilListClass = javaPsiFacade.findClass(JAVA_UTIL_LIST, scope);
		if(javaUtilListClass != null && clazzz.isInheritor(javaUtilListClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_UTIL_LIST_RB);
		}

		// java.lang.Runnable
		final PsiClass javaLangRunnableClass = javaPsiFacade.findClass(JAVA_LANG_RUNNABLE, scope);
		if(javaLangRunnableClass != null && clazzz.isInheritor(javaLangRunnableClass, true))
		{
			SymbolUtil.includeTopLevelClassSymbol(fileSymbol, children, context, JAVA_LANG_RUNNABLE_RB);
		}
	}
}
