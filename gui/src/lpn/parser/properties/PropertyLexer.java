package lpn.parser.properties;
  //package antlrPackage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class PropertyLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__50=50;
    public static final int ALWAYS=4;
    public static final int AND=5;
    public static final int ASSERT=6;
    public static final int ASSERT_STABLE=7;
    public static final int ASSERT_UNTIL=8;
    public static final int BOOLEAN=9;
    public static final int COMMA=10;
    public static final int COMMENT=11;
    public static final int DIV=12;
    public static final int ELSE=13;
    public static final int ELSEIF=14;
    public static final int END=15;
    public static final int EQUAL=16;
    public static final int ESC_SEQ=17;
    public static final int EXPONENT=18;
    public static final int FLOAT=19;
    public static final int GET=20;
    public static final int GETEQ=21;
    public static final int HEX_DIGIT=22;
    public static final int ID=23;
    public static final int IF=24;
    public static final int INT=25;
    public static final int INTEGER=26;
    public static final int LCURL=27;
    public static final int LET=28;
    public static final int LETEQ=29;
    public static final int LPARA=30;
    public static final int MINUS=31;
    public static final int MOD=32;
    public static final int MULT=33;
    public static final int NOT=34;
    public static final int NOT_EQUAL=35;
    public static final int OCTAL_ESC=36;
    public static final int OR=37;
    public static final int PLUS=38;
    public static final int POSEDGE=39;
    public static final int RCURL=40;
    public static final int REAL=41;
    public static final int RPARA=42;
    public static final int SAMEAS=43;
    public static final int SEMICOL=44;
    public static final int STRING=45;
    public static final int UNICODE_ESC=46;
    public static final int WAIT=47;
    public static final int WAIT_STABLE=48;
    public static final int WS=49;

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
    public String getGrammarFileName() { return "/home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g"; }

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:12:7: ( 'property' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:12:9: 'property'
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
    // $ANTLR end "T__50"

    // $ANTLR start "ALWAYS"
    public final void mALWAYS() throws RecognitionException {
        try {
            int _type = ALWAYS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:39:3: ( 'always' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:39:3: 'always'
            {
            match("always"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ALWAYS"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:2: ( 'boolean' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:2: 'boolean'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: ( 'real' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: 'real'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:51:2: ( 'int' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:51:2: 'int'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:56:2: ( 'wait' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:56:2: 'wait'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:61:3: ( '~' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:61:3: '~'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:65:2: ( '%' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:65:2: '%'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:70:2: ( '&' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:70:2: '&'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:74:2: ( '|' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:74:2: '|'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:2: ( 'assert' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:2: 'assert'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:2: ( 'if' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:2: 'if'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:89:2: ( 'end' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:89:2: 'end'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:94:2: ( 'else if' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:94:2: 'else if'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:98:2: ( 'else' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:98:2: 'else'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:102:2: ( 'waitStable' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:102:2: 'waitStable'
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

    // $ANTLR start "ASSERT_STABLE"
    public final void mASSERT_STABLE() throws RecognitionException {
        try {
            int _type = ASSERT_STABLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:106:2: ( 'assertStable' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:106:2: 'assertStable'
            {
            match("assertStable"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ASSERT_STABLE"

    // $ANTLR start "ASSERT_UNTIL"
    public final void mASSERT_UNTIL() throws RecognitionException {
        try {
            int _type = ASSERT_UNTIL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:110:2: ( 'assertUntil' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:110:2: 'assertUntil'
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

    // $ANTLR start "POSEDGE"
    public final void mPOSEDGE() throws RecognitionException {
        try {
            int _type = POSEDGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:114:3: ( 'posedge' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:114:3: 'posedge'
            {
            match("posedge"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "POSEDGE"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:118:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:118:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:118:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:121:5: ( ( '0' .. '9' )+ )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:121:7: ( '0' .. '9' )+
            {
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:121:7: ( '0' .. '9' )+
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
            	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:5: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT )
            int alt9=3;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )?
                    {
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:9: ( '0' .. '9' )+
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
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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

                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:25: ( '0' .. '9' )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:37: ( EXPONENT )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0=='E'||LA5_0=='e') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:125:37: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:9: '.' ( '0' .. '9' )+ ( EXPONENT )?
                    {
                    match('.'); 

                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:13: ( '0' .. '9' )+
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
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:25: ( EXPONENT )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='E'||LA7_0=='e') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:25: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:127:9: ( '0' .. '9' )+ EXPONENT
                    {
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:127:9: ( '0' .. '9' )+
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
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:131:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:131:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 



                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:131:14: (~ ( '\\n' | '\\r' ) )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0 >= '\u0000' && LA10_0 <= '\t')||(LA10_0 >= '\u000B' && LA10_0 <= '\f')||(LA10_0 >= '\u000E' && LA10_0 <= '\uFFFF')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:131:28: ( '\\r' )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0=='\r') ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:131:28: '\\r'
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:132:9: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 



                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:132:14: ( options {greedy=false; } : . )*
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
                    	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:132:42: .
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:5: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' ) )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
            {
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:11: ' '
                    {
                    match(' '); 

                    }
                    break;
                case 2 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:136:11: '\\t'
                    {
                    match('\t'); 

                    }
                    break;
                case 3 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:137:11: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:138:11: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 5 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:10: '\\r\\n'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:5: ( '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\'' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:8: '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\''
            {
            match('\''); 

            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:13: ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )*
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
            	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:15: ESC_SEQ
            	    {
            	    mESC_SEQ(); 


            	    }
            	    break;
            	case 2 :
            	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:25: ~ ( '\\\\' | '\\'' )
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:152:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:152:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:152:22: ( '+' | '-' )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='+'||LA16_0=='-') ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:152:33: ( '0' .. '9' )+
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
            	    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:155:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:159:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:159:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 


                    }
                    break;
                case 3 :
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:161:9: OCTAL_ESC
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:166:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:166:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:167:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
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
                    // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:168:9: '\\\\' ( '0' .. '7' )
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:173:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:173:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:179:2: ( '+' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:179:2: '+'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:183:3: ( '-' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:183:3: '-'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:188:2: ( '*' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:188:2: '*'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:192:2: ( '/' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:192:2: '/'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:198:2: ( '=' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:198:2: '='
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:203:2: ( '!=' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:203:2: '!='
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:207:2: ( '>' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:207:2: '>'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:211:2: ( '<' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:211:2: '<'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:215:2: ( '>=' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:215:2: '>='
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:218:2: ( '<=' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:218:2: '<='
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:223:2: ( '==' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:223:2: '=='
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:229:2: ( '(' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:229:2: '('
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:233:2: ( ')' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:233:2: ')'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:237:2: ( '{' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:237:2: '{'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:241:2: ( '}' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:241:2: '}'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:247:2: ( ';' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:247:2: ';'
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
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:251:2: ( ',' )
            // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:251:2: ','
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
        // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:8: ( T__50 | ALWAYS | BOOLEAN | REAL | INTEGER | WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_STABLE | ASSERT_UNTIL | POSEDGE | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA )
        int alt20=42;
        alt20 = dfa20.predict(input);
        switch (alt20) {
            case 1 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:10: T__50
                {
                mT__50(); 


                }
                break;
            case 2 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:16: ALWAYS
                {
                mALWAYS(); 


                }
                break;
            case 3 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:23: BOOLEAN
                {
                mBOOLEAN(); 


                }
                break;
            case 4 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:31: REAL
                {
                mREAL(); 


                }
                break;
            case 5 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:36: INTEGER
                {
                mINTEGER(); 


                }
                break;
            case 6 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:44: WAIT
                {
                mWAIT(); 


                }
                break;
            case 7 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:49: NOT
                {
                mNOT(); 


                }
                break;
            case 8 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:53: MOD
                {
                mMOD(); 


                }
                break;
            case 9 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:57: AND
                {
                mAND(); 


                }
                break;
            case 10 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:61: OR
                {
                mOR(); 


                }
                break;
            case 11 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:64: ASSERT
                {
                mASSERT(); 


                }
                break;
            case 12 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:71: IF
                {
                mIF(); 


                }
                break;
            case 13 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:74: END
                {
                mEND(); 


                }
                break;
            case 14 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:78: ELSEIF
                {
                mELSEIF(); 


                }
                break;
            case 15 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:85: ELSE
                {
                mELSE(); 


                }
                break;
            case 16 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:90: WAIT_STABLE
                {
                mWAIT_STABLE(); 


                }
                break;
            case 17 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:102: ASSERT_STABLE
                {
                mASSERT_STABLE(); 


                }
                break;
            case 18 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:116: ASSERT_UNTIL
                {
                mASSERT_UNTIL(); 


                }
                break;
            case 19 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:129: POSEDGE
                {
                mPOSEDGE(); 


                }
                break;
            case 20 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:137: ID
                {
                mID(); 


                }
                break;
            case 21 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:140: INT
                {
                mINT(); 


                }
                break;
            case 22 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:144: FLOAT
                {
                mFLOAT(); 


                }
                break;
            case 23 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:150: COMMENT
                {
                mCOMMENT(); 


                }
                break;
            case 24 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:158: WS
                {
                mWS(); 


                }
                break;
            case 25 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:161: STRING
                {
                mSTRING(); 


                }
                break;
            case 26 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:168: PLUS
                {
                mPLUS(); 


                }
                break;
            case 27 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:173: MINUS
                {
                mMINUS(); 


                }
                break;
            case 28 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:179: MULT
                {
                mMULT(); 


                }
                break;
            case 29 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:184: DIV
                {
                mDIV(); 


                }
                break;
            case 30 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:188: EQUAL
                {
                mEQUAL(); 


                }
                break;
            case 31 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:194: NOT_EQUAL
                {
                mNOT_EQUAL(); 


                }
                break;
            case 32 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:204: GET
                {
                mGET(); 


                }
                break;
            case 33 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:208: LET
                {
                mLET(); 


                }
                break;
            case 34 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:212: GETEQ
                {
                mGETEQ(); 


                }
                break;
            case 35 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:218: LETEQ
                {
                mLETEQ(); 


                }
                break;
            case 36 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:224: SAMEAS
                {
                mSAMEAS(); 


                }
                break;
            case 37 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:231: LPARA
                {
                mLPARA(); 


                }
                break;
            case 38 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:237: RPARA
                {
                mRPARA(); 


                }
                break;
            case 39 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:243: LCURL
                {
                mLCURL(); 


                }
                break;
            case 40 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:249: RCURL
                {
                mRCURL(); 


                }
                break;
            case 41 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:255: SEMICOL
                {
                mSEMICOL(); 


                }
                break;
            case 42 :
                // /home/chou/dhanashree/research/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:263: COMMA
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
            return "124:1: FLOAT : ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT );";
        }
    }
    static final String DFA20_eotS =
        "\1\uffff\6\14\4\uffff\1\14\1\uffff\1\52\1\uffff\1\54\5\uffff\1\56"+
        "\1\uffff\1\60\1\62\6\uffff\7\14\1\72\3\14\11\uffff\6\14\1\104\1"+
        "\uffff\1\14\1\106\6\14\1\115\1\uffff\1\117\1\uffff\1\121\5\14\1"+
        "\uffff\1\14\3\uffff\2\14\1\132\1\135\3\14\1\141\1\uffff\2\14\1\uffff"+
        "\1\144\1\14\1\146\1\uffff\2\14\1\uffff\1\14\1\uffff\5\14\1\157\1"+
        "\14\1\161\1\uffff\1\162\2\uffff";
    static final String DFA20_eofS =
        "\163\uffff";
    static final String DFA20_minS =
        "\1\11\1\157\1\154\1\157\1\145\1\146\1\141\4\uffff\1\154\1\uffff"+
        "\1\56\1\uffff\1\52\5\uffff\1\75\1\uffff\2\75\6\uffff\1\157\1\163"+
        "\1\167\1\163\1\157\1\141\1\164\1\60\1\151\1\144\1\163\11\uffff\1"+
        "\160\1\145\1\141\1\145\2\154\1\60\1\uffff\1\164\1\60\2\145\1\144"+
        "\1\171\1\162\1\145\1\60\1\uffff\1\60\1\uffff\1\40\1\162\1\147\1"+
        "\163\1\164\1\141\1\uffff\1\164\3\uffff\1\164\1\145\2\60\1\156\1"+
        "\141\1\171\1\60\1\uffff\1\164\1\156\1\uffff\1\60\1\142\1\60\1\uffff"+
        "\1\141\1\164\1\uffff\1\154\1\uffff\1\142\1\151\1\145\2\154\1\60"+
        "\1\145\1\60\1\uffff\1\60\2\uffff";
    static final String DFA20_maxS =
        "\1\176\1\162\1\163\1\157\1\145\1\156\1\141\4\uffff\1\156\1\uffff"+
        "\1\145\1\uffff\1\57\5\uffff\1\75\1\uffff\2\75\6\uffff\1\157\1\163"+
        "\1\167\1\163\1\157\1\141\1\164\1\172\1\151\1\144\1\163\11\uffff"+
        "\1\160\1\145\1\141\1\145\2\154\1\172\1\uffff\1\164\1\172\2\145\1"+
        "\144\1\171\1\162\1\145\1\172\1\uffff\1\172\1\uffff\1\172\1\162\1"+
        "\147\1\163\1\164\1\141\1\uffff\1\164\3\uffff\1\164\1\145\2\172\1"+
        "\156\1\141\1\171\1\172\1\uffff\1\164\1\156\1\uffff\1\172\1\142\1"+
        "\172\1\uffff\1\141\1\164\1\uffff\1\154\1\uffff\1\142\1\151\1\145"+
        "\2\154\1\172\1\145\1\172\1\uffff\1\172\2\uffff";
    static final String DFA20_acceptS =
        "\7\uffff\1\7\1\10\1\11\1\12\1\uffff\1\24\1\uffff\1\26\1\uffff\1"+
        "\30\1\31\1\32\1\33\1\34\1\uffff\1\37\2\uffff\1\45\1\46\1\47\1\50"+
        "\1\51\1\52\13\uffff\1\25\1\27\1\35\1\44\1\36\1\42\1\40\1\43\1\41"+
        "\7\uffff\1\14\11\uffff\1\5\1\uffff\1\15\6\uffff\1\4\1\uffff\1\6"+
        "\1\16\1\17\10\uffff\1\2\2\uffff\1\13\3\uffff\1\23\2\uffff\1\3\1"+
        "\uffff\1\1\10\uffff\1\20\1\uffff\1\22\1\21";
    static final String DFA20_specialS =
        "\163\uffff}>";
    static final String[] DFA20_transitionS = {
            "\2\20\2\uffff\1\20\22\uffff\1\20\1\26\3\uffff\1\10\1\11\1\21"+
            "\1\31\1\32\1\24\1\22\1\36\1\23\1\16\1\17\12\15\1\uffff\1\35"+
            "\1\30\1\25\1\27\2\uffff\32\14\4\uffff\1\14\1\uffff\1\2\1\3\2"+
            "\14\1\13\3\14\1\5\6\14\1\1\1\14\1\4\4\14\1\6\3\14\1\33\1\12"+
            "\1\34\1\7",
            "\1\40\2\uffff\1\37",
            "\1\41\6\uffff\1\42",
            "\1\43",
            "\1\44",
            "\1\46\7\uffff\1\45",
            "\1\47",
            "",
            "",
            "",
            "",
            "\1\51\1\uffff\1\50",
            "",
            "\1\16\1\uffff\12\15\13\uffff\1\16\37\uffff\1\16",
            "",
            "\1\53\4\uffff\1\53",
            "",
            "",
            "",
            "",
            "",
            "\1\55",
            "",
            "\1\57",
            "\1\61",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\73",
            "\1\74",
            "\1\75",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\105",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\12\14\7\uffff\22\14\1\116\7\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\120\17\uffff\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32"+
            "\14",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "",
            "\1\127",
            "",
            "",
            "",
            "\1\130",
            "\1\131",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\12\14\7\uffff\22\14\1\133\1\14\1\134\5\14\4\uffff\1\14\1\uffff"+
            "\32\14",
            "\1\136",
            "\1\137",
            "\1\140",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\142",
            "\1\143",
            "",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\145",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\1\147",
            "\1\150",
            "",
            "\1\151",
            "",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\156",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "\1\160",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "\12\14\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
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
            return "1:1: Tokens : ( T__50 | ALWAYS | BOOLEAN | REAL | INTEGER | WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_STABLE | ASSERT_UNTIL | POSEDGE | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA );";
        }
    }
 

}