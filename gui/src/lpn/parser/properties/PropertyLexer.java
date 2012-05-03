// $ANTLR 3.4 /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g 2012-05-02 21:17:52

package lpn.parser.properties;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class PropertyLexer extends Lexer {
    public static final int EOF=-1;
    public static final int AND=4;
    public static final int ASSERT=5;
    public static final int COMMA=6;
    public static final int COMMENT=7;
    public static final int DIV=8;
    public static final int ELSEIF=9;
    public static final int END=10;
    public static final int EQUAL=11;
    public static final int ESC_SEQ=12;
    public static final int EXPONENT=13;
    public static final int FLOAT=14;
    public static final int GET=15;
    public static final int GETEQ=16;
    public static final int HEX_DIGIT=17;
    public static final int ID=18;
    public static final int IF=19;
    public static final int INT=20;
    public static final int LCURL=21;
    public static final int LET=22;
    public static final int LETEQ=23;
    public static final int LPARA=24;
    public static final int MINUS=25;
    public static final int MOD=26;
    public static final int MULT=27;
    public static final int NOT=28;
    public static final int NOT_EQUAL=29;
    public static final int OCTAL_ESC=30;
    public static final int OR=31;
    public static final int PLUS=32;
    public static final int RCURL=33;
    public static final int RPARA=34;
    public static final int SAMEAS=35;
    public static final int SEMICOL=36;
    public static final int STRING=37;
    public static final int UNICODE_ESC=38;
    public static final int WAIT=39;
    public static final int WS=40;

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
    public String getGrammarFileName() { return "/Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g"; }

    // $ANTLR start "WAIT"
    public final void mWAIT() throws RecognitionException {
        try {
            int _type = WAIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:29:2: ( 'wait' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:29:2: 'wait'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:34:3: ( '~' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:34:3: '~'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:38:2: ( '%' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:38:2: '%'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:2: ( '&' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:2: '&'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: ( '|' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: '|'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:52:2: ( 'assert' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:52:2: 'assert'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:57:2: ( 'if' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:57:2: 'if'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:62:2: ( 'end' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:62:2: 'end'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:67:2: ( 'else if' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:67:2: 'else if'
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

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:71:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:71:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:71:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:74:5: ( ( '0' .. '9' )+ )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:74:7: ( '0' .. '9' )+
            {
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:74:7: ( '0' .. '9' )+
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
            	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:5: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT )
            int alt9=3;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )?
                    {
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:9: ( '0' .. '9' )+
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
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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

                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:25: ( '0' .. '9' )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:37: ( EXPONENT )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0=='E'||LA5_0=='e') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:78:37: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:9: '.' ( '0' .. '9' )+ ( EXPONENT )?
                    {
                    match('.'); 

                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:13: ( '0' .. '9' )+
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
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:25: ( EXPONENT )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='E'||LA7_0=='e') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:79:25: EXPONENT
                            {
                            mEXPONENT(); 


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:80:9: ( '0' .. '9' )+ EXPONENT
                    {
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:80:9: ( '0' .. '9' )+
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
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 



                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:14: (~ ( '\\n' | '\\r' ) )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0 >= '\u0000' && LA10_0 <= '\t')||(LA10_0 >= '\u000B' && LA10_0 <= '\f')||(LA10_0 >= '\u000E' && LA10_0 <= '\uFFFF')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:28: ( '\\r' )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0=='\r') ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:84:28: '\\r'
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:85:9: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 



                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:85:14: ( options {greedy=false; } : . )*
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
                    	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:85:42: .
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:88:5: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' ) )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:88:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
            {
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:88:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:88:11: ' '
                    {
                    match(' '); 

                    }
                    break;
                case 2 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:89:11: '\\t'
                    {
                    match('\t'); 

                    }
                    break;
                case 3 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:90:11: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:91:11: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 5 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:92:10: '\\r\\n'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:99:5: ( '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\'' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:99:8: '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\''
            {
            match('\''); 

            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:99:13: ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )*
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
            	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:99:15: ESC_SEQ
            	    {
            	    mESC_SEQ(); 


            	    }
            	    break;
            	case 2 :
            	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:99:25: ~ ( '\\\\' | '\\'' )
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:22: ( '+' | '-' )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='+'||LA16_0=='-') ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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


            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:33: ( '0' .. '9' )+
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
            	    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:108:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:112:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:112:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:113:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 


                    }
                    break;
                case 3 :
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:114:9: OCTAL_ESC
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:119:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:119:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:120:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
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
                    // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:121:9: '\\\\' ( '0' .. '7' )
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:132:2: ( '+' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:132:2: '+'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:136:3: ( '-' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:136:3: '-'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:141:2: ( '*' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:141:2: '*'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:145:2: ( '/' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:145:2: '/'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:151:2: ( '=' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:151:2: '='
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:156:2: ( '!=' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:156:2: '!='
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:2: ( '>' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:2: '>'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:164:2: ( '<' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:164:2: '<'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:168:2: ( '>=' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:168:2: '>='
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:171:2: ( '<=' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:171:2: '<='
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:176:2: ( '==' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:176:2: '=='
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:182:2: ( '(' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:182:2: '('
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:186:2: ( ')' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:186:2: ')'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:190:2: ( '{' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:190:2: '{'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:194:2: ( '}' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:194:2: '}'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:200:2: ( ';' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:200:2: ';'
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
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:204:2: ( ',' )
            // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:204:2: ','
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
        // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:8: ( WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA )
        int alt20=32;
        alt20 = dfa20.predict(input);
        switch (alt20) {
            case 1 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:10: WAIT
                {
                mWAIT(); 


                }
                break;
            case 2 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:15: NOT
                {
                mNOT(); 


                }
                break;
            case 3 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:19: MOD
                {
                mMOD(); 


                }
                break;
            case 4 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:23: AND
                {
                mAND(); 


                }
                break;
            case 5 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:27: OR
                {
                mOR(); 


                }
                break;
            case 6 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:30: ASSERT
                {
                mASSERT(); 


                }
                break;
            case 7 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:37: IF
                {
                mIF(); 


                }
                break;
            case 8 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:40: END
                {
                mEND(); 


                }
                break;
            case 9 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:44: ELSEIF
                {
                mELSEIF(); 


                }
                break;
            case 10 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:51: ID
                {
                mID(); 


                }
                break;
            case 11 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:54: INT
                {
                mINT(); 


                }
                break;
            case 12 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:58: FLOAT
                {
                mFLOAT(); 


                }
                break;
            case 13 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:64: COMMENT
                {
                mCOMMENT(); 


                }
                break;
            case 14 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:72: WS
                {
                mWS(); 


                }
                break;
            case 15 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:75: STRING
                {
                mSTRING(); 


                }
                break;
            case 16 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:82: PLUS
                {
                mPLUS(); 


                }
                break;
            case 17 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:87: MINUS
                {
                mMINUS(); 


                }
                break;
            case 18 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:93: MULT
                {
                mMULT(); 


                }
                break;
            case 19 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:98: DIV
                {
                mDIV(); 


                }
                break;
            case 20 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:102: EQUAL
                {
                mEQUAL(); 


                }
                break;
            case 21 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:108: NOT_EQUAL
                {
                mNOT_EQUAL(); 


                }
                break;
            case 22 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:118: GET
                {
                mGET(); 


                }
                break;
            case 23 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:122: LET
                {
                mLET(); 


                }
                break;
            case 24 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:126: GETEQ
                {
                mGETEQ(); 


                }
                break;
            case 25 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:132: LETEQ
                {
                mLETEQ(); 


                }
                break;
            case 26 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:138: SAMEAS
                {
                mSAMEAS(); 


                }
                break;
            case 27 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:145: LPARA
                {
                mLPARA(); 


                }
                break;
            case 28 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:151: RPARA
                {
                mRPARA(); 


                }
                break;
            case 29 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:157: LCURL
                {
                mLCURL(); 


                }
                break;
            case 30 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:163: RCURL
                {
                mRCURL(); 


                }
                break;
            case 31 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:169: SEMICOL
                {
                mSEMICOL(); 


                }
                break;
            case 32 :
                // /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:177: COMMA
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
            return "77:1: FLOAT : ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT );";
        }
    }
    static final String DFA20_eotS =
        "\1\uffff\1\11\4\uffff\3\11\1\uffff\1\41\1\uffff\1\43\5\uffff\1\45"+
        "\1\uffff\1\47\1\51\6\uffff\2\11\1\54\2\11\11\uffff\2\11\1\uffff"+
        "\1\61\1\11\1\63\1\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\67\1\uffff";
    static final String DFA20_eofS =
        "\70\uffff";
    static final String DFA20_minS =
        "\1\11\1\141\4\uffff\1\163\1\146\1\154\1\uffff\1\56\1\uffff\1\52"+
        "\5\uffff\1\75\1\uffff\2\75\6\uffff\1\151\1\163\1\60\1\144\1\163"+
        "\11\uffff\1\164\1\145\1\uffff\1\60\1\145\1\60\1\162\1\uffff\1\40"+
        "\1\uffff\1\164\1\uffff\1\60\1\uffff";
    static final String DFA20_maxS =
        "\1\176\1\141\4\uffff\1\163\1\146\1\156\1\uffff\1\145\1\uffff\1\57"+
        "\5\uffff\1\75\1\uffff\2\75\6\uffff\1\151\1\163\1\172\1\144\1\163"+
        "\11\uffff\1\164\1\145\1\uffff\1\172\1\145\1\172\1\162\1\uffff\1"+
        "\40\1\uffff\1\164\1\uffff\1\172\1\uffff";
    static final String DFA20_acceptS =
        "\2\uffff\1\2\1\3\1\4\1\5\3\uffff\1\12\1\uffff\1\14\1\uffff\1\16"+
        "\1\17\1\20\1\21\1\22\1\uffff\1\25\2\uffff\1\33\1\34\1\35\1\36\1"+
        "\37\1\40\5\uffff\1\13\1\15\1\23\1\32\1\24\1\30\1\26\1\31\1\27\2"+
        "\uffff\1\7\4\uffff\1\10\1\uffff\1\1\1\uffff\1\11\1\uffff\1\6";
    static final String DFA20_specialS =
        "\70\uffff}>";
    static final String[] DFA20_transitionS = {
            "\2\15\2\uffff\1\15\22\uffff\1\15\1\23\3\uffff\1\3\1\4\1\16\1"+
            "\26\1\27\1\21\1\17\1\33\1\20\1\13\1\14\12\12\1\uffff\1\32\1"+
            "\25\1\22\1\24\2\uffff\32\11\4\uffff\1\11\1\uffff\1\6\3\11\1"+
            "\10\3\11\1\7\15\11\1\1\3\11\1\30\1\5\1\31\1\2",
            "\1\34",
            "",
            "",
            "",
            "",
            "\1\35",
            "\1\36",
            "\1\40\1\uffff\1\37",
            "",
            "\1\13\1\uffff\12\12\13\uffff\1\13\37\uffff\1\13",
            "",
            "\1\42\4\uffff\1\42",
            "",
            "",
            "",
            "",
            "",
            "\1\44",
            "",
            "\1\46",
            "\1\50",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\52",
            "\1\53",
            "\12\11\7\uffff\32\11\4\uffff\1\11\1\uffff\32\11",
            "\1\55",
            "\1\56",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\57",
            "\1\60",
            "",
            "\12\11\7\uffff\32\11\4\uffff\1\11\1\uffff\32\11",
            "\1\62",
            "\12\11\7\uffff\32\11\4\uffff\1\11\1\uffff\32\11",
            "\1\64",
            "",
            "\1\65",
            "",
            "\1\66",
            "",
            "\12\11\7\uffff\32\11\4\uffff\1\11\1\uffff\32\11",
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
            return "1:1: Tokens : ( WAIT | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA );";
        }
    }
 

}