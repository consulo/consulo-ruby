package org.jetbrains.plugins.ruby.ruby.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.FlexLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.managers.ContentManager;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.managers.HeredocsManager;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.managers.StatesManager;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.managers.TokensManager;

/* Auto generated File */
@SuppressWarnings({"AccessStaticViaInstance", "FieldCanBeLocal", "UnusedAssignment", "JavaDoc", "UnusedDeclaration", "SimplifiableIfStatement", "ConstantConditions"})
%%

%public
%class RubyRawLexer
%implements FlexLexer, RubyTokenTypes
%unicode
%public

%function advance
%type IElementType

%eof{ return;
%eof}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// USER CODE //////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%{
    /**
    * resets lexer to initialState
    * @param initialState initial state to set up.
    */
    protected void reset(int initialState){
        mySM.reset();
        myCM.reset(zzStartRead, zzEndRead);
        myTM.reset();
        myHM.reset();
        mySM.toState(initialState);
    }



// This is manager for Lexical states
        private StatesManager mySM = new StatesManager(this);

// This is manager to perform all reading operations;
        private ContentManager myCM = new ContentManager(this);

// This is manager to change process each Token;
        private TokensManager myTM = new TokensManager(this);

// This is manager for Heredocs
        private HeredocsManager myHM = new HeredocsManager(this);

    @NotNull
    public final CharSequence getBuffer(){
        return zzBuffer;
    }

    @NotNull
    public final ContentManager getContentManager(){
        return myCM;
    }

    @NotNull
    public final HeredocsManager getHeredocsManager(){
        return myHM;
    }

    @NotNull
    public final StatesManager getStatesManager(){
        return mySM;
    }

    @NotNull
    public final TokensManager getTokensManager(){
        return myTM;
    }

%}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////// REGEXPS DECLARATIONS //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// whitespaces and comments ////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
EOL =                               "\r" | "\n" | "\r\n"
ANY_CHAR =                          .|{EOL}

WHITE_SPACE_CHAR =                  [ \t\f\r\13]
WHITE_SPACE =                       {WHITE_SPACE_CHAR}+
NON_WHITE_SPACE_CHAR =              !((!.) |{WHITE_SPACE_CHAR})

LINE_CONTINUATION =                 "\\"{EOL}
LINE =                              .*
LINE_COMMENT =                      "#"{LINE}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////// integers /////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
DIGIT =                             [0-9]
DIGITS =                            {DIGIT} | {DIGIT}"_"{DIGIT}
DECIMAL_INTEGER_LITERAL =           {DIGITS}+

OCTAL_DIGIT =                       [0-7]
OCTAL_DIGITS =                      {OCTAL_DIGIT} | {OCTAL_DIGIT}"_"{OCTAL_DIGIT}
OCTAL_INTEGER_LITERAL =             [0-7]{OCTAL_DIGITS}* | "0"[Oo]{OCTAL_DIGITS}+

BINARY_DIGIT =                      [0-1]
BINARY_DIGITS =                     {BINARY_DIGIT}|{BINARY_DIGIT}"_"{BINARY_DIGIT}
BINARY_INTEGER_LITERAL =            "0"[Bb]{BINARY_DIGITS}*

HEX_DIGIT =                         [0-9A-Fa-f]
HEX_DIGITS =                        {HEX_DIGIT}|{HEX_DIGIT}"_"{HEX_DIGIT}
HEX_INTEGER_LITERAL =               "0"[Xx]{HEX_DIGITS}*


SINGLE_CHAR =                       {ESCAPE_SEQUENCE} | {NON_WHITE_SPACE_CHAR}

ASCII_CODE_LITERAL =                "?"{SINGLE_CHAR} | "?\\\\"
CONTROL_LITERAL =                   "?\\C-"{SINGLE_CHAR}
META_LITERAL =                      "?\\M-"{SINGLE_CHAR}
META_CONTROL_LITERAL =              "?\\M-\\C-"{SINGLE_CHAR}
QBEGIN_INTEGER_LITERAL =            {ASCII_CODE_LITERAL} |
                                    {CONTROL_LITERAL} |
                                    {META_LITERAL} |
                                    {META_CONTROL_LITERAL}


INTEGER_LITERAL =                   {DECIMAL_INTEGER_LITERAL} |
                                    {HEX_INTEGER_LITERAL} |
                                    {OCTAL_INTEGER_LITERAL} |
                                    {BINARY_INTEGER_LITERAL} |
                                    {QBEGIN_INTEGER_LITERAL}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////// floating point ////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
