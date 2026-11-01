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

package org.jetbrains.plugins.ruby.ruby.lang.parser;

import consulo.language.ast.IFileElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubyClassElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubyFileElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubyMethodElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubyModuleElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubyObjectClassElementType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.elements.RubySingletonMethodElementType;
import consulo.language.ast.IElementType;


public interface RubyElementTypes
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// These types will be used to mark tree elements in syntactic tree //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	IFileElementType FILE = RubyFileElementType.INSTANCE;

	// Expressions
	IElementType EXPRESSION_IN_PARENS = new RubyElementType("Expression in parens");
	IElementType UNARY_EXPRESSION = new RubyElementType("Unary expression");

	IElementType MATH_BINARY_EXPRESSION = new RubyElementType("Math binary expression");
	IElementType NEGATIVE_EXPRESSION = new RubyElementType("Negative expression");

	IElementType BOOL_BINARY_EXPRESSION = new RubyElementType("Boolean binary expression");
	IElementType BOOL_NEGATIVE_EXPRESSION = new RubyElementType("Boolean negative expression");
	IElementType BOOL_MATCHING_EXPRESSION = new RubyElementType("Boolean matching expression");

	IElementType SHIFT_EXPRESSION = new RubyElementType("Shift operation");
	IElementType BIT_EXPRESSION = new RubyElementType("Bit expression");
	IElementType RANGE_EXPRESSION = new RubyElementType("Range expression");
	IElementType TERNARY_EXPRESSION = new RubyElementType("Ternary expression");

	IElementType ASSIGNMENT_EXPRESSION = new RubyElementType("Assignment expression");
	IElementType SELF_ASSIGNMENT_EXPRESSION = new RubyElementType("Self assignment expression");
	IElementType MULTI_ASSIGNMENT_EXPRESSION = new RubyElementType("Multi assignment expression");

	IElementType LIST_OF_EXPRESSIONS = new RubyElementType("List of expressions");

	// strings
	IElementType STRING = new RubyElementType("Double quoted like string");
	IElementType NI_STRING = new RubyElementType("Single quoted like string");
	IElementType X_STRING = new RubyElementType("Double quoted like x command string");
	IElementType NI_X_STRING = new RubyElementType("Double quoted like x command string");

	IElementType HEREDOC_ID = new RubyElementType("Heredoc id");
	IElementType HEREDOC_VALUE = new RubyElementType("Heredoc value");

	// words
	IElementType WORDS = new RubyElementType("Double quoted like words");
	IElementType NI_WORDS = new RubyElementType("Single quoted like words");

	// regular expression
	IElementType REGEXP = new RubyElementType("Regular expression");

	IElementType STRINGS = new RubyElementType("Strings");
	IElementType EXPR_SUBTITUTION = new RubyElementType("Expression subtitution");


	// references
	IElementType ARRAY_REFERENCE = new RubyElementType("Array reference");
	IElementType DOT_REFERENCE = new RubyElementType("Dot reference");
	IElementType COLON_REFERENCE = new RubyElementType("Colon reference");

	// calls
	IElementType FUNCTION_CALL = new RubyElementType("Function call");
	IElementType COMMAND_CALL = new RubyElementType("Command call");

	IElementType FUNCTION_ARGUMENT_LIST = new RubyElementType("Function argument list");
	IElementType COMMAND_ARGUMENT_LIST = new RubyElementType("Command argument list");
	IElementType BLOCK_TO_ARG = new RubyElementType("Block to argument");
	IElementType ARRAY_TO_ARGS = new RubyElementType("Array to arguments");


	// numbers
	IElementType INTEGER = new RubyElementType("Integer");
	IElementType FLOAT = new RubyElementType("Float");

	// arrays
	IElementType ARRAY = new RubyElementType("Array");

	// hash
	IElementType ASSOC_LIST = new RubyElementType("Assoc list");
	IElementType ASSOC = new RubyElementType("Assoc");

	IElementType KEYWORD_ARGUMENT = new RubyElementType("Keyword argument");
	IElementType KEYWORD_REST_ARGUMENT = new RubyElementType("Keyword rest argument");
	IElementType FORWARD_ARGUMENT = new RubyElementType("Forward argument");
	IElementType LAMBDA = new RubyElementType("Lambda");
	IElementType PATTERN = new RubyElementType("Pattern");
	IElementType PATTERN_GUARD = new RubyElementType("Pattern guard");
	IElementType PATTERN_MATCH = new RubyElementType("Pattern match");
	IElementType IN_CASE = new RubyElementType("In case");

	// identifier, constant, etc
	IElementType IDENTIFIER = new RubyElementType("Identifier");
	IElementType CONSTANT = new RubyElementType("Constant");
	IElementType FID = new RubyElementType("Fid");
	IElementType INSTANCE_VARIABLE = new RubyElementType("Instance variable");
	IElementType CLASS_VARIABLE = new RubyElementType("Class variable");
	IElementType GLOBAL_VARIABLE = new RubyElementType("Global variable");
	IElementType BACKREF = new RubyElementType("Back reference");
	IElementType NTHREF = new RubyElementType("Nth reference");
	IElementType SYMBOL = new RubyElementType("Symbol");
	IElementType PSEUDO_CONSTANT = new RubyElementType("Pseudo constant");
	// It`s a fake element, we don`t create any nodes on it, just use it as result of parsing
	IElementType SUPER = new RubyElementType("Super");

	// control structures
	IElementType IF_STATEMENT = new RubyElementType("If statement");
	IElementType IF_MOD_STATEMENT = new RubyElementType("If modifier statement");
	IElementType UNLESS_STATEMENT = new RubyElementType("Unless statement");
	IElementType UNLESS_MOD_STATEMENT = new RubyElementType("Unless modifier statement");
	IElementType CASE_STATEMENT = new RubyElementType("Case statement");
	IElementType WHEN_CASE = new RubyElementType("When case");
	IElementType WHILE_STATEMENT = new RubyElementType("While statement");
	IElementType WHILE_MOD_STATEMENT = new RubyElementType("While modifier statement");
	IElementType UNTIL_STATEMENT = new RubyElementType("Until statement");
	IElementType UNTIL_MOD_STATEMENT = new RubyElementType("Until modifier statement");
	IElementType ALIAS_STATEMENT = new RubyElementType("Alias statement");
	IElementType DEFINED_STATEMENT = new RubyElementType("Defined statement");

	IElementType FOR_STATEMENT = new RubyElementType("For statement");
	IElementType YIELD_STATEMENT = new RubyElementType("Yield statement");
	IElementType BEGIN_END_BLOCK_STATEMENT = new RubyElementType("Block statement");
	IElementType RETURN_STATEMENT = new RubyElementType("Return statement");
	IElementType RETRY_STATEMENT = new RubyElementType("Retry statement");
	IElementType BREAK_STATEMENT = new RubyElementType("Break statement");
	IElementType NEXT_STATEMENT = new RubyElementType("Next statement");
	IElementType UNDEF_STATEMENT = new RubyElementType("Undef statement");
	IElementType REDO_STATEMENT = new RubyElementType("Redo statement");
	IElementType LBEGIN_STATEMENT = new RubyElementType("Large begin statement");
	IElementType LEND_STATEMENT = new RubyElementType("Large end statement");
	IElementType RESCUE_MOD_STATEMENT = new RubyElementType("Rescue modifier statement");


	IElementType CLASS = new RubyClassElementType();
	IElementType SUPER_CLASS = new RubyElementType("Superclass");
	IElementType OBJECT_CLASS = new RubyObjectClassElementType();
	IElementType CLASS_NAME = new RubyElementType("Class name");

	IElementType METHOD = new RubyMethodElementType();
	IElementType SINGLETON_METHOD = new RubySingletonMethodElementType();
	IElementType CLASS_OBJECT = new RubyElementType("Class object");
	IElementType METHOD_NAME = new RubyElementType("Method name");
	IElementType FNAME = new RubyElementType("Function name");

	IElementType ARGUMENT = new RubyElementType("Argument");
	IElementType BLOCK_ARGUMENT = new RubyElementType("Block argument");
	IElementType ARRAY_ARGUMENT = new RubyElementType("Array argument");
	IElementType PREDEFINED_ARGUMENT = new RubyElementType("Predefined argument");

	IElementType MODULE = new RubyModuleElementType();
	IElementType MODULE_NAME = new RubyElementType("Module name");


	IElementType CONDITION = new RubyElementType("Condition");
	IElementType ELSE_BLOCK = new RubyElementType("Else block");
	IElementType ELSIF_BLOCK = new RubyElementType("Elsif block");
	IElementType RESCUE_BLOCK = new RubyElementType("Rescue block");
	IElementType ENSURE_BLOCK = new RubyElementType("Ensure block");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType BLOCK_CALL = new RubyElementType("Block call");
	IElementType DO_CODE_BLOCK = new RubyElementType("Do code block");
	IElementType BRACE_CODE_BLOCK = new RubyElementType("Brace code block");
	IElementType BLOCK_VARIABLES = new RubyElementType("Block variables");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType COMPOUND_STATEMENT = new RubyElementType("Compound statement");
	IElementType BODY_STATEMENT = new RubyElementType("Body statement");
	IElementType OP = new RubyElementType("Operation");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////// Virtual IElements /////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType EMPTY_INPUT = new RubyElementType("empty input");
	IElementType MRHS = new RubyElementType("Multiassignment right part");
	IElementType MLHS = new RubyElementType("Multiassigment left part");
}
