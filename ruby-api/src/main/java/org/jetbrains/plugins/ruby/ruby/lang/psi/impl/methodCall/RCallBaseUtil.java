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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualName;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.FieldAttrType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.RNameUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RFileUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RubyCallType;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 9, 2008
 */
public class RCallBaseUtil
{

	@Nonnull
	public static RubyCallType getCallType(@Nonnull final RCall call)
	{
		final String command = call.getCommand();
		if(RCall.REQUIRE_COMMAND.equals(command))
		{
			return RubyCallType.REQUIRE_CALL;
		}
		if(RCall.LOAD_COMMAND.equals(command))
		{
			return RubyCallType.LOAD_CALL;
		}

		if(RCall.INCLUDE_COMMAND.equals(command))
		{
			return RubyCallType.INCLUDE_CALL;
		}
		if(RCall.EXTEND_COMMAND.equals(command))
		{
			return RubyCallType.EXTEND_CALL;
		}

		if(RCall.ATTR_READER_COMMAND.equals(command))
		{
			return RubyCallType.ATTR_READER_CALL;
		}
		if(RCall.ATTR_WRITER_COMMAND.equals(command))
		{
			return RubyCallType.ATTR_WRITER_CALL;
		}
		if(RCall.ATTR_ACCESSOR_COMMAND.equals(command))
		{
			return RubyCallType.ATTR_ACCESSOR_CALL;
		}
		if(RCall.ATTR_INTERNAL.equals(command))
		{
			return RubyCallType.ATTR_INTERNAL_CALL;
		}
		if(RCall.CATTR_ACCESSOR.equals(command))
		{
			return RubyCallType.CATTR_ACCESSOR_CALL;
		}

		if(RCall.PRIVATE_COMMAND.equals(command))
		{
			return RubyCallType.PRIVATE_CALL;
		}
		if(RCall.PROTECTED_COMMAND.equals(command))
		{
			return RubyCallType.PROTECTED_CALL;
		}
		if(RCall.PUBLIC_COMMAND.equals(command))
		{
			return RubyCallType.PUBLIC_CALL;
		}

		// JRuby specific
		if(RCall.IMPORT_COMMAND.equals(command) || RCall.JAVA_IMPORT_COMMAND.equals(command))
		{
			return RubyCallType.IMPORT_CALL;
		}
		if(RCall.INCLUDE_CLASS_COMMAND.equals(command))
		{
			return RubyCallType.INCLUDE_CLASS_CALL;
		}
		if(RCall.INCLUDE_PACKAGE_COMMAND.equals(command))
		{
			return RubyCallType.INCLUDE_PACKAGE_CALL;
		}

		// Gems specific
		if(RCall.REQUIRE_GEM_COMMAND.equals(command))
		{
			return RubyCallType.REQUIRE_GEM_CALL;
		}
		if(RCall.GEM_COMMAND.equals(command))
		{
			return RubyCallType.GEM_CALL;
		}

		return RubyCallType.UNKNOWN;
	}

	public static StructureType getType(@Nonnull final RCall call)
	{
		final RubyCallType callType = call.getCallType();
		if(callType == RubyCallType.REQUIRE_CALL)
		{
			return StructureType.CALL_REQUIRE;
		}
		if(callType == RubyCallType.LOAD_CALL)
		{
			return StructureType.CALL_LOAD;
		}
		if(callType == RubyCallType.INCLUDE_CALL)
		{
			return StructureType.CALL_INCLUDE;
		}
		if(callType == RubyCallType.EXTEND_CALL)
		{
			return StructureType.CALL_EXTEND;
		}
		if(callType.isAttributeCall())
		{
			return StructureType.FIELD_ATTR_CALL;
		}
		if(callType == RubyCallType.IMPORT_CALL)
		{
			return StructureType.CALL_IMPORT;
		}
		if(callType == RubyCallType.INCLUDE_CLASS_CALL)
		{
			return StructureType.CALL_INCLUDE_CLASS;
		}
		if(callType == RubyCallType.INCLUDE_PACKAGE_CALL)
		{
			return StructureType.CALL_INCLUDE_PACKAGE;
		}
		return StructureType.FAKE;
	}

	/*
	 * returns fieldAttr by CallType
	 */
	public static FieldAttrType getFieldAttrType(@Nonnull final RCall call)
	{
		final RubyCallType callType = getCallType(call);
		if(callType == RubyCallType.ATTR_ACCESSOR_CALL)
		{
			return FieldAttrType.ATTR_ACCESSOR;
		}
		if(callType == RubyCallType.ATTR_READER_CALL)
		{
			return FieldAttrType.ATTR_READER;
		}
		if(callType == RubyCallType.ATTR_WRITER_CALL)
		{
			return FieldAttrType.ATTR_WRITER;
		}
		if(callType == RubyCallType.ATTR_INTERNAL_CALL)
		{
			return FieldAttrType.ATTR_INTERNAL;
		}
		if(callType == RubyCallType.CATTR_ACCESSOR_CALL)
		{
			return FieldAttrType.CATTR_ACCESSOR;
		}
		return null;
	}

	/*
	 * Gathers virtual names. Used in include, extend
	 */
	public static List<RVirtualName> gatherVirtualNames(@Nonnull final RCall call)
	{
		final ArrayList<RVirtualName> includes = new ArrayList<RVirtualName>();
		for(RPsiElement include : call.getArguments())
		{
			final boolean global = RNameUtil.isGlobal(include);
			includes.add(RVirtualName.of(RNameUtil.getPath(include), global));
		}
		return includes;
	}

	/*
	 * Gathers possible urls. Used in require, load
	 */
	public static List<String> gatherUrls(@Nonnull final RCall call, @Nullable final VirtualFile file)
	{
		final ArrayList<String> requires = new ArrayList<String>();
		for(RPsiElement require : call.getArguments())
		{
			requires.add(RFileUtil.evaluate(file, require));
		}
		return requires;
	}

	/*
	 * Gathers strings and symbols. Used in field attr calls
	 */
	public static List<String> gatherStrings(@Nonnull final RCall call)
	{
		final ArrayList<String> names = new ArrayList<String>();
		for(RPsiElement arg : call.getArguments())
		{
			names.add(RubyPsiUtil.evaluate(arg));
		}
		return names;
	}

	/*
	 * Returns java reference full qualified name
	 */
	@Nullable
	public static String getJavaQualifiedName(final RCall call)
	{
		final List<RPsiElement> args = call.getArguments();
		return !args.isEmpty() ? RubyPsiUtil.evaluate(args.get(0)) : null;
	}
}
