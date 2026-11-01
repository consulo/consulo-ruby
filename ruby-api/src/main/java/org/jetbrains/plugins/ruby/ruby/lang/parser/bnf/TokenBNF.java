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

import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.RHTMLTokenTypeEx;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 05.09.2006
 */
public interface TokenBNF
{

	//TODO If you want to use RubyParser in your template(e.g. RHTML)
	//you should add here all your template's outers elements(or the injection closing element).
	final TokenSet tOUTER_ELEMENTS = TokenSet.create(RHTMLTokenTypeEx.RHTML_INJECTION_IN_RUBY);

	final TokenSet tSEMICOLONS = TokenSet.orSet(TokenSet.create(RubyTokenTypes.tSEMICOLON), tOUTER_ELEMENTS);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////// Tokens //////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet tSTRING_LIKE_CONTENTS = TokenSet.create(RubyTokenTypes.tSTRING_LIKE_CONTENT, RubyTokenTypes.T_STRING_CONTENT, RubyTokenTypes.tWORDS_CONTENT, RubyTokenTypes.tREGEXP_CONTENT, RubyTokenTypes.tHEREDOC_CONTENT);

	final TokenSet tESCAPE_SEQUENCES = TokenSet.create(RubyTokenTypes.tESCAPE_SEQUENCE, RubyTokenTypes.tINVALID_ESCAPE_SEQUENCE);

	final TokenSet tEXPR_SUBT_TOKENS = TokenSet.create(RubyTokenTypes.tSTRING_DBEG, RubyTokenTypes.tSTRING_DEND, RubyTokenTypes.tSTRING_DVAR);

	final TokenSet tREFS = TokenSet.create(RubyTokenTypes.tNTH_REF, RubyTokenTypes.tBACK_REF);

	final TokenSet tVARS = TokenSet.create(RubyTokenTypes.tIVAR, RubyTokenTypes.tGVAR, RubyTokenTypes.tCVAR);

	final TokenSet tSTRING_DVAR = TokenSet.orSet(tREFS, tVARS);

	//////////////////// STRINGS ///////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet tSTRINGS_BEGINNINGS = TokenSet.create(RubyTokenTypes.tDOUBLE_QUOTED_STRING_BEG, RubyTokenTypes.tSINGLE_QUOTED_STRING_BEG, RubyTokenTypes.tXSTRING_BEG);
	final TokenSet tSTRING_DELIMITERS = TokenSet.orSet(tSTRINGS_BEGINNINGS, TokenSet.create(RubyTokenTypes.tSTRING_END));
	final TokenSet tSTRING_TOKENS = TokenSet.orSet(tSTRING_DELIMITERS, tESCAPE_SEQUENCES, tEXPR_SUBT_TOKENS, TokenSet.create(RubyTokenTypes.tSTRING_LIKE_CONTENT, RubyTokenTypes.T_STRING_CONTENT));

	//////////////////// REGEXPS ///////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet tREGEXP_BEGINNINGS = TokenSet.create(RubyTokenTypes.tREGEXP_BEG);
	final TokenSet tREGEXP_DELIMITERS = TokenSet.orSet(tREGEXP_BEGINNINGS, TokenSet.create(RubyTokenTypes.tREGEXP_END));
	final TokenSet tREGEXP_TOKENS = TokenSet.orSet(tREGEXP_DELIMITERS, tESCAPE_SEQUENCES, tEXPR_SUBT_TOKENS, TokenSet.create(RubyTokenTypes.tSTRING_LIKE_CONTENT, RubyTokenTypes.tREGEXP_CONTENT));


	//////////////////// WORDS /////////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet tWORDS_BEGINNINGS = TokenSet.create(RubyTokenTypes.tWORDS_BEG, RubyTokenTypes.tQWORDS_BEG);
	final TokenSet tWORDS_DELIMITERS = TokenSet.orSet(tWORDS_BEGINNINGS, TokenSet.create(RubyTokenTypes.tWORDS_END));
	final TokenSet tWORDS_TOKENS = TokenSet.orSet(tWORDS_DELIMITERS, tESCAPE_SEQUENCES, tEXPR_SUBT_TOKENS, TokenSet.create(RubyTokenTypes.tSTRING_LIKE_CONTENT, RubyTokenTypes.tWORDS_CONTENT));