FLOATING_POINT_LITERAL1 =       {DIGITS}*"."{DIGITS}+{EXPONENT_PART}?
FLOATING_POINT_LITERAL2 =       {DIGITS}+{EXPONENT_PART}
EXPONENT_PART =                 [Ee][+-]?{DIGITS}+

FLOAT_LITERAL =                 {FLOATING_POINT_LITERAL1} |
                                {FLOATING_POINT_LITERAL2}

NUMBER =                        {INTEGER_LITERAL} | {FLOAT_LITERAL}

NUMBER_SUFFIX =                 "r" | "i" | "ri"

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////// varibles ///////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
IDENT_CHAR =                    [a-zA-Z0-9_]
IDENT_LEADER =                  [a-z_]
CONST_LEADER =                  [A-Z]

IDENTIFIER =                    {IDENT_LEADER}{IDENT_CHAR}*
CONSTANT =                      ({CONST_LEADER}{IDENT_CHAR}*) | \u0416\u041E\u041F\u0410
CID =                           {IDENTIFIER} | {CONSTANT}

IVAR =                          "@"{CID}
CVAR =                          "@@"{CID}

/*
$*  argv
$$  pid
$?  last status
$!  error string
$@  error position
$/  input record separator
$\  output record separator
$;  field separator
$,  output field separator
$.  last read line number
$=  ignorecase
$:  load path
$<  reading filename
$>  default output handle 
$"  already loaded files

$-0        The alias to $/.
$-a        True if option -a is set. Read-only variable.
$-d        The alias to $DEBUG.
$-F        The alias to $;.
$-i        In in-place-edit mode, this variable holds the extension, otherwise nil.
$-I        The alias to $:.
$-K        Sets the multibyte coding system for strings and regular expressions.
$-l        True if option -l is set. Read-only variable.
$-p        True if option -p is set. Read-only variable.
$-v        The alias to $VERBOSE.
$-w        True if option -w is set.
*/

GVAR =                          "$"{CID} | "$*" | "$$" | "$?" | "$!" | "$@" |
                                "$/" | "$\\" | "$;" | "$," | "$." | "$=" | "$:" |
                                "$<" | "$>" | "$\"" | "$-" |
                                "$-" [a-zA-Z0-9_]


/*
$~  match-data
$&  last match
$`  string before last match
$'  string after last match
$+  string matches last paren.
*/
BACK_REF =                      "$~" | "$&" | "$`" | "$'" | "$+"

NTH_REF =                       "$"{DIGIT}+

FID =                           {CID}("?" | "!")
AID =                           {CID}"="

VARIABLE =                      {CID} | {GVAR} | {CVAR} | {IVAR} | {BACK_REF} | {NTH_REF}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////// strings /////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
DELIMITER_CHAR =                [^ _a-zA-Z0-9] | {EOL}

// \a Bell/alert (0x07)
// \b Backspace (0x08)
// \e Escape (0x1b)
// \f Formfeed (0x0c)
// \n Newline (0x0a)
// \r Return (0x0d)
// \s Space (0x20)
// \t Tab (0x09)
// \v Vertical tab (0x0b)
// \nnn  Octal nnn
// \xnn  Hex nn
// \cx  Control-x
// \C-x  Control-x
// \M-x  Meta-x
// \M-\C-x  Meta-control-x
OCTAL_NNN = "\\"({OCTAL_DIGIT}{1,3})
HEX_NN = "\\x"({HEX_DIGIT}{1,2})
CONTROL_X = "\\"("c" | "C-"){NON_WHITE_SPACE_CHAR}
META_X = "\\M-"{NON_WHITE_SPACE_CHAR}
META_CONTROL_X = "\\M-C-"{NON_WHITE_SPACE_CHAR}

ESCAPE_SEQUENCE =           "\\a" | "\\b" | "\\e" | "\\f" | "\\n" | "\\r" | "\\s" | "\\t" | "\\v" |
                            {OCTAL_NNN} | {HEX_NN} | {CONTROL_X} | {META_X} | {META_CONTROL_X}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////// others //////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
