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

package org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall;

import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPossibleCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 14.08.2006
 */
public interface RCall extends RPossibleCall, RStructuralElement, RExpression
{
	/**
	 * Ruby defined function names
	 */
	@NonNls
	final String RAISE_COMMAND = "raise";

	@NonNls
	final String REQUIRE_COMMAND = "require";
	@NonNls
	final String LOAD_COMMAND = "load";

	@NonNls
	final String INCLUDE_COMMAND = "include";
	@NonNls
	final String EXTEND_COMMAND = "extend";

	@NonNls
	final String ATTR_READER_COMMAND = "attr_reader";
	@NonNls
	final String ATTR_WRITER_COMMAND = "attr_writer";
	@NonNls
	final String ATTR_ACCESSOR_COMMAND = "attr_accessor";

	@NonNls
	final String ATTR_INTERNAL = "attr_internal";
	@NonNls
	final String CATTR_ACCESSOR = "cattr_accessor";

	@NonNls
	final String PRIVATE_COMMAND = "private";
	@NonNls
	final String PUBLIC_COMMAND = "public";
	@NonNls
	final String PROTECTED_COMMAND = "protected";

	// JRuby specific call
	@NonNls
	final String IMPORT_COMMAND = "import";
	@NonNls
	final String JAVA_IMPORT_COMMAND = "java_import";
	@NonNls
	final String INCLUDE_CLASS_COMMAND = "include_class";
	@NonNls
	final String INCLUDE_PACKAGE_COMMAND = "include_package";

	// Gems specific call
	@NonNls
	final String REQUIRE_GEM_COMMAND = "require_gem";
	@NonNls
	final String GEM_COMMAND = "gem";

	/**
	 * @return DuckType of current call
	 */
	@Nonnull
	public RubyCallType getCallType();

	/**
	 * @return command object.
	 */
	@Nonnull
	public PsiElement getPsiCommand();

	/**
	 * @return PsiElement - arguments
	 */
	@Nonnull
	public RListOfExpressions getCallArguments();

	/**
	 * @return Command name
	 */
	@Nonnull
	public String getCommand();

	/**
	 * @return call arguments
	 */
	@Nonnull
	List<RPsiElement> getArguments();
}