	//////////////////// HEREDOCS //////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet tHEREDOC_ENDS = TokenSet.create(RubyTokenTypes.tHEREDOC_END, RubyTokenTypes.tHEREDOC_INDENTED_END);

	final TokenSet tHEREDOC_ALL_IDS = TokenSet.orSet(TokenSet.create(RubyTokenTypes.tHEREDOC_ID), tHEREDOC_ENDS);
	final TokenSet tHEREDOC_VALUE_BEGINNINGS = TokenSet.orSet(TokenSet.create(RubyTokenTypes.tSTRING_LIKE_CONTENT, RubyTokenTypes.tHEREDOC_CONTENT, RubyTokenTypes.tSTRING_DBEG, RubyTokenTypes.tSTRING_DVAR), tHEREDOC_ENDS, tESCAPE_SEQUENCES);
	final TokenSet tHEREDOC_TOKENS = TokenSet.orSet(tEXPR_SUBT_TOKENS, tHEREDOC_VALUE_BEGINNINGS, tHEREDOC_ALL_IDS);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// Variables and so on ///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final TokenSet kPSEUDO_CONSTANTS = TokenSet.create(RubyTokenTypes.kFILE, RubyTokenTypes.kLINE, RubyTokenTypes.kNIL, RubyTokenTypes.kSELF, RubyTokenTypes.kSUPER, RubyTokenTypes.kTRUE, RubyTokenTypes.kFALSE);

	final TokenSet tCID = TokenSet.create(RubyTokenTypes.tIDENTIFIER, RubyTokenTypes.tCONSTANT);

	final TokenSet tF_NORMARGS = TokenSet.orSet(tCID, tVARS);

	final TokenSet tVARIABLES = TokenSet.orSet(kPSEUDO_CONSTANTS, tCID, tVARS);


	final TokenSet tNUMBERS = TokenSet.create(RubyTokenTypes.tINTEGER, RubyTokenTypes.tFLOAT);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// Other signs ///////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tCOLONS = TokenSet.create(RubyTokenTypes.tCOLON, RubyTokenTypes.tSYMBEG);

	// (
	final TokenSet tLPARENS = TokenSet.create(RubyTokenTypes.tLPAREN, RubyTokenTypes.tfLPAREN, RubyTokenTypes.tLPAREN_ARG);
	// [
	final TokenSet tLBRACKS = TokenSet.create(RubyTokenTypes.tLBRACK, RubyTokenTypes.tfLBRACK);
	// {
	final TokenSet tLBRACES = TokenSet.create(RubyTokenTypes.tLBRACE, RubyTokenTypes.tfLBRACE);

	final TokenSet tMINUSES = TokenSet.create(RubyTokenTypes.tMINUS, RubyTokenTypes.tUMINUS);

	final TokenSet tPLUSES = TokenSet.create(RubyTokenTypes.tPLUS, RubyTokenTypes.tUPLUS);

	final TokenSet tPIPES = TokenSet.create(RubyTokenTypes.tPIPE, RubyTokenTypes.tBIT_OR);

	final TokenSet tAMPERS = TokenSet.create(RubyTokenTypes.tAMPER, RubyTokenTypes.tBIT_AND);

	final TokenSet tSTARS = TokenSet.create(RubyTokenTypes.tSTAR, RubyTokenTypes.tMULT);

	final TokenSet tDOT_OR_COLON = TokenSet.create(RubyTokenTypes.tCOLON2, RubyTokenTypes.tDOT, RubyTokenTypes.tANDDOT);

	final TokenSet tCODE_BLOCK_BEG_TOKENS = TokenSet.orSet(TokenSet.create(RubyTokenTypes.kDO), tLBRACES);


	final TokenSet tWHITESPACES = TokenSet.create(RubyTokenTypes.tWHITE_SPACE, RubyTokenTypes.tLINE_CONTINUATION);