HEREDOC_ID =                [-~]? ( {CID} | "\"" [^\\\"]+ "\""  | "'" [^\\']+ "'" )
HEREDOC =                   "<<" {HEREDOC_ID}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// STATES DECLARATIONS //////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////// INCLUSIVE STATES /////////////////////////////////////////////////////////////////////////

%state SPECIAL_STATE
// Special state, when BaseComponent.isSpecial() is true we are in this state

%state IN_EXPR_SUBT_STATE
// After tSTRING_DBEG in string like state with expressions allowed.


///////////////////////////// EXCLUSIVE STATES /////////////////////////////////////////////////////////////////////////

%xstate IN_VAR_SUBT_STATE
// After tSTRING_DVAR in string like state with expressions allowed.

%xstate IN_BLOCK_COMMENT_STATE
// In block comment. To previous state:  "=end"

//// IN STRING LIKE STATES /////////////////////////////////////////////////////////////////////////////////////////////
%xstate IN_WORDS_STATE
// After tWORDS_BEG.    To previous state : tWORDS_END   Escape sequence: no,  Expressions: yes

%xstate IN_NI_WORDS_STATE
// After tNI_WORDS_BEG. To previous state : tWORDS_END   Escape sequence: no,  Expressions: no

%xstate IN_REGEXP_STATE
// After tREGEXP_BEG.   To previous state : tREGEXP_END  Escape sequence: no,  Expressions: yes

%xstate IN_STRING_STATE
// After tSTRING_BEG.   To previous state : tSTRING_END  Escape sequence: yes, Expressions: yes

%xstate IN_NI_STRING_STATE
// After tREGEXP_BEG.   To previous state : tREGEXP_END  Escape sequence: no,  Expressions: no

%xstate IN_HEREDOC_STATE
// After tEOL in AFTER_HEREDOC_STATE. To prevoius state: tHEREDOC_END; Escape sequence: no,  Expressions: yes

%%
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////// RULES declarations ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////// EXCLUSIVE STATES //////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_BLOCK_COMMENT_STATE ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_BLOCK_COMMENT_STATE>{
^"=end" / ({LINE} {EOL}?)               {   mySM.toPreviousState();
                                            return myTM.process(tBLOCK_COMMENT_END);
                                        }
{LINE} {EOL}?                           {   return myTM.process(tBLOCK_COMMENT_CONTENT); }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////// IN_HEREDOC_STATE, IN_STRING_STATE, IN_REGEXP_STATE, IN_WORDS_STATE ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

<IN_HEREDOC_STATE, IN_STRING_STATE, IN_REGEXP_STATE, IN_WORDS_STATE>{
"#{"                                    {   mySM.toExprSubtState(IN_EXPR_SUBT_STATE);
                                            return myTM.process(tSTRING_DBEG);
                                        }

"#" / ({GVAR} | {IVAR} | {CVAR} | {BACK_REF} | {NTH_REF})
                                        {   mySM.toVarSubtState(IN_VAR_SUBT_STATE);
                                            return myTM.process(tSTRING_DVAR);
                                        }
// pseudo escape sequence
"\\#"                                   {   return myTM.process(tSTRING_LIKE_CONTENT); }
// pseudo DVAR subtitution
"#@" | "#@@" | "#$"                     {   return myTM.process(tSTRING_LIKE_CONTENT); }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_STRING_STATE //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_STRING_STATE> {
{ANY_CHAR}                              {   // IN_STRING_STATE
                                            char beginDelimiter = mySM.getBeginDelimiter();
                                            char endDelimiter = mySM.getEndDelimiter();
                                            int l = myCM.eatExprEsc(beginDelimiter, endDelimiter);
// end
                                            if (l==ContentManager.END_SEEN){
                                                mySM.toPreviousState();
                                                return myTM.process(tSTRING_END);
                                            }
// simple escape
                                            if (l==ContentManager.SIMPLE_ESCAPE_SEEN){
                                                zzMarkedPos+=1;
                                                return myTM.process(tESCAPE_SEQUENCE);
                                            }
// backslash
                                            if (l==ContentManager.BACKSLASH_SEEN){
                                                return myTM.process(tINVALID_ESCAPE_SEQUENCE);
                                            }
// content
                                            assert(l>0);
                                            zzMarkedPos+=l-1;
                                            return myTM.process(T_STRING_CONTENT);
                                        }
{ESCAPE_SEQUENCE}                       {   return myTM.process(tESCAPE_SEQUENCE); }
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_NI_STRING_STATE ///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_NI_STRING_STATE> {
{ANY_CHAR}                              {   // IN_NI_STRING_STATE
                                            char beginDelimiter = mySM.getBeginDelimiter();
                                            char endDelimiter = mySM.getEndDelimiter();
                                            int l = myCM.eatNoExprNoEsc(beginDelimiter, endDelimiter);
// end
                                            if (l==ContentManager.END_SEEN){
                                                mySM.toPreviousState();
                                                return myTM.process(tSTRING_END);
                                            }
// simple escape
                                            if (l==ContentManager.SIMPLE_ESCAPE_SEEN){
                                                zzMarkedPos+=1;
                                                return myTM.process(tESCAPE_SEQUENCE);
                                            }
// content
                                            assert(l>0);
                                            zzMarkedPos+=l-1;
                                            return myTM.process(T_STRING_CONTENT);
                                        }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_REGEXP_STATE //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_REGEXP_STATE> {
{ANY_CHAR}                              {   // IN_REGEXP_STATE
                                            char beginDelimiter = mySM.getBeginDelimiter();
                                            char endDelimiter = mySM.getEndDelimiter();
                                            int l = myCM.eatExprNoEsc(beginDelimiter, endDelimiter);
// end
                                            if (l==ContentManager.END_SEEN){
                                               int modLength = 1;
                                               while (TextUtil.isRegexpModifier(myCM.safeReadAt(modLength))){
                                                   zzMarkedPos++;
                                                   modLength++;
                                               }
                                               mySM.toPreviousState();
                                               return myTM.process(tREGEXP_END);
                                            }
// simple escape
                                            if (l==ContentManager.SIMPLE_ESCAPE_SEEN){
                                                zzMarkedPos+=1;
                                                return myTM.process(tESCAPE_SEQUENCE);
                                            }
// content
                                            assert (l>0);
                                            zzMarkedPos+=l-1;
                                            return myTM.process(tREGEXP_CONTENT);
                                        }
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////// IN_WORDS_STATE ///////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_WORDS_STATE> {
{ANY_CHAR}                              {   // IN_WORDS_STATE
                                            char beginDelimiter = mySM.getBeginDelimiter();
                                            char endDelimiter = mySM.getEndDelimiter();
                                            int l = myCM.eatExprNoEsc(beginDelimiter, endDelimiter);
// end
                                            if (l==ContentManager.END_SEEN){
                                                mySM.toPreviousState();
                                                return myTM.process(tWORDS_END);
                                            }
// simple escape
                                            if (l==ContentManager.SIMPLE_ESCAPE_SEEN){
                                                zzMarkedPos+=1;
                                                return myTM.process(tESCAPE_SEQUENCE);
                                            }
// content
                                            assert (l>0);
                                            zzMarkedPos+=l-1;
                                            return myTM.process(tWORDS_CONTENT);
                                        }
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_NI_WORDS_STATE ////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_NI_WORDS_STATE> {
{ANY_CHAR}                              {   // IN_NI_WORDS_STATE
                                            char beginDelimiter = mySM.getBeginDelimiter();
                                            char endDelimiter = mySM.getEndDelimiter();
                                            int l = myCM.eatNoExprNoEsc(beginDelimiter, endDelimiter);
// end
                                            if (l==ContentManager.END_SEEN){
                                                mySM.toPreviousState();
                                                return myTM.process(tWORDS_END);
                                            }
// simple escape
                                            if (l==ContentManager.SIMPLE_ESCAPE_SEEN){
                                                zzMarkedPos+=1;
                                                return myTM.process(tESCAPE_SEQUENCE);
                                            }
// content
                                            assert (l>0);
                                            zzMarkedPos+=l-1;
                                            return myTM.process(tWORDS_CONTENT);
                                        }
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_HEREDOC_STATE /////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_HEREDOC_STATE> {

{ANY_CHAR}                             {   // IN_HEREDOC_STATE
                                            int l = ContentManager.END_SEEN;
                                            if (!myHM.isEndSeen()){
                                                l = myHM.eatHereDocContent();
                                                if (l>0){
// content
                                                    zzMarkedPos+=l-1;
                                                    return myTM.process(tHEREDOC_CONTENT);
                                                }
                                            }
                                            assert l == ContentManager.END_SEEN;
// end
                                            if (myHM.isEndSeen()){
// eol
                                                if (TextUtil.isEol(myCM.safeReadAt(0))){
                                                    return myTM.process(tEOL);
                                                }
// whitespace
                                                int number = 0;
                                                while (TextUtil.isWhiteSpace(myCM.safeReadAt(number))){
                                                    number++;
                                                }
                                                if (number>0){
                                                    zzMarkedPos+=number-1;
                                                    return myTM.process(tWHITE_SPACE);
                                                }
// heredoc_end
                                                zzMarkedPos+=myHM.getIdLength()-1;
                                                boolean indented = myHM.isIndented();
                                                myHM.poll();
                                                mySM.toPreviousState();
                                                mySM.setAfterHeredoc(myHM.size()>0);
                                                return myTM.process(indented ? tHEREDOC_INDENTED_END : tHEREDOC_END);
                                            }
                                        }
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// IN_VAR_SUBT_STATE ////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_VAR_SUBT_STATE>{
{GVAR}                                  {   mySM.toPreviousState();  return myTM.process(tGVAR); }
{IVAR}                                  {   mySM.toPreviousState();  return myTM.process(tIVAR); }
{CVAR}                                  {   mySM.toPreviousState();  return myTM.process(tCVAR); }
{BACK_REF}                              {   mySM.toPreviousState();  return myTM.process(tBACK_REF); }
{NTH_REF}                               {   mySM.toPreviousState();  return myTM.process(tNTH_REF); }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////// INCLUSIVE STATES //////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// IN_EXPR_SUBT_STATE //////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<IN_EXPR_SUBT_STATE>{
"{"                                     {   mySM.processOpenBrace();
                                            return myTM.process(myTM.isFTokenAllowed() ? tfLBRACE :  tLBRACE);
                                        }
"}"                                     {   mySM.processCloseBrace();
                                            if (mySM.wasEndBraceSeen()){
                                                mySM.toPreviousState();
                                                return myTM.process(tSTRING_DEND);
                                            } else {
                                                return myTM.process(tRBRACE);
                                            }
                                        }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// ALL INCLUSIVE STATES DEFAULTS //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

"__END__" {ANY_CHAR}*                   {   return myTM.process(tEND_MARKER); }

^"=begin"                               {   mySM.toState(IN_BLOCK_COMMENT_STATE);
                                            return myTM.process(tBLOCK_COMMENT_BEGIN);
                                        }
{LINE_COMMENT}                          {   return myTM.process(TLINE_COMMENT); }

{HEREDOC}                               {   if (myTM.isHeredocAllowed()){
                                                myHM.registerHeredoc(yytext().toString());
                                                return myTM.process(tHEREDOC_ID);
                                            }
                                            zzMarkedPos = getTokenStart()+2;
                                            return myTM.process(tLSHFT);
                                        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// keywords /////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"class"                                 {   if (myTM.reswordAllowed()){
                                                return myTM.process(kCLASS);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"if"                                    {   if (myTM.reswordAllowed()){
                                                return myTM.process(myTM.isExprEnd() ? kIF_MOD : kIF);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"rescue"                                {   if (myTM.reswordAllowed()){
                                                return myTM.process(myTM.isExprEnd() ? kRESCUE_MOD : kRESCUE);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"unless"                                {   if (myTM.reswordAllowed()){
                                                return myTM.process(myTM.isExprEnd() ? kUNLESS_MOD : kUNLESS);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"until"                                 {   if (myTM.reswordAllowed()){
                                                return myTM.process(myTM.isExprEnd() ? kUNTIL_MOD : kUNTIL);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"while"                                 {   if (myTM.reswordAllowed()){
                                                return myTM.process(myTM.isExprEnd() ? kWHILE_MOD : kWHILE);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }
"BEGIN"                                 {   return myTM.process(myTM.reswordAllowed() ? klBEGIN : tCONSTANT); }
"END"                                   {   return myTM.process(myTM.reswordAllowed() ? klEND : tCONSTANT); }

"alias"                                 {   return myTM.process(myTM.reswordAllowed() ? kALIAS : tIDENTIFIER); }
"and"                                   {   return myTM.process(myTM.reswordAllowed() ? kAND : tIDENTIFIER); }
"begin"                                 {   return myTM.process(myTM.reswordAllowed() ? kBEGIN : tIDENTIFIER); }
"break"                                 {   return myTM.process(myTM.reswordAllowed() ? kBREAK : tIDENTIFIER); }
"case"                                  {   return myTM.process(myTM.reswordAllowed() ? kCASE : tIDENTIFIER); }
"def"                                   {   return myTM.process(myTM.reswordAllowed() ? kDEF : tIDENTIFIER); }
"defined?"                              {   return myTM.process(myTM.reswordAllowed() ? kDEFINED : tIDENTIFIER); }
"else"                                  {   return myTM.process(myTM.reswordAllowed() ? kELSE : tIDENTIFIER); }
"elsif"                                 {   return myTM.process(myTM.reswordAllowed() ? kELSIF : tIDENTIFIER); }
"end"                                   {   return myTM.process(myTM.reswordAllowed() ? kEND : tIDENTIFIER); }
"ensure"                                {   return myTM.process(myTM.reswordAllowed() ? kENSURE : tIDENTIFIER); }
"for"                                   {   return myTM.process(myTM.reswordAllowed() ? kFOR : tIDENTIFIER); }
"in"                                    {   return myTM.process(myTM.reswordAllowed() ? kIN : tIDENTIFIER); }
"module"                                {   return myTM.process(myTM.reswordAllowed() ? kMODULE : tIDENTIFIER); }
"next"                                  {   return myTM.process(myTM.reswordAllowed() ? kNEXT : tIDENTIFIER); }
"not"                                   {   return myTM.process(myTM.reswordAllowed() ? kNOT : tIDENTIFIER); }
"or"                                    {   return myTM.process(myTM.reswordAllowed() ? kOR : tIDENTIFIER); }
"redo"                                  {   return myTM.process(myTM.reswordAllowed() ? kREDO : tIDENTIFIER); }
"retry"                                 {   return myTM.process(myTM.reswordAllowed() ? kRETRY : tIDENTIFIER); }
"return"                                {   return myTM.process(myTM.reswordAllowed() ? kRETURN : tIDENTIFIER); }
"then"                                  {   return myTM.process(myTM.reswordAllowed() ? kTHEN : tIDENTIFIER); }
"undef"                                 {   return myTM.process(myTM.reswordAllowed() ? kUNDEF : tIDENTIFIER); }
"when"                                  {   return myTM.process(myTM.reswordAllowed() ? kWHEN : tIDENTIFIER); }
"yield"                                 {   return myTM.process(myTM.reswordAllowed() ? kYIELD : tIDENTIFIER); }

"do"                                    {   if (myTM.reswordAllowed()){
                                                return myTM.process(mySM.isDoCondExpected() ? kDO_COND : kDO);
                                            }
                                            return myTM.process(tIDENTIFIER);
                                        }

"__FILE__"                              {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kFILE); }
"__LINE__"                              {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kLINE); }
"true"                                  {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kTRUE); }
"false"                                 {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kFALSE); }
"super"                                 {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kSUPER); }
"nil"                                   {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kNIL); }
"self"                                  {   return myTM.process(myTM.isMethodNameAfterDot() ? tIDENTIFIER : kSELF); }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// braces ///////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"("                                     {  if (myTM.isFTokenAllowed()){
                                            return myTM.process(tfLPAREN);
                                           }
                                           if (myTM.isArgTokenAllowed()){
                                            return myTM.process(tLPAREN_ARG);
                                           }
                                           return myTM.process(tLPAREN);
                                        }
"["                                     {  return myTM.process(myTM.isFTokenAllowed() ? tfLBRACK :  tLBRACK); }
"{"                                     {  return myTM.process(myTM.isFTokenAllowed() ? tfLBRACE :  tLBRACE); }

")"                                     {   return myTM.process(tRPAREN); }
"]"                                     {   return myTM.process(tRBRACK); }
"}"                                     {   return myTM.process(tRBRACE); }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// Variables  /////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

({IDENTIFIER} | {CONSTANT} | {FID}) ":" / [^:]
                                        {   if (myTM.isLabelAllowed()){
                                                return myTM.process(tLABEL);
                                            }
                                            yypushback(1);
                                            return myTM.process(myTM.getWordType(yytext()));
                                        }
{FID}                                   {   return myTM.process(tFID);  }

{IDENTIFIER}                            {   return myTM.process(tIDENTIFIER); }
{IDENTIFIER} / "=~" | "==" | "=>"       {   return myTM.process(tIDENTIFIER); }
{IDENTIFIER} / "!="                     {   return myTM.process(tIDENTIFIER); }
{IDENTIFIER} "="                        {   if (myTM.isAssignOpAllowed()){
                                                return myTM.process(tAID);
                                            }
                                            yypushback(1);
                                            return myTM.process(tIDENTIFIER);
                                        }

{CONSTANT}                              {   return myTM.process(tCONSTANT); }
{CONSTANT} / "=~" | "==" | "=>"         {   return myTM.process(tCONSTANT); }
{CONSTANT} / "!="                       {   return myTM.process(tCONSTANT); }
{CONSTANT} "="                          {   if (myTM.isAssignOpAllowed()){
                                                return myTM.process(tAID);
                                            }
                                            yypushback(1);
                                            return myTM.process(tCONSTANT);
                                        }

{GVAR}                                  {   return myTM.process(tGVAR); }
{IVAR}                                  {   return myTM.process(tIVAR); }
{CVAR}                                  {   return myTM.process(tCVAR); }

{NTH_REF}                               {   return myTM.process(tNTH_REF); }
{BACK_REF}                              {   return myTM.process(tBACK_REF); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Literals ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{INTEGER_LITERAL}                       {   return myTM.process(tINTEGER); }
{FLOAT_LITERAL}                         {   return myTM.process(tFLOAT); }
({DECIMAL_INTEGER_LITERAL} | {HEX_INTEGER_LITERAL} | {OCTAL_INTEGER_LITERAL} | {BINARY_INTEGER_LITERAL}) {NUMBER_SUFFIX} / [^a-zA-Z0-9_]
                                        {   return myTM.process(tINTEGER); }
{FLOAT_LITERAL} {NUMBER_SUFFIX} / [^a-zA-Z0-9_]
                                        {   return myTM.process(tFLOAT); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Strings /////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"\""                                    {   mySM.toStringLikeState(IN_STRING_STATE, '"');
                                            return myTM.process(tDOUBLE_QUOTED_STRING_BEG);
                                        }
"\'"                                    {   mySM.toStringLikeState(IN_NI_STRING_STATE, '\'');
                                            return myTM.process(tSINGLE_QUOTED_STRING_BEG);
                                        }
"`"                                     {   if (!myTM.isFnameAllowed()){
                                                mySM.toStringLikeState(IN_STRING_STATE, '`');
                                                return myTM.process(tXSTRING_BEG);
                                            }
                                            return myTM.process(tSHELL);
                                        }

"%Q"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_STRING_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tDOUBLE_QUOTED_STRING_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%q"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_NI_STRING_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tSINGLE_QUOTED_STRING_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%x"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_STRING_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tXSTRING_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%I"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_WORDS_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tQWORDS_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%i"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_NI_WORDS_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tWORDS_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%s"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_NI_STRING_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tSINGLE_QUOTED_STRING_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%W"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_WORDS_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tQWORDS_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%w"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_NI_WORDS_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tWORDS_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }
"%r"{DELIMITER_CHAR}                    {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_REGEXP_STATE, myCM.safeReadAt(2));
                                                return myTM.process(tREGEXP_BEG);
                                            }
                                            yypushback(2);
                                            return myTM.process(tPERC);
                                        }


"%="                                    {   if (myTM.isExprEnd()){
                                                return myTM.process(tPERC_OP_ASGN);
                                            }
                                            if (myTM.stringAllowed(false)){
                                                mySM.toStringLikeState(IN_STRING_STATE, '=');
                                                return myTM.process(tDOUBLE_QUOTED_STRING_BEG);
                                            }
                                            yypushback(1);
                                            return myTM.process(tPERC);
                                        }

"%"{DELIMITER_CHAR}                     {   if (myTM.stringAllowed()){
                                                mySM.toStringLikeState(IN_STRING_STATE, myCM.safeReadAt(1));
                                                return myTM.process(tDOUBLE_QUOTED_STRING_BEG);
                                            }
                                            yypushback(1);
                                            return myTM.process(tPERC);
                                        }

"/="                                    {   if (myTM.isExprEnd()){
                                                return myTM.process(tDIV_OP_ASGN);
                                            }
                                            if (myTM.stringAllowed(false)){
                                                yypushback(1);
                                                mySM.toStringLikeState(IN_REGEXP_STATE, '/');
                                                return myTM.process(tREGEXP_BEG);
                                            }
                                            yypushback(1);
                                            return myTM.process(tDIV);
                                        }

"/"                                     {   if (myTM.stringAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(1)))){
                                                mySM.toStringLikeState(IN_REGEXP_STATE, '/');
                                                return myTM.process(tREGEXP_BEG);
                                            }
                                            return myTM.process(tDIV);
                                        }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Bit operators ///////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"&."                                    {   if (myTM.isExprEnd()){
                                                return myTM.process(tANDDOT);
                                            }
                                            yypushback(1);
                                            return myTM.process(myTM.unaryAllowed(false) ? tAMPER : tBIT_AND);
                                        }
"&"                                     {   return myTM.process(myTM.unaryAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(1))) ? tAMPER : tBIT_AND); }
"|"                                     {   return myTM.process(myTM.isExprEnd() ? tBIT_OR : tPIPE); }
"^"                                     {   return myTM.process(tXOR); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Logical operators ///////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"!"                                     {   return myTM.process(tEXCLAMATION); }
"||"                                    {   if (myTM.isExprEnd()){
                                                return myTM.process(tOR);
                                            }
                                            yypushback(1);
                                            return myTM.process(tPIPE);
                                        }
"&&"                                    {   return myTM.process(tAND); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Comparison operators ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"<=>"                                   {   return myTM.process(tCMP); }
"=="                                    {   return myTM.process(tEQ); }
"!="                                    {   return myTM.process(tNEQ); }
"==="                                   {   return myTM.process(tEQQ); }
"=~"                                    {   return myTM.process(tMATCH); }
"!~"                                    {   return myTM.process(tNMATCH); }

">"                                     {   return myTM.process(tGT); }
">="                                    {   return myTM.process(tGEQ); }
"<"                                     {   return myTM.process(tLT); }
"<="                                    {   return myTM.process(tLEQ); }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Shift operators /////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"<<"                                    {   return myTM.process(tLSHFT); }
">>"                                    {   return myTM.process(tRSHFT); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////// Assignment operators ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"="                                     {   return myTM.process(tASSGN); }
"**="                                   {   return myTM.process(tPOW_OP_ASGN); }
"*="                                    {   return myTM.process(tMULT_OP_ASGN); }
"/="                                    {   return myTM.process(tDIV_OP_ASGN); }
"%="                                    {   return myTM.process(tPERC_OP_ASGN); }
"+="                                    {   return myTM.process(tPLUS_OP_ASGN); }
"-="                                    {   return myTM.process(tMINUS_OP_ASGN); }
"|="                                    {   return myTM.process(tBIT_OR_OP_ASGN); }
"&="                                    {   return myTM.process(tBIT_AND_OP_ASGN); }
"^="                                    {   return myTM.process(tXOR_OP_ASGN); }
"<<="                                   {   return myTM.process(tLSHFT_OP_ASGN); }
">>="                                   {   return myTM.process(tRSHFT_OP_ASGN); }
"&&="                                   {   return myTM.process(tAND_OP_ASGN); }
"||="                                   {   return myTM.process(tOR_OP_ASGN); }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
"+@"                                    {   if (myTM.isFnameAllowed()){
                                                return myTM.process(tUPLUS_OP);
                                            }

                                            yypushback(1);
                                            return myTM.process(myTM.unaryAllowed(false) ? tUPLUS : tPLUS);
                                        }
"-@"                                    {   if (myTM.isFnameAllowed()){
                                                return myTM.process(tUMINUS_OP);
                                            }

                                            yypushback(1);
                                            return myTM.process(myTM.unaryAllowed(false) ? tUMINUS : tMINUS);
                                        }
"~@"                                    {   if (myTM.isFnameAllowed()){
                                                return myTM.process(tUTILDE_OP);
                                            }

                                            yypushback(1);
                                            return myTM.process(tTILDE);
                                        }

"+"                                     {   return myTM.process(myTM.unaryAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(1))) ? tUPLUS : tPLUS); }
"->"                                    {   return myTM.process(tLAMBDA); }
"-"                                     {   return myTM.process(myTM.unaryAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(1))) ? tUMINUS : tMINUS); }


