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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 13, 2007
 */
public class Types
{

	public static TypeSet EMPTY_CONTEXT_RESOLVE_TYPES = new TypeSet(Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.ALIAS,

			Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.CONSTANT, Type.GLOBAL_VARIABLE,

			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD,

			// Field readers and writers
			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,
			// for Rails
			Type.NOT_DEFINED);

	// The same as EMPTY_CONTEXT_RESOLVE_TYPES, but with fields
	public static TypeSet EMPTY_CONTEXT_AUTOCOMPLETE_TYPES = new TypeSet(Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.ALIAS,

			Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.CONSTANT, Type.GLOBAL_VARIABLE,

			Type.INSTANCE_FIELD, Type.CLASS_INSTANCE_FIELD, Type.CLASS_FIELD,

			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD,

			// Field readers and writers
			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,
			// for Rails
			Type.ATTRIBUTE, Type.NOT_DEFINED);

	public static TypeSet TOP_LEVEL_AUTOCOMPLETE_TYPES = new TypeSet(Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.ALIAS,

			Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.CONSTANT, Type.GLOBAL_VARIABLE,

			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD);

	public static TypeSet REFERENCE_AUTOCOMPLETE_TYPES = new TypeSet(Type.CLASS_METHOD, Type.INSTANCE_METHOD, Type.ALIAS,

			// Field readers and writers
			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,

			// Local variables concerned types
			Type.CALL_ACCESS, Type.CONSTANT_ACCESS, Type.FIELD_WRITE_ACCESS,

			Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.CONSTANT,

			// Field readers and writers
			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,

			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD);

	public static TypeSet OBJECT_SUPERCLASSES = new TypeSet(Type.MODULE, Type.CLASS, Type.LOCAL_VARIABLE);

	public static TypeSet FIELDS = new TypeSet(Type.INSTANCE_FIELD, Type.CLASS_INSTANCE_FIELD, Type.CLASS_FIELD);

	public static TypeSet MODULE_OR_CLASS = new TypeSet(Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.FOREIGN_CLASS);

	public static TypeSet MODULE_OR_CLASS_OR_CONSTANT = new TypeSet(Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.FOREIGN_CLASS, Type.CONSTANT);

	public static TypeSet METHODS = new TypeSet(Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.FOREIGN_METHOD);

	public static TypeSet ATTR_METHODS = new TypeSet(Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR);

	public static TypeSet METHODS_LIKE = new TypeSet(Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.FOREIGN_METHOD, Type.ALIAS,

			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,

			Type.CALL_ACCESS, Type.FIELD_WRITE_ACCESS);

	public static TypeSet ARGS = new TypeSet(Type.ARG_ARRAY, Type.ARG_BLOCK, Type.ARG_PREDEFINED, Type.ARG_SIMPLE);

	public static TypeSet STATIC_TYPES = new TypeSet(Type.CLASS_METHOD,

			Type.MODULE, Type.CLASS, Type.FOREIGN_PROXY_CLASS, Type.CONSTANT, Type.GLOBAL_VARIABLE,

			Type.CLASS_INSTANCE_FIELD, Type.CLASS_FIELD,

			// Java is static and instance
			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD,

			// for Rails
			Type.NOT_DEFINED);

	public static TypeSet INSTANCE_TYPES = new TypeSet(Type.INSTANCE_METHOD, Type.ALIAS,

			Type.INSTANCE_FIELD,

			// Java is static and instance
			Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD,

			// Field readers and writers
			Type.FIELD_READER, Type.FIELD_WRITER, Type.ATTR_INTERNAL, Type.CATTR_ACCESSOR,
			// for Rails
			Type.NOT_DEFINED);

	public static TypeSet FOREIGN = new TypeSet(Type.FOREIGN_CLASS, Type.FOREIGN_METHOD, Type.FOREIGN_PACKAGE, Type.FOREIGN_FIELD);

	public static TypeSet ALIAS_OBJECTS = new TypeSet(Type.CLASS, Type.MODULE, Type.INSTANCE_METHOD, Type.CLASS_METHOD, Type.CONSTANT, Type.GLOBAL_VARIABLE, Type.INSTANCE_FIELD, Type.CLASS_FIELD, Type.CLASS_INSTANCE_FIELD, Type.FOREIGN_CLASS, Type.FOREIGN_PROXY_CLASS);
}