	final TokenSet tBLOCK_COMMENT_TOKENS = TokenSet.create(RubyTokenTypes.tBLOCK_COMMENT_CONTENT, RubyTokenTypes.tBLOCK_COMMENT_BEGIN, RubyTokenTypes.tBLOCK_COMMENT_END);

	final TokenSet tCOMMENTS = TokenSet.orSet(tBLOCK_COMMENT_TOKENS, TokenSet.create(RubyTokenTypes.TLINE_COMMENT, RubyTokenTypes.tEND_MARKER));

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// Operation signs ////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
	op		: '|'
            | '^'
            | '&'
            | tCMP
            | tASSGN
            | tEQ
            | tMATCH
            | '>'
            | tGEQ
            | '<'
            | tLEQ
            | tLSHFT
            | tRSHFT
            | '+'
            | '-'
            | '*'
            | tMULT
            | '/'
            | '%'
            | tPOW
            | '~'
            | tUPLUS
            | tUMINUS
            | tAREF
            | tASET
            | '`'
            ;
*/

	final TokenSet tOPS = TokenSet.orSet(tPIPES,                       // |
			tAMPERS,                      // &
			tPLUSES,                      // +
			tMINUSES,                     // -
			tSTARS,                       // *
			TokenSet.create(RubyTokenTypes.tXOR,              //  ^
					RubyTokenTypes.tCMP,              //  <=>
					RubyTokenTypes.tASSGN,            //  =
					RubyTokenTypes.tEQ,               //  ==
					RubyTokenTypes.tEQQ,              //  ===
					RubyTokenTypes.tMATCH,            //  ~=
					RubyTokenTypes.tGT,               //  >
					RubyTokenTypes.tGEQ,              //  >=
					RubyTokenTypes.tLT,               //  <
					RubyTokenTypes.tLEQ,              //  <=
					RubyTokenTypes.tLSHFT,            //  <<
					RubyTokenTypes.tRSHFT,            //  >>
					RubyTokenTypes.tUPLUS_OP,         //  +@
					RubyTokenTypes.tUMINUS_OP,        //  -@
					RubyTokenTypes.tUTILDE_OP,        //  ~@
					RubyTokenTypes.tDIV,              //    /
					RubyTokenTypes.tPERC,             //    %
					RubyTokenTypes.tPOW,              //    **
					RubyTokenTypes.tTILDE,            //    ~
					RubyTokenTypes.tAREF,             //    []
					RubyTokenTypes.tASET,             //    []=
					RubyTokenTypes.tSHELL,            //    `
					RubyTokenTypes.tNEQ,              //    !=
					RubyTokenTypes.tNMATCH,           //    !~
					RubyTokenTypes.tEXCLAMATION       //    !
			));

	final TokenSet tUNARY_OPS = TokenSet.create(RubyTokenTypes.tEXCLAMATION, RubyTokenTypes.kNOT, RubyTokenTypes.tUMINUS, RubyTokenTypes.tUPLUS, RubyTokenTypes.tTILDE);

	final TokenSet tOPERATION = TokenSet.orSet(tCID, TokenSet.create(RubyTokenTypes.tFID));

	final TokenSet tOPERATION2 = TokenSet.orSet(tOPERATION, tOPS);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// Assignment signs /////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	OP_ASGN		: `+=' | `-=' | `*=' | `/=' | `%=' | `**='
			| `&=' | `|=' | `^=' | `<<=' | `>>='
			| `&&=' | `||='
	*/
	final TokenSet tOP_ASGNS = TokenSet.create(RubyTokenTypes.tBIT_AND_OP_ASGN, RubyTokenTypes.tAND_OP_ASGN, RubyTokenTypes.tDIV_OP_ASGN, RubyTokenTypes.tLSHFT_OP_ASGN, RubyTokenTypes.tMINUS_OP_ASGN, RubyTokenTypes.tOR_OP_ASGN, RubyTokenTypes.tPERC_OP_ASGN, RubyTokenTypes.tBIT_OR_OP_ASGN, RubyTokenTypes.tPLUS_OP_ASGN, RubyTokenTypes.tPOW_OP_ASGN, RubyTokenTypes.tRSHFT_OP_ASGN, RubyTokenTypes.tMULT_OP_ASGN, RubyTokenTypes.tXOR_OP_ASGN);

