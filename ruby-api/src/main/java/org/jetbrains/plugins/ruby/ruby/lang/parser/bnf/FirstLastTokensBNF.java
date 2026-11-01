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

package org.jetbrains.plugins.ruby.ruby.lang.parser.bnf;

import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Oct 11, 2006
 */
public interface FirstLastTokensBNF extends TokenBNF
{

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// First tokens of nonterminals //////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tLITERAL_FIRST_TOKEN = TokenSet.orSet(tNUMBERS, TokenSet.create(RubyTokenTypes.tSYMBEG));


	final TokenSet tPRIMARY_FISRT_TOKEN = TokenSet.orSet(tLITERAL_FIRST_TOKEN, tVARIABLES, tREFS,

			// string like beginnings
			tSTRINGS_BEGINNINGS, tREGEXP_BEGINNINGS, tWORDS_BEGINNINGS,

			TokenSet.create(RubyTokenTypes.tHEREDOC_ID,

					// left brackets! Not fBraces!!!
					RubyTokenTypes.tLPAREN, RubyTokenTypes.tLPAREN_ARG, RubyTokenTypes.tLBRACK, RubyTokenTypes.tLBRACE,

					RubyTokenTypes.tCOLON3, RubyTokenTypes.tFID, RubyTokenTypes.tLAMBDA,
					// keyword section
					RubyTokenTypes.kBEGIN, RubyTokenTypes.kRETURN, RubyTokenTypes.kYIELD, RubyTokenTypes.kDEFINED, RubyTokenTypes.kIF, RubyTokenTypes.kUNLESS, RubyTokenTypes.kWHILE, RubyTokenTypes.kUNTIL, RubyTokenTypes.kCASE, RubyTokenTypes.kFOR, RubyTokenTypes.kCLASS, RubyTokenTypes.kMODULE, RubyTokenTypes.kDEF, RubyTokenTypes.kBREAK, RubyTokenTypes.kNEXT, RubyTokenTypes.kREDO, RubyTokenTypes.kRETRY));


	final TokenSet tARG_FIRST_TOKEN = TokenSet.orSet(tPRIMARY_FISRT_TOKEN, TokenSet.create(
			// plus, minus, tilde, exclamation
			RubyTokenTypes.tUPLUS, RubyTokenTypes.tUMINUS, RubyTokenTypes.tEXCLAMATION, RubyTokenTypes.tTILDE, RubyTokenTypes.kDEFINED,
			// beginless ranges
			RubyTokenTypes.tDOT2, RubyTokenTypes.tDOT3));


	final TokenSet tCOMMAND_FIRST_TOKENS = TokenSet.orSet(tPRIMARY_FISRT_TOKEN, TokenSet.create(RubyTokenTypes.kSUPER, RubyTokenTypes.kYIELD));

	final TokenSet tCOMMAND_CALL_FIRST_TOKENS = TokenSet.orSet(tCOMMAND_FIRST_TOKENS, TokenSet.create(RubyTokenTypes.kRETURN, RubyTokenTypes.kBREAK, RubyTokenTypes.kNEXT));

	final TokenSet tAREF_ARGS_FIRST_TOKENS = TokenSet.orSet(tARG_FIRST_TOKEN, tCOMMAND_FIRST_TOKENS, TokenSet.create(RubyTokenTypes.tSTAR, RubyTokenTypes.tDSTAR, RubyTokenTypes.tLABEL));

	final TokenSet tCALL_ARG_FIRST_TOKEN = TokenSet.orSet(TokenSet.create(RubyTokenTypes.tAMPER, RubyTokenTypes.tSTAR, RubyTokenTypes.tDSTAR, RubyTokenTypes.tLABEL, RubyTokenTypes.tDOT3), tARG_FIRST_TOKEN, tCOMMAND_FIRST_TOKENS);

	final TokenSet tEXPR_FIRST_TOKENS = TokenSet.orSet(tCOMMAND_CALL_FIRST_TOKENS, tARG_FIRST_TOKEN, TokenSet.create(RubyTokenTypes.kNOT, RubyTokenTypes.tEXCLAMATION));


	final TokenSet tLHS_FIRST_TOKEN = tPRIMARY_FISRT_TOKEN;

	final TokenSet tMLHS_FIRST_TOKEN = TokenSet.orSet(tLHS_FIRST_TOKEN, TokenSet.create(RubyTokenTypes.tSTAR, RubyTokenTypes.tLPAREN));


	final TokenSet tSTMT_FIRST_TOKENS = TokenSet.orSet(tHEREDOC_VALUE_BEGINNINGS,

			tEXPR_FIRST_TOKENS, tLHS_FIRST_TOKEN, tMLHS_FIRST_TOKEN, TokenSet.create(RubyTokenTypes.kALIAS, RubyTokenTypes.kUNDEF, RubyTokenTypes.klBEGIN, RubyTokenTypes.klEND));

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Last tokens of nonterminals ///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tPRIMARY_LAST_TOKEN = TokenSet.orSet(tVARIABLES, tREFS, tNUMBERS,


			TokenSet.create(
					// right brackets
					RubyTokenTypes.tRBRACK, RubyTokenTypes.tRPAREN, RubyTokenTypes.tRBRACE,

					// fid
					RubyTokenTypes.tFID,

					// string like ends
					RubyTokenTypes.tSTRING_END, RubyTokenTypes.tWORDS_END, RubyTokenTypes.tREGEXP_END, RubyTokenTypes.tHEREDOC_ID,

					// keyword section
					RubyTokenTypes.kEND, RubyTokenTypes.kBREAK, RubyTokenTypes.kNEXT, RubyTokenTypes.kREDO, RubyTokenTypes.kRETRY,

					RubyTokenTypes.kRETURN, RubyTokenTypes.kYIELD


			));
	final TokenSet EXPR_LAST_TOKEN = TokenSet.orSet(tHEREDOC_ENDS, tPRIMARY_LAST_TOKEN);

}
