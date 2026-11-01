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

import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.CoreTypes;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 17, 2007
 */
public class JRubyTypeConventions
{
	@NonNls
	private static final String CHAR = "char";
	@NonNls
	private static final String JAVA_LANG_STRING = "java.lang.String";
	@NonNls
	private static final String LONG = "long";
	@NonNls
	private static final String INT = "int";
	@NonNls
	private static final String JAVA_LANG_LONG = "java.lang.Long";
	@NonNls
	private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
	@NonNls
	private static final String FLOAT = "float";
	@NonNls
	private static final String DOUBLE = "double";
	@NonNls
	private static final String JAVA_LANG_FLOAT = "java.lang.Float";
	@NonNls
	private static final String JAVA_LANG_DOUBLE = "java.lang.Double";
	@NonNls
	private static final String BOOLEAN = "boolean";
	@NonNls
	private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";

	@Nullable
	public static String getRubyType(@Nonnull final String javaType)
	{
		if(CHAR.equals(javaType) || JAVA_LANG_STRING.equals(javaType))
		{
			return CoreTypes.String;
		}
		if(LONG.equals(javaType) || INT.equals(javaType) ||
				JAVA_LANG_LONG.equals(javaType) || JAVA_LANG_INTEGER.equals(javaType))
		{
			return CoreTypes.Fixnum;
		}
		if(FLOAT.equals(javaType) || DOUBLE.equals(javaType) ||
				JAVA_LANG_FLOAT.equals(javaType) || JAVA_LANG_DOUBLE.equals(javaType))
		{
			return CoreTypes.Float;
		}
		if(BOOLEAN.equals(javaType) || JAVA_LANG_BOOLEAN.equals(javaType))
		{
			return CoreTypes.TrueClass;
		}
		return null;
	}
}
