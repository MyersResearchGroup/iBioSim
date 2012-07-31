// $ANTLR 3.4 ../src/antlrPackage/Property.g 2012-07-31 16:12:35

  //package lpn.parser.properties;
  package antlrPackage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class PropertyLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__47=47;
    public static final int AND=4;
    public static final int ASSERT=5;
    public static final int ASSERT_UNTIL=6;
    public static final int BOOLEAN=7;
    public static final int COMMA=8;
    public static final int COMMENT=9;
    public static final int DIV=10;
    public static final int ELSE=11;
    public static final int ELSEIF=12;
    public static final int END=13;
    public static final int EQUAL=14;
    public static final int ESC_SEQ=15;
    public static final int EXPONENT=16;
    public static final int FLOAT=17;
    public static final int GET=18;
    public static final int GETEQ=19;
    public static final int HEX_DIGIT=20;
    public static final int ID=21;
    public static final int IF=22;
    public static final int INT=23;
    public static final int INTEGER=24;
    public static final int LCURL=25;
    public static final int LET=26;
    public static final int LETEQ=27;
    public static final int LPARA=28;
    public static final int MINUS=29;
    public static final int MOD=30;
    public static final int MULT=31;
    public static final int NOT=32;
    public static final int NOT_EQUAL=33;
    public static final int OCTAL_ESC=34;
    public static final int OR=35;
    public static final int PLUS=36;
    public static final int RCURL=37;
    public static final int REAL=38;
    public static final int RPARA=39;
    public static final int SAMEAS=40;
    public static final int SEMICOL=41;
    public static final int STRING=42;
    public static final int UNICODE_ESC=43;
    public static final int WAIT=44;
    public static final int WAIT_STABLE=45;
    public static final int WS=46;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public PropertyLexer() {} 
    public PropertyLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PropertyLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "../src/antlrPackage/Property.g"; }

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:12:7: ( 'property' )
            // ../src/antlrPackage/Property.g:12:9: 'property'
            {
            match("property"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:39:2: ( 'boolean' )
            // ../src/antlrPackage/Property.g:39:2: 'boolean'
            {
            match("boolean"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "REAL"
    public final void mREAL() throws RecognitionException {
        try {
            int _type = REAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:43:2: ( 'real' )
            // ../src/antlrPackage/Property.g:43:2: 'real'
            {
            match("real"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "REAL"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:47:2: ( 'int' )
            // ../src/antlrPackage/Property.g:47:2: 'int'
            {
            match("int"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "WAIT"
    public final void mWAIT() throws RecognitionException {
        try {
            int _type = WAIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:52:2: ( 'wait' )
            // ../src/antlrPackage/Property.g:52:2: 'wait'
            {
            match("wait"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WAIT"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:57:3: ( '~' )
            // ../src/antlrPackage/Property.g:57:3: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "MOD"
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:61:2: ( '%' )
            // ../src/antlrPackage/Property.g:61:2: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MOD"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:66:2: ( '&' )
            // ../src/antlrPackage/Property.g:66:2: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:70:2: ( '|' )
            // ../src/antlrPackage/Property.g:70:2: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "ASSERT"
    public final void mASSERT() throws RecognitionException {
        try {
            int _type = ASSERT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:75:2: ( 'assert' )
            // ../src/antlrPackage/Property.g:75:2: 'assert'
            {
            match("assert"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ASSERT"

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:80:2: ( 'if' )
            // ../src/antlrPackage/Property.g:80:2: 'if'
            {
            match("if"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "END"
    public final void mEND() throws RecognitionException {
        try {
            int _type = END;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:85:2: ( 'end' )
            // ../src/antlrPackage/Property.g:85:2: 'end'
            {
            match("end"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "END"

    // $ANTLR start "ELSEIF"
    public final void mELSEIF() throws RecognitionException {
        try {
            int _type = ELSEIF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:90:2: ( 'else if' )
            // ../src/antlrPackage/Property.g:90:2: 'else if'
            {
            match("else if"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ELSEIF"

    // $ANTLR start "ELSE"
    public final void mELSE() throws RecognitionException {
        try {
            int _type = ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:94:2: ( 'else' )
            // ../src/antlrPackage/Property.g:94:2: 'else'
            {
            match("else"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ELSE"

    // $ANTLR start "WAIT_STABLE"
    public final void mWAIT_STABLE() throws RecognitionException {
        try {
            int _type = WAIT_STABLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:98:2: ( 'waitStable' )
            // ../src/antlrPackage/Property.g:98:2: 'waitStable'
            {
            match("waitStable"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WAIT_STABLE"

    // $ANTLR start "ASSERT_UNTIL"
    public final void mASSERT_UNTIL() throws RecognitionException {
        try {
            int _type = ASSERT_UNTIL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:102:2: ( 'assertUntil' )
            // ../src/antlrPackage/Property.g:102:2: 'assertUntil'
            {
            match("assertUntil"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ASSERT_UNTIL"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:106:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // ../src/antlrPackage/Property.g:106:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // ../src/antlrPackage/Property.g:106:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../src/antlrPackage/Property.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:109:5: ( ( '0' .. '9' )+ )
            // ../src/antlrPackage/Property.g:109:7: ( '0' .. '9' )+
            {
            // ../src/antlrPackage/Property.g:109:7: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../src/antlrPackage/Property.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:113:5: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT )
            int alt9=3;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // ../src/antlrPackage/Property.g:113:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )?
                    {
                    // ../src/antlrPackage/Property.g:113:9: ( '0' .. '9' )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);


                    match('.'); 

                    // ../src/antlrPackage/Property.g:113:25: ( '0' .. '9' )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    // ../src/antlrPackage/Property.g:113:37: ( EXPONENT )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0=='E'||LA5_0=='e') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // ../src/antlrPackage/Property.g:113:37: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ../src/antlrPackage/Property.g:114:9: '.' ( '0' .. '9' )+ ( EXPONENT )?
                    {
                    match('.'); 

                    // ../src/antlrPackage/Property.g:114:13: ( '0' .. '9' )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    // ../src/antlrPackage/Property.g:114:25: ( EXPONENT )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='E'||LA7_0=='e') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // ../src/antlrPackage/Property.g:114:25: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // ../src/antlrPackage/Property.g:115:9: ( '0' .. '9' )+ EXPONENT
                    {
                    // ../src/antlrPackage/Property.g:115:9: ( '0' .. '9' )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0 >= '0' && LA8_0 <= '9')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    mEXPONENT(); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:119:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='/') ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1=='/') ) {
                    alt13=1;
                }
                else if ( (LA13_1=='*') ) {
                    alt13=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }
            switch (alt13) {
                case 1 :
                    // ../src/antlrPackage/Property.g:119:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 



                    // ../src/antlrPackage/Property.g:119:14: (~ ( '\\n' | '\\r' ) )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0 >= '\u0000' && LA10_0 <= '\t')||(LA10_0 >= '\u000B' && LA10_0 <= '\f')||(LA10_0 >= '\u000E' && LA10_0 <= '\uFFFF')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:
                    	    {
                    	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    // ../src/antlrPackage/Property.g:119:28: ( '\\r' )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0=='\r') ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // ../src/antlrPackage/Property.g:119:28: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }


                    match('\n'); 

                    _channel=HIDDEN;

                    }
                    break;
                case 2 :
                    // ../src/antlrPackage/Property.g:120:9: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 



                    // ../src/antlrPackage/Property.g:120:14: ( options {greedy=false; } : . )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0=='*') ) {
                            int LA12_1 = input.LA(2);

                            if ( (LA12_1=='/') ) {
                                alt12=2;
                            }
                            else if ( ((LA12_1 >= '\u0000' && LA12_1 <= '.')||(LA12_1 >= '0' && LA12_1 <= '\uFFFF')) ) {
                                alt12=1;
                            }


                        }
                        else if ( ((LA12_0 >= '\u0000' && LA12_0 <= ')')||(LA12_0 >= '+' && LA12_0 <= '\uFFFF')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // ../src/antlrPackage/Property.g:120:42: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    match("*/"); 



                    _channel=HIDDEN;

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:123:5: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' ) )
            // ../src/antlrPackage/Property.g:123:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
            {
            // ../src/antlrPackage/Property.g:123:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
            int alt14=5;
            switch ( input.LA(1) ) {
            case ' ':
                {
                alt14=1;
                }
                break;
            case '\t':
                {
                alt14=2;
                }
                break;
            case '\r':
                {
                int LA14_3 = input.LA(2);

                if ( (LA14_3=='\n') ) {
                    alt14=5;
                }
                else {
                    alt14=3;
                }
                }
                break;
            case '\n':
                {
                alt14=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // ../src/antlrPackage/Property.g:123:11: ' '
                    {
                    match(' '); 

                    }
                    break;
                case 2 :
                    // ../src/antlrPackage/Property.g:124:11: '\\t'
                    {
                    match('\t'); 

                    }
                    break;
                case 3 :
                    // ../src/antlrPackage/Property.g:125:11: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // ../src/antlrPackage/Property.g:126:11: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 5 :
                    // ../src/antlrPackage/Property.g:127:10: '\\r\\n'
                    {
                    match("\r\n"); 



                    }
                    break;

            }


            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:134:5: ( '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\'' )
            // ../src/antlrPackage/Property.g:134:8: '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\''
            {
            match('\''); 

            // ../src/antlrPackage/Property.g:134:13: ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )*
            loop15:
            do {
                int alt15=3;
                int LA15_0 = input.LA(1);

                if ( (LA15_0=='\\') ) {
                    alt15=1;
                }
                else if ( ((LA15_0 >= '\u0000' && LA15_0 <= '&')||(LA15_0 >= '(' && LA15_0 <= '[')||(LA15_0 >= ']' && LA15_0 <= '\uFFFF')) ) {
                    alt15=2;
                }


                switch (alt15) {
            	case 1 :
            	    // ../src/antlrPackage/Property.g:134:15: ESC_SEQ
            	    {
            	    mESC_SEQ(); 


            	    }
            	    break;
            	case 2 :
            	    // ../src/antlrPackage/Property.g:134:25: ~ ( '\\\\' | '\\'' )
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // ../src/antlrPackage/Property.g:140:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // ../src/antlrPackage/Property.g:140:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // ../src/antlrPackage/Property.g:140:22: ( '+' | '-' )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='+'||LA16_0=='-') ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // ../src/antlrPackage/Property.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }


            // ../src/antlrPackage/Property.g:140:33: ( '0' .. '9' )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0 >= '0' && LA17_0 <= '9')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // ../src/antlrPackage/Property.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt17 >= 1 ) break loop17;
                        EarlyExitException eee =
                            new EarlyExitException(17, input);
                        throw eee;
                }
                cnt17++;
            } while (true);


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // ../src/antlrPackage/Property.g:143:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // ../src/antlrPackage/Property.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // ../src/antlrPackage/Property.g:147:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt18=3;
            int LA18_0 = input.LA(1);

            if ( (LA18_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt18=1;
                    }
                    break;
                case 'u':
                    {
                    alt18=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt18=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;

                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // ../src/antlrPackage/Property.g:147:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 

                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // ../src/antlrPackage/Property.g:148:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 


                    }
                    break;
                case 3 :
                    // ../src/antlrPackage/Property.g:149:9: OCTAL_ESC
                    {
                    mOCTAL_ESC(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // ../src/antlrPackage/Property.g:154:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt19=3;
            int LA19_0 = input.LA(1);

            if ( (LA19_0=='\\') ) {
                int LA19_1 = input.LA(2);

                if ( ((LA19_1 >= '0' && LA19_1 <= '3')) ) {
                    int LA19_2 = input.LA(3);

                    if ( ((LA19_2 >= '0' && LA19_2 <= '7')) ) {
                        int LA19_4 = input.LA(4);

                        if ( ((LA19_4 >= '0' && LA19_4 <= '7')) ) {
                            alt19=1;
                        }
                        else {
                            alt19=2;
                        }
                    }
                    else {
                        alt19=3;
                    }
                }
                else if ( ((LA19_1 >= '4' && LA19_1 <= '7')) ) {
                    int LA19_3 = input.LA(3);

                    if ( ((LA19_3 >= '0' && LA19_3 <= '7')) ) {
                        alt19=2;
                    }
                    else {
                        alt19=3;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }
            switch (alt19) {
                case 1 :
                    // ../src/antlrPackage/Property.g:154:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // ../src/antlrPackage/Property.g:155:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 3 :
                    // ../src/antlrPackage/Property.g:156:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // ../src/antlrPackage/Property.g:161:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // ../src/antlrPackage/Property.g:161:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
            match('\\'); 

            match('u'); 

            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UNICODE_ESC"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:167:2: ( '+' )
            // ../src/antlrPackage/Property.g:167:2: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:171:3: ( '-' )
            // ../src/antlrPackage/Property.g:171:3: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "MULT"
    public final void mMULT() throws RecognitionException {
        try {
            int _type = MULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:176:2: ( '*' )
            // ../src/antlrPackage/Property.g:176:2: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MULT"

    // $ANTLR start "DIV"
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:180:2: ( '/' )
            // ../src/antlrPackage/Property.g:180:2: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIV"

    // $ANTLR start "EQUAL"
    public final void mEQUAL() throws RecognitionException {
        try {
            int _type = EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:186:2: ( '=' )
            // ../src/antlrPackage/Property.g:186:2: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EQUAL"

    // $ANTLR start "NOT_EQUAL"
    public final void mNOT_EQUAL() throws RecognitionException {
        try {
            int _type = NOT_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:191:2: ( '!=' )
            // ../src/antlrPackage/Property.g:191:2: '!='
            {
            match("!="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NOT_EQUAL"

    // $ANTLR start "GET"
    public final void mGET() throws RecognitionException {
        try {
            int _type = GET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:195:2: ( '>' )
            // ../src/antlrPackage/Property.g:195:2: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GET"

    // $ANTLR start "LET"
    public final void mLET() throws RecognitionException {
        try {
            int _type = LET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:199:2: ( '<' )
            // ../src/antlrPackage/Property.g:199:2: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LET"

    // $ANTLR start "GETEQ"
    public final void mGETEQ() throws RecognitionException {
        try {
            int _type = GETEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:203:2: ( '>=' )
            // ../src/antlrPackage/Property.g:203:2: '>='
            {
            match(">="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GETEQ"

    // $ANTLR start "LETEQ"
    public final void mLETEQ() throws RecognitionException {
        try {
            int _type = LETEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:206:2: ( '<=' )
            // ../src/antlrPackage/Property.g:206:2: '<='
            {
            match("<="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LETEQ"

    // $ANTLR start "SAMEAS"
    public final void mSAMEAS() throws RecognitionException {
        try {
            int _type = SAMEAS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:211:2: ( '==' )
            // ../src/antlrPackage/Property.g:211:2: '=='
            {
            match("=="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SAMEAS"

    // $ANTLR start "LPARA"
    public final void mLPARA() throws RecognitionException {
        try {
            int _type = LPARA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:217:2: ( '(' )
            // ../src/antlrPackage/Property.g:217:2: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LPARA"

    // $ANTLR start "RPARA"
    public final void mRPARA() throws RecognitionException {
        try {
            int _type = RPARA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:221:2: ( ')' )
            // ../src/antlrPackage/Property.g:221:2: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RPARA"

    // $ANTLR start "LCURL"
    public final void mLCURL() throws RecognitionException {
        try {
            int _type = LCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:225:2: ( '{' )
            // ../src/antlrPackage/Property.g:225:2: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LCURL"

    // $ANTLR start "RCURL"
    public final void mRCURL() throws RecognitionException {
        try {
            int _type = RCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:229:2: ( '}' )
            // ../src/antlrPackage/Property.g:229:2: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RCURL"

    // $ANTLR start "SEMICOL"
    public final void mSEMICOL() throws RecognitionException {
        try {
            int _type = SEMICOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:235:2: ( ';' )
            // ../src/antlrPackage/Property.g:235:2: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SEMICOL"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../src/antlrPackage/Property.g:239:2: ( ',' )
            // ../src/antlrPackage/Property.g:239:2: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMA"

    public void mTokens() throws RecognitionException {
        // ../src/antlrPackage/Property.g:1:8: ( T__47 | BOOLEAN | REAL | INTEGER | WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_UNTIL | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA )
        int alt20=39;
        alt20 = dfa20.predict(input);
        switch (alt20) {
            case 1 :
                // ../src/antlrPackage/Property.g:1:10: T__47
                {
                mT__47(); 


                }
                break;
            case 2 :
                // ../src/antlrPackage/Property.g:1:16: BOOLEAN
                {
                mBOOLEAN(); 


                }
                break;
            case 3 :
                // ../src/antlrPackage/Property.g:1:24: REAL
                {
                mREAL(); 


                }
                break;
            case 4 :
                // ../src/antlrPackage/Property.g:1:29: INTEGER
                {
                mINTEGER(); 


                }
                break;
            case 5 :
                // ../src/antlrPackage/Property.g:1:37: WAIT
                {
                mWAIT(); 


                }
                break;
            case 6 :
                // ../src/antlrPackage/Property.g:1:42: NOT
                {
                mNOT(); 


                }
                break;
            case 7 :
                // ../src/antlrPackage/Property.g:1:46: MOD
                {
                mMOD(); 


                }
                break;
            case 8 :
                // ../src/antlrPackage/Property.g:1:50: AND
                {
                mAND(); 


                }
                break;
            case 9 :
                // ../src/antlrPackage/Property.g:1:54: OR
                {
                mOR(); 


                }
                break;
            case 10 :
                // ../src/antlrPackage/Property.g:1:57: ASSERT
                {
                mASSERT(); 


                }
                break;
            case 11 :
                // ../src/antlrPackage/Property.g:1:64: IF
                {
                mIF(); 


                }
                break;
            case 12 :
                // ../src/antlrPackage/Property.g:1:67: END
                {
                mEND(); 


                }
                break;
            case 13 :
                // ../src/antlrPackage/Property.g:1:71: ELSEIF
                {
                mELSEIF(); 


                }
                break;
            case 14 :
                // ../src/antlrPackage/Property.g:1:78: ELSE
                {
                mELSE(); 


                }
                break;
            case 15 :
                // ../src/antlrPackage/Property.g:1:83: WAIT_STABLE
                {
                mWAIT_STABLE(); 


                }
                break;
            case 16 :
                // ../src/antlrPackage/Property.g:1:95: ASSERT_UNTIL
                {
                mASSERT_UNTIL(); 


                }
                break;
            case 17 :
                // ../src/antlrPackage/Property.g:1:108: ID
                {
                mID(); 


                }
                break;
            case 18 :
                // ../src/antlrPackage/Property.g:1:111: INT
                {
                mINT(); 


                }
                break;
            case 19 :
                // ../src/antlrPackage/Property.g:1:115: FLOAT
                {
                mFLOAT(); 


                }
                break;
            case 20 :
                // ../src/antlrPackage/Property.g:1:121: COMMENT
                {
                mCOMMENT(); 


                }
                break;
            case 21 :
                // ../src/antlrPackage/Property.g:1:129: WS
                {
                mWS(); 


                }
                break;
            case 22 :
                // ../src/antlrPackage/Property.g:1:132: STRING
                {
                mSTRING(); 


                }
                break;
            case 23 :
                // ../src/antlrPackage/Property.g:1:139: PLUS
                {
                mPLUS(); 


                }
                break;
            case 24 :
                // ../src/antlrPackage/Property.g:1:144: MINUS
                {
                mMINUS(); 


                }
                break;
            case 25 :
                // ../src/antlrPackage/Property.g:1:150: MULT
                {
                mMULT(); 


                }
                break;
            case 26 :
                // ../src/antlrPackage/Property.g:1:155: DIV
                {
                mDIV(); 


                }
                break;
            case 27 :
                // ../src/antlrPackage/Property.g:1:159: EQUAL
                {
                mEQUAL(); 


                }
                break;
            case 28 :
                // ../src/antlrPackage/Property.g:1:165: NOT_EQUAL
                {
                mNOT_EQUAL(); 


                }
                break;
            case 29 :
                // ../src/antlrPackage/Property.g:1:175: GET
                {
                mGET(); 


                }
                break;
            case 30 :
                // ../src/antlrPackage/Property.g:1:179: LET
                {
                mLET(); 


                }
                break;
            case 31 :
                // ../src/antlrPackage/Property.g:1:183: GETEQ
                {
                mGETEQ(); 


                }
                break;
            case 32 :
                // ../src/antlrPackage/Property.g:1:189: LETEQ
                {
                mLETEQ(); 


                }
                break;
            case 33 :
                // ../src/antlrPackage/Property.g:1:195: SAMEAS
                {
                mSAMEAS(); 


                }
                break;
            case 34 :
                // ../src/antlrPackage/Property.g:1:202: LPARA
                {
                mLPARA(); 


                }
                break;
            case 35 :
                // ../src/antlrPackage/Property.g:1:208: RPARA
                {
                mRPARA(); 


                }
                break;
            case 36 :
                // ../src/antlrPackage/Property.g:1:214: LCURL
                {
                mLCURL(); 


                }
                break;
            case 37 :
                // ../src/antlrPackage/Property.g:1:220: RCURL
                {
                mRCURL(); 


                }
                break;
            case 38 :
                // ../src/antlrPackage/Property.g:1:226: SEMICOL
                {
                mSEMICOL(); 


                }
                break;
            case 39 :
                // ../src/antlrPackage/Property.g:1:234: COMMA
                {
                mCOMMA(); 


                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    protected DFA20 dfa20 = new DFA20(this);
    static final String DFA9_eotS =
        "\5\uffff";
    static final String DFA9_eofS =
        "\5\uffff";
    static final String DFA9_minS =
        "\2\56\3\uffff";
    static final String DFA9_maxS =
        "\1\71\1\145\3\uffff";
    static final String DFA9_acceptS =
        "\2\uffff\1\2\1\1\1\3";
    static final String DFA9_specialS =
        "\5\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\2\1\uffff\12\1",
            "\1\3\1\uffff\12\1\13\uffff\1\4\37\uffff\1\4",
            "",
            "",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "112:1: FLOAT : ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT );";
        }
    }
    static final String DFA20_eotS =
        "\1\uffff\5\14\4\uffff\2\14\1\uffff\1\50\1\uffff\1\52\5\uffff\1\54"+
        "\1\uffff\1\56\1\60\6\uffff\4\14\1\65\4\14\11\uffff\3\14\1\75\1\uffff"+
        "\2\14\1\100\3\14\1\104\1\uffff\1\106\1\14\1\uffff\1\111\2\14\1\uffff"+
        "\1\14\1\uffff\1\14\2\uffff\3\14\1\122\1\14\1\124\2\14\1\uffff\1"+
        "\127\1\uffff\2\14\1\uffff\2\14\1\134\1\14\1\uffff\1\136\1\uffff";
    static final String DFA20_eofS =
        "\137\uffff";
    static final String DFA20_minS =
        "\1\11\1\162\1\157\1\145\1\146\1\141\4\uffff\1\163\1\154\1\uffff"+
        "\1\56\1\uffff\1\52\5\uffff\1\75\1\uffff\2\75\6\uffff\2\157\1\141"+
        "\1\164\1\60\1\151\1\163\1\144\1\163\11\uffff\1\160\2\154\1\60\1"+
        "\uffff\1\164\1\145\1\60\3\145\1\60\1\uffff\1\60\1\162\1\uffff\1"+
        "\40\1\162\1\141\1\uffff\1\164\1\uffff\1\164\2\uffff\1\164\1\156"+
        "\1\141\1\60\1\171\1\60\1\142\1\156\1\uffff\1\60\1\uffff\1\154\1"+
        "\164\1\uffff\1\145\1\151\1\60\1\154\1\uffff\1\60\1\uffff";
    static final String DFA20_maxS =
        "\1\176\1\162\1\157\1\145\1\156\1\141\4\uffff\1\163\1\156\1\uffff"+
        "\1\145\1\uffff\1\57\5\uffff\1\75\1\uffff\2\75\6\uffff\2\157\1\141"+
        "\1\164\1\172\1\151\1\163\1\144\1\163\11\uffff\1\160\2\154\1\172"+
        "\1\uffff\1\164\1\145\1\172\3\145\1\172\1\uffff\1\172\1\162\1\uffff"+
        "\1\172\1\162\1\141\1\uffff\1\164\1\uffff\1\164\2\uffff\1\164\1\156"+
        "\1\141\1\172\1\171\1\172\1\142\1\156\1\uffff\1\172\1\uffff\1\154"+
        "\1\164\1\uffff\1\145\1\151\1\172\1\154\1\uffff\1\172\1\uffff";
    static final String DFA20_acceptS =
        "\6\uffff\1\6\1\7\1\10\1\11\2\uffff\1\21\1\uffff\1\23\1\uffff\1\25"+
        "\1\26\1\27\1\30\1\31\1\uffff\1\34\2\uffff\1\42\1\43\1\44\1\45\1"+
        "\46\1\47\11\uffff\1\22\1\24\1\32\1\41\1\33\1\37\1\35\1\40\1\36\4"+
        "\uffff\1\13\7\uffff\1\4\2\uffff\1\14\3\uffff\1\3\1\uffff\1\5\1\uffff"+
        "\1\15\1\16\10\uffff\1\12\1\uffff\1\2\2\uffff\1\1\4\uffff\1\17\1"+
        "\uffff\1\20";
    static final String DFA20_specialS =
        "\137\uffff}>";
    static final String[] DFA20_transitionS = {
            "\2\20\2\uffff\1\20\22\uffff\1\20\1\26\3\uffff\1\7\1\10\1\21"+
            "\1\31\1\32\1\24\1\22\1\36\1\23\1\16\1\17\12\15\1\uffff\1\35"+
            "\1\30\1\25\1\27\2\uffff\32\14\4\uffff\1\14\1\uffff\1\12\1\2"+
            "\2\14\1\13\3\14\1\4\6\14\1\1\1\14\1\3\4\14\1\5\3\14\1\33\1\11"+
            "\1\34\1\6",
            "\1\37",
            "\1\40",
            "\1\41",
            "\1\43\7\uffff\1\42",
            "\1\44",
            "",
            "",
            "",
            "",
            "\1\45",
            "\1\47\1\uffff\1\46",
            "",
            "\1\16\1\uffff\12\15\13\uffff\1\16\37\uffff\1\16",
            "",
            "\1\51\4\uffff\1\51",
            "",
            "",
            "",
            "",
            "",
            "\1\53",
            "",
            "\1\55",
            "\1\57",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\72",
            "\1\73",
            "\1\74",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\76",
            "\1\77",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\101",
            "\1\102",
            "\1\103",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\12\14\7\uffff\22\14\1\105\7\14\4\uffff\1\14\1\uffff\32\14",
            "\1\107",
            "",
            "\1\110\17\uffff\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32"+
            "\14",
            "\1\112",
            "\1\113",
            "",
            "\1\114",
            "",
            "\1\115",
            "",
            "",
            "\1\116",
            "\1\117",
            "\1\120",
            "\12\14\7\uffff\24\14\1\121\5\14\4\uffff\1\14\1\uffff\32\14",
            "\1\123",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\125",
            "\1\126",
            "",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\130",
            "\1\131",
            "",
            "\1\132",
            "\1\133",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\135",
            "",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            ""
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__47 | BOOLEAN | REAL | INTEGER | WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_UNTIL | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA );";
        }
    }
 

}