	final TokenSet tASSGNS = TokenSet.orSet(tOP_ASGNS, TokenSet.create(RubyTokenTypes.tASSGN));

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// Matching tokens ///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tMATCH_HIGH_PRIORITY_OPS = TokenSet.create(RubyTokenTypes.tGT, RubyTokenTypes.tGEQ, RubyTokenTypes.tLT, RubyTokenTypes.tLEQ);

	final TokenSet tMATCH_LOW_PRIORITY_OPS = TokenSet.create(RubyTokenTypes.tCMP, RubyTokenTypes.tEQ, RubyTokenTypes.tEQQ, RubyTokenTypes.tNEQ, RubyTokenTypes.tMATCH, RubyTokenTypes.tNMATCH);

	final TokenSet tMATCH_OPS = TokenSet.orSet(tMATCH_HIGH_PRIORITY_OPS, tMATCH_LOW_PRIORITY_OPS);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// Range tokens //////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tRANGE_TOKENS = TokenSet.create(RubyTokenTypes.tDOT2, RubyTokenTypes.tDOT3);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final TokenSet tMATH_OPS = TokenSet.create(RubyTokenTypes.tPLUS, RubyTokenTypes.tMINUS, RubyTokenTypes.tMULT, RubyTokenTypes.tDIV, RubyTokenTypes.tPERC, RubyTokenTypes.tPOW);

	final TokenSet tSHIFT_OPS = TokenSet.create(RubyTokenTypes.tLSHFT, RubyTokenTypes.tRSHFT);

	final TokenSet tBIT_OR_OPS = TokenSet.create(RubyTokenTypes.tBIT_OR, RubyTokenTypes.tXOR);
	final TokenSet tBIT_OPS = TokenSet.orSet(tBIT_OR_OPS, TokenSet.create(RubyTokenTypes.tBIT_AND));

	final TokenSet tBOOL_OPS = TokenSet.create(RubyTokenTypes.tOR, RubyTokenTypes.tAND);

	final TokenSet kBOOL_OPS = TokenSet.create(RubyTokenTypes.kOR, RubyTokenTypes.kAND);

	final TokenSet tBINARY_OPS = TokenSet.orSet(tRANGE_TOKENS, tMATCH_OPS, tMATH_OPS, tSHIFT_OPS, tBIT_OPS, tBOOL_OPS, kBOOL_OPS);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// Reswords ///////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
        reswords	: k__LINE__ | k__FILE__  | klBEGIN | klEND
                | kALIAS | kAND | kBEGIN | kBREAK | kCASE | kCLASS | kDEF
                | kDEFINED | kDO | kELSE | kELSIF | kEND | kENSURE | kFALSE
                | kFOR | kIN | kMODULE | kNEXT | kNIL | kNOT
                | kOR | kREDO | kRESCUE | kRETRY | kRETURN | kSELF | kSUPER
                | kTHEN | kTRUE | kUNDEF | kWHEN | kYIELD
                | kIF_MOD | kUNLESS_MOD | kWHILE_MOD | kUNTIL_MOD | kRESCUE_MOD
                ;
    */

	final TokenSet kMOD_RESWORDS = TokenSet.create(RubyTokenTypes.kIF_MOD, RubyTokenTypes.kUNLESS_MOD, RubyTokenTypes.kWHILE_MOD, RubyTokenTypes.kUNTIL_MOD, RubyTokenTypes.kRESCUE_MOD);