"*"                                     {   return myTM.process(myTM.unaryAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(1))) ? tSTAR : tMULT); }
"/"                                     {   return myTM.process(tDIV); }
"%"                                     {   return myTM.process(tPERC); }


"**"                                    {   return myTM.process(myTM.unaryAllowed(TextUtil.isWhiteSpaceOrEol(myCM.safeReadAt(2))) ? tDSTAR : tPOW); }
"~"                                     {   return myTM.process(tTILDE); }


"[]"                                    {   if (myTM.isFnameAllowed()){
                                                return myTM.process(tAREF);
                                            }
                                            yypushback(1);
                                            return myTM.process(myTM.isFTokenAllowed() ? tfLBRACK :  tLBRACK);
                                        }
"[]="                                   {   if (myTM.isFnameAllowed()){
                                                return myTM.process(tASET);
                                            }
                                            yypushback(2);
                                            return myTM.process(myTM.isFTokenAllowed() ? tfLBRACK :  tLBRACK);
                                        }

"=>"                                    {   return myTM.process(tASSOC); }



"."                                     {   return myTM.process(tDOT); }
"::"                                    {   return myTM.process(myTM.isColon2Allowed() ? tCOLON2 : tCOLON3); }

":"                                     {   return myTM.process(tCOLON); }
":" / (!((!.) | ":"|{EOL}|{WHITE_SPACE_CHAR})) 
                                        {   return myTM.process(tSYMBEG); }

".."                                    {   return myTM.process(tDOT2); }
"..."                                   {   return myTM.process(tDOT3); }

","                                     {   return myTM.process(tCOMMA); }
";"                                     {   return myTM.process(tSEMICOLON); }


"?"                                     {   return myTM.process(tQUESTION); }

{WHITE_SPACE}                           {   return myTM.process(tWHITE_SPACE); }
{LINE_CONTINUATION}                     {   return myTM.process(tLINE_CONTINUATION); }

{EOL}                                   {   if (myTM.ignoreEOL()){
                                                return myTM.process(tWHITE_SPACE);
                                            }
                                            if (mySM.isAfterHeredoc()){
                                                mySM.toState(IN_HEREDOC_STATE);
                                                return myTM.process(tEOL);
                                            }
// foo
//   .bar - the call goes on with a leading dot
                                            if (myCM.isLeadingDotAhead(yylength())){
                                                return myTM.process(tWHITE_SPACE);
                                            }
                                            return myTM.process(tEOL);
                                        }


{ANY_CHAR}                              {   return myTM.process(tBAD_CHARACTER); }