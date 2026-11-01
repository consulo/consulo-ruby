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

package org.jetbrains.plugins.ruby.ruby.lang.lexer;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyElementType;
import consulo.language.ast.IElementType;


public interface RubyTokenTypes
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// strings //////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tSTRING_LIKE_CONTENT = new RubyElementType("string like content");


	IElementType tXSTRING_BEG = new RubyElementType("shell command like string beginning");
	IElementType tDOUBLE_QUOTED_STRING_BEG = new RubyElementType("double quoted like string beginning");
	IElementType tSINGLE_QUOTED_STRING_BEG = new RubyElementType("single quoted like string beginning");
	IElementType T_STRING_CONTENT = new RubyElementType("string content");
	IElementType tESCAPE_SEQUENCE = new RubyElementType("escape sequence");
	IElementType tINVALID_ESCAPE_SEQUENCE = new RubyElementType("invalid escape sequence");
	IElementType tSTRING_END = new RubyElementType("string end");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// regular expressions //////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tREGEXP_BEG = new RubyElementType("regexp beginning");
	IElementType tREGEXP_CONTENT = new RubyElementType("regexp content");
	IElementType tREGEXP_END = new RubyElementType("regexp end");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// heredocs /////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tHEREDOC_ID = new RubyElementType("heredoc id");
	IElementType tHEREDOC_CONTENT = new RubyElementType("heredoc content");
	IElementType tHEREDOC_END = new RubyElementType("heredoc end");
	IElementType tHEREDOC_INDENTED_END = new RubyElementType("indented heredoc end");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// words and qwords /////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tWORDS_BEG = new RubyElementType("%w");
	IElementType tQWORDS_BEG = new RubyElementType("%W");
	IElementType tWORDS_CONTENT = new RubyElementType("words content");
	IElementType tWORDS_END = new RubyElementType("words end");

	IElementType tSTRING_DVAR = new RubyElementType("#");
	IElementType tSTRING_DBEG = new RubyElementType("#{");
	IElementType tSTRING_DEND = new RubyElementType("}");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// integer and float literals ///////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tINTEGER = new RubyElementType("integer literal");
	IElementType tFLOAT = new RubyElementType("float literal");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Operators ////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	IElementType tASSGN = new RubyElementType("=");

	IElementType tPLUS_OP_ASGN = new RubyElementType("+=");
	IElementType tMINUS_OP_ASGN = new RubyElementType("-=");
	IElementType tMULT_OP_ASGN = new RubyElementType("*=");
	IElementType tDIV_OP_ASGN = new RubyElementType("/=");
	IElementType tPOW_OP_ASGN = new RubyElementType("**=");
	IElementType tPERC_OP_ASGN = new RubyElementType("%=");

	IElementType tLSHFT_OP_ASGN = new RubyElementType("<<=");
	IElementType tRSHFT_OP_ASGN = new RubyElementType(">>=");

	IElementType tAND_OP_ASGN = new RubyElementType("&&=");
	IElementType tOR_OP_ASGN = new RubyElementType("||=");

	IElementType tBIT_AND_OP_ASGN = new RubyElementType("&=");
	IElementType tBIT_OR_OP_ASGN = new RubyElementType("|=");
	IElementType tXOR_OP_ASGN = new RubyElementType("^=");

	///////////////////////// Range operators ////////////////////////////////////////////////////////////////////////////////////
	IElementType tDOT3 = new RubyElementType("...");
	IElementType tDOT2 = new RubyElementType("..");


	IElementType tSTAR = new RubyElementType("s*"); // *arg
	IElementType tAMPER = new RubyElementType("a&"); // &arg
	IElementType tPIPE = new RubyElementType("p|"); // {|

	IElementType tQUESTION = new RubyElementType("?");
	IElementType tEXCLAMATION = new RubyElementType("!");

	IElementType tCMP = new RubyElementType("<=>");
	IElementType tEQQ = new RubyElementType("===");
	IElementType tEQ = new RubyElementType("==");
	IElementType tNEQ = new RubyElementType("!=");
	IElementType tGEQ = new RubyElementType(">=");
	IElementType tGT = new RubyElementType(">");
	IElementType tLEQ = new RubyElementType("<=");
	IElementType tLT = new RubyElementType("<");

	IElementType tMATCH = new RubyElementType("=~");
	IElementType tNMATCH = new RubyElementType("!~");

	IElementType tASSOC = new RubyElementType("=>");

	IElementType tLSHFT = new RubyElementType("<<");
	IElementType tRSHFT = new RubyElementType(">>");


	IElementType tASET = new RubyElementType("[]=");
	IElementType tAREF = new RubyElementType("[]");

	IElementType tAND = new RubyElementType("&&");
	IElementType tOR = new RubyElementType("||");

	IElementType tBIT_AND = new RubyElementType("&");
	IElementType tBIT_OR = new RubyElementType("|");
	IElementType tXOR = new RubyElementType("^");

	IElementType tPOW = new RubyElementType("**");
	IElementType tMULT = new RubyElementType("*");
	IElementType tDIV = new RubyElementType("/");
	IElementType tPERC = new RubyElementType("%");
	IElementType tPLUS = new RubyElementType("+");
	IElementType tMINUS = new RubyElementType("-");

	IElementType tUPLUS = new RubyElementType("u+"); // +number
	IElementType tUMINUS = new RubyElementType("u-"); // -number

	IElementType tUPLUS_OP = new RubyElementType("+@");
	IElementType tUMINUS_OP = new RubyElementType("-@");
	IElementType tUTILDE_OP = new RubyElementType("~@");

	IElementType tDOT = new RubyElementType(".");

	IElementType tCOLON3 = new RubyElementType("::3"); // ::Constant
	IElementType tCOLON2 = new RubyElementType("::"); // in A::B
	IElementType tCOLON = new RubyElementType(":");
	IElementType tSYMBEG = new RubyElementType(":"); // :symbol


	IElementType tSEMICOLON = new RubyElementType(";");
	IElementType tCOMMA = new RubyElementType(",");
	IElementType tTILDE = new RubyElementType("~");
	IElementType tSHELL = new RubyElementType("`");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Braces ///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tfLBRACK = new RubyElementType("f["); // func[
	IElementType tLBRACK = new RubyElementType("[");
	IElementType tRBRACK = new RubyElementType("]");

	IElementType tfLBRACE = new RubyElementType("f{"); // func{
	IElementType tLBRACE = new RubyElementType("{");
	IElementType tRBRACE = new RubyElementType("}");

	IElementType tfLPAREN = new RubyElementType("f("); // func(
	IElementType tLPAREN_ARG = new RubyElementType("a("); // command (
	IElementType tLPAREN = new RubyElementType("(");
	IElementType tRPAREN = new RubyElementType(")");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// keywords /////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType kALIAS = new RubyElementType("alias");
	IElementType kEND = new RubyElementType("end");
	IElementType kELSE = new RubyElementType("else");
	IElementType kCASE = new RubyElementType("case");
	IElementType kENSURE = new RubyElementType("ensure");
	IElementType kMODULE = new RubyElementType("module");
	IElementType kELSIF = new RubyElementType("elsif");
	IElementType kDEF = new RubyElementType("def");

	IElementType kRESCUE = new RubyElementType("rescue");
	IElementType kRESCUE_MOD = new RubyElementType("rescue modifier");

	IElementType kNOT = new RubyElementType("not");
	IElementType kTHEN = new RubyElementType("then");
	IElementType kYIELD = new RubyElementType("yield");
	IElementType kFOR = new RubyElementType("for");
	IElementType kSELF = new RubyElementType("self");
	IElementType kFALSE = new RubyElementType("false");
	IElementType kRETRY = new RubyElementType("retry");
	IElementType kRETURN = new RubyElementType("return");
	IElementType kTRUE = new RubyElementType("true");

	IElementType kIF = new RubyElementType("if");
	IElementType kIF_MOD = new RubyElementType("if modifier");

	IElementType kDEFINED = new RubyElementType("defined?");
	IElementType kSUPER = new RubyElementType("super");
	IElementType kUNDEF = new RubyElementType("undef");
	IElementType kBREAK = new RubyElementType("break");
	IElementType kIN = new RubyElementType("in");

	IElementType kDO = new RubyElementType("do");
	IElementType kDO_COND = new RubyElementType("do_cond");

	IElementType kNIL = new RubyElementType("nil");

	IElementType kUNTIL = new RubyElementType("until");
	IElementType kUNTIL_MOD = new RubyElementType("until modifier");

	IElementType kUNLESS = new RubyElementType("unless");
	IElementType kUNLESS_MOD = new RubyElementType("unless modifier");

	IElementType kOR = new RubyElementType("or");
	IElementType kNEXT = new RubyElementType("next");
	IElementType kWHEN = new RubyElementType("when");
	IElementType kREDO = new RubyElementType("redo");
	IElementType kAND = new RubyElementType("and");
	IElementType kBEGIN = new RubyElementType("begin");
	IElementType kCLASS = new RubyElementType("class");
	IElementType klEND = new RubyElementType("END");
	IElementType klBEGIN = new RubyElementType("BEGIN");

	IElementType kLINE = new RubyElementType("__LINE__");
	IElementType kFILE = new RubyElementType("__FILE__");

	IElementType kWHILE = new RubyElementType("while");
	IElementType kWHILE_MOD = new RubyElementType("while modifier");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// variables and constants //////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tIDENTIFIER = new RubyElementType("identifier");
	IElementType tCONSTANT = new RubyElementType("constant");

	IElementType tIVAR = new RubyElementType("instance variable");
	IElementType tGVAR = new RubyElementType("global variable");
	IElementType tCVAR = new RubyElementType("class variable");
	IElementType tFID = new RubyElementType("fid"); // name!
	IElementType tAID = new RubyElementType("aid"); // field=
	IElementType tNTH_REF = new RubyElementType("nth ref");
	IElementType tBACK_REF = new RubyElementType("back ref");


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// comments /////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType TLINE_COMMENT = new RubyElementType("line comment");
	IElementType tBLOCK_COMMENT_CONTENT = new RubyElementType("block comment content");
	IElementType tBLOCK_COMMENT_BEGIN = new RubyElementType("=begin");
	IElementType tBLOCK_COMMENT_END = new RubyElementType("=end");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////// others ///////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tEND_MARKER = new RubyElementType("__END__");
	IElementType tWHITE_SPACE = new RubyElementType("white space");
	IElementType tLINE_CONTINUATION = new RubyElementType("line continuation");
	IElementType tEOL = new RubyElementType("end of line");
	IElementType tEOF = new RubyElementType("end of file");

	IElementType tBAD_CHARACTER = new RubyElementType("bad character");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////// Ruby 1.9+ ////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	IElementType tLABEL = new RubyElementType("label"); // name:
	IElementType tLAMBDA = new RubyElementType("->");
	IElementType tANDDOT = new RubyElementType("&.");
	IElementType tDSTAR = new RubyElementType("s**"); // **arg

}