	final TokenSet kRESWORDS = TokenSet.orSet(kPSEUDO_CONSTANTS, TokenSet.create(RubyTokenTypes.klBEGIN, RubyTokenTypes.klEND, RubyTokenTypes.kALIAS, RubyTokenTypes.kAND, RubyTokenTypes.kBEGIN, RubyTokenTypes.kBREAK, RubyTokenTypes.kCASE, RubyTokenTypes.kCLASS, RubyTokenTypes.kDEF, RubyTokenTypes.kDEFINED, RubyTokenTypes.kDO, RubyTokenTypes.kDO_COND, RubyTokenTypes.kELSE, RubyTokenTypes.kELSIF, RubyTokenTypes.kEND, RubyTokenTypes.kENSURE, RubyTokenTypes.kFOR, RubyTokenTypes.kIF, RubyTokenTypes.kIN, RubyTokenTypes.kMODULE, RubyTokenTypes.kNEXT, RubyTokenTypes.kNOT, RubyTokenTypes.kOR, RubyTokenTypes.kREDO, RubyTokenTypes.kRESCUE, RubyTokenTypes.kRETRY, RubyTokenTypes.kRETURN, RubyTokenTypes.kTHEN, RubyTokenTypes.kTRUE, RubyTokenTypes.kUNDEF, RubyTokenTypes.kUNTIL, RubyTokenTypes.kUNLESS, RubyTokenTypes.kWHEN, RubyTokenTypes.kWHILE, RubyTokenTypes.kYIELD));

	final TokenSet kALL_RESWORDS = TokenSet.orSet(kMOD_RESWORDS, kRESWORDS);

	// Used for JRuby support
	final TokenSet KALL_RESWORDS = kALL_RESWORDS;

	final TokenSet tTERM_TOKENS = TokenSet.orSet(TokenSet.create(RubyTokenTypes.tEOL), tSEMICOLONS);

	final TokenSet kCONDITIONAL = TokenSet.create(RubyTokenTypes.kIF, RubyTokenTypes.kTHEN, RubyTokenTypes.kELSE, RubyTokenTypes.kELSIF, RubyTokenTypes.kUNLESS, RubyTokenTypes.kUNTIL, RubyTokenTypes.kWHEN, RubyTokenTypes.kWHILE, RubyTokenTypes.kDO, RubyTokenTypes.kDO_COND);


	final TokenSet tWHITESPACES_OR_COMMENTS = TokenSet.orSet(tWHITESPACES, tCOMMENTS);

	final TokenSet tFNAME = TokenSet.orSet(BNF.kRESWORDS, BNF.tCID, BNF.tOPS, TokenSet.create(RubyTokenTypes.tFID, RubyTokenTypes.tAID));

	final TokenSet tEXPR_BEG_AFTER_TOKENS = TokenSet.orSet(tTERM_TOKENS,

			kCONDITIONAL,

			tCODE_BLOCK_BEG_TOKENS,

			kMOD_RESWORDS,

			tLPARENS, tLBRACKS, tLBRACES,

			tASSGNS, tRANGE_TOKENS, tMATCH_OPS, tSHIFT_OPS, tBIT_OPS, tBOOL_OPS, kBOOL_OPS, tUNARY_OPS,

			TokenSet.create(RubyTokenTypes.kCLASS, RubyTokenTypes.kMODULE,

					RubyTokenTypes.tCOMMA,

					RubyTokenTypes.tASSOC, RubyTokenTypes.tSTRING_DBEG,

					RubyTokenTypes.tQUESTION, RubyTokenTypes.tCOLON,

					RubyTokenTypes.kENSURE, RubyTokenTypes.kIN,

					RubyTokenTypes.tLABEL, RubyTokenTypes.tLAMBDA, RubyTokenTypes.tDSTAR,

					// prefix splat and block pass: *(a + b), &(proc)
					RubyTokenTypes.tSTAR, RubyTokenTypes.tAMPER));

	// WE interpret EOL as whiteSpace after these tokens
	public final TokenSet tIGNORE_EOL_TOKENS = TokenSet.orSet(tEXPR_BEG_AFTER_TOKENS, tBINARY_OPS, tDOT_OR_COLON);

	final TokenSet tCONTINUATION_INDENT = TokenSet.orSet(tBINARY_OPS, tASSGNS,

			kMOD_RESWORDS,

			tLPARENS, tLBRACKS, tLBRACES,

			tDOT_OR_COLON,

			TokenSet.create(RubyTokenTypes.tCOMMA,

					RubyTokenTypes.tASSOC, RubyTokenTypes.tSTRING_DBEG,

					RubyTokenTypes.tQUESTION,

					RubyTokenTypes.tCOMMA, RubyTokenTypes.tCOLON));

}
