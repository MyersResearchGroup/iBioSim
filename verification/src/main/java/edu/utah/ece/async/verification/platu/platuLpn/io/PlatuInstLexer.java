/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g 2011-09-05 15:36:31

    package edu.utah.ece.async.verification.platu.platuLpn.io;


import org.antlr.runtime.*;

public class PlatuInstLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__73=73;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int PATH=4;
    public static final int ID=5;
    public static final int INT=6;
    public static final int MEMBER=7;
    public static final int LPAREN=8;
    public static final int RPAREN=9;
    public static final int TRUE=10;
    public static final int FALSE=11;
    public static final int QMARK=12;
    public static final int COLON=13;
    public static final int SEMICOLON=14;
    public static final int PERIOD=15;
    public static final int UNDERSCORE=16;
    public static final int COMMA=17;
    public static final int QUOTE=18;
    public static final int MODULE=19;
    public static final int NAME=20;
    public static final int INPUT=21;
    public static final int OUTPUT=22;
    public static final int INTERNAL=23;
    public static final int MARKING=24;
    public static final int STATE_VECTOR=25;
    public static final int TRANSITION=26;
    public static final int LABEL=27;
    public static final int PRESET=28;
    public static final int POSTSET=29;
    public static final int PLUS=30;
    public static final int MINUS=31;
    public static final int TIMES=32;
    public static final int DIV=33;
    public static final int MOD=34;
    public static final int EQUALS=35;
    public static final int GREATER=36;
    public static final int LESS=37;
    public static final int GREATER_EQUAL=38;
    public static final int LESS_EQUAL=39;
    public static final int EQUIV=40;
    public static final int NOT_EQUIV=41;
    public static final int NEGATION=42;
    public static final int AND=43;
    public static final int OR=44;
    public static final int IMPLICATION=45;
    public static final int BITWISE_NEGATION=46;
    public static final int BITWISE_AND=47;
    public static final int BITWISE_OR=48;
    public static final int BITWISE_XOR=49;
    public static final int BITWISE_LSHIFT=50;
    public static final int BITWISE_RSHIFT=51;
    public static final int LETTER=52;
    public static final int DIGIT=53;
    public static final int FILE=54;
    public static final int WS=55;
    public static final int COMMENT=56;
    public static final int MULTILINECOMMENT=57;
    public static final int XMLCOMMENT=58;
    public static final int IGNORE=59;

    // delegates
    // delegators

    public PlatuInstLexer() {} 
    public PlatuInstLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PlatuInstLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    @Override
	public String getGrammarFileName() { return "/Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g"; }

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:11:7: ( 'include' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:11:9: 'include'
            {
            match("include"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:12:7: ( '/include' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:12:9: '/include'
            {
            match("/include"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:13:7: ( 'main' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:13:9: 'main'
            {
            match("main"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:14:7: ( '/mod' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:14:9: '/mod'
            {
            match("/mod"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:15:7: ( 'process' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:15:9: 'process'
            {
            match("process"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:16:7: ( '/process' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:16:9: '/process'
            {
            match("/process"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:17:7: ( 'class' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:17:9: 'class'
            {
            match("class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:18:7: ( 'arg' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:18:9: 'arg'
            {
            match("arg"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__67"

    // $ANTLR start "T__68"
    public final void mT__68() throws RecognitionException {
        try {
            int _type = T__68;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:19:7: ( '[' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:19:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__68"

    // $ANTLR start "T__69"
    public final void mT__69() throws RecognitionException {
        try {
            int _type = T__69;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:20:7: ( ']' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:20:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__69"

    // $ANTLR start "T__70"
    public final void mT__70() throws RecognitionException {
        try {
            int _type = T__70;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:21:7: ( '/class' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:21:9: '/class'
            {
            match("/class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__70"

    // $ANTLR start "T__71"
    public final void mT__71() throws RecognitionException {
        try {
            int _type = T__71;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:22:7: ( 'const' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:22:9: 'const'
            {
            match("const"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__71"

    // $ANTLR start "T__72"
    public final void mT__72() throws RecognitionException {
        try {
            int _type = T__72;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:23:7: ( '/const' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:23:9: '/const'
            {
            match("/const"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__72"

    // $ANTLR start "T__73"
    public final void mT__73() throws RecognitionException {
        try {
            int _type = T__73;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:24:7: ( '/var' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:24:9: '/var'
            {
            match("/var"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__73"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:25:7: ( '{' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:25:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:26:7: ( '}' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:26:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:27:7: ( '/marking' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:27:9: '/marking'
            {
            match("/marking"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "T__77"
    public final void mT__77() throws RecognitionException {
        try {
            int _type = T__77;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:28:7: ( '/transition' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:28:9: '/transition'
            {
            match("/transition"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__77"

    // $ANTLR start "T__78"
    public final void mT__78() throws RecognitionException {
        try {
            int _type = T__78;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:29:7: ( 'assert' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:29:9: 'assert'
            {
            match("assert"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__78"

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:30:7: ( 'condition' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:30:9: 'condition'
            {
            match("condition"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:31:7: ( 'delay' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:31:9: 'delay'
            {
            match("delay"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:32:7: ( 'inf' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:32:9: 'inf'
            {
            match("inf"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1426:7: ( '(' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1426:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1427:7: ( ')' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1427:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "QMARK"
    public final void mQMARK() throws RecognitionException {
        try {
            int _type = QMARK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1428:6: ( '?' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1428:8: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QMARK"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1429:6: ( ':' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1429:8: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "SEMICOLON"
    public final void mSEMICOLON() throws RecognitionException {
        try {
            int _type = SEMICOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1430:10: ( ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1430:12: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMICOLON"

    // $ANTLR start "PERIOD"
    public final void mPERIOD() throws RecognitionException {
        try {
            int _type = PERIOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1431:7: ( '.' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1431:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERIOD"

    // $ANTLR start "UNDERSCORE"
    public final void mUNDERSCORE() throws RecognitionException {
        try {
            int _type = UNDERSCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1432:11: ( '_' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1432:13: '_'
            {
            match('_'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNDERSCORE"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1433:6: ( ',' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1433:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "QUOTE"
    public final void mQUOTE() throws RecognitionException {
        try {
            int _type = QUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1434:6: ( '\"' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1434:8: '\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTE"

    // $ANTLR start "MODULE"
    public final void mMODULE() throws RecognitionException {
        try {
            int _type = MODULE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1437:7: ( 'mod' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1437:9: 'mod'
            {
            match("mod"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MODULE"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1438:5: ( 'name' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1438:7: 'name'
            {
            match("name"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "INPUT"
    public final void mINPUT() throws RecognitionException {
        try {
            int _type = INPUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1439:6: ( 'input' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1439:8: 'input'
            {
            match("input"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INPUT"

    // $ANTLR start "OUTPUT"
    public final void mOUTPUT() throws RecognitionException {
        try {
            int _type = OUTPUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1440:7: ( 'output' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1440:9: 'output'
            {
            match("output"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OUTPUT"

    // $ANTLR start "INTERNAL"
    public final void mINTERNAL() throws RecognitionException {
        try {
            int _type = INTERNAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1441:9: ( 'var' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1441:11: 'var'
            {
            match("var"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTERNAL"

    // $ANTLR start "MARKING"
    public final void mMARKING() throws RecognitionException {
        try {
            int _type = MARKING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1442:8: ( 'marking' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1442:10: 'marking'
            {
            match("marking"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MARKING"

    // $ANTLR start "STATE_VECTOR"
    public final void mSTATE_VECTOR() throws RecognitionException {
        try {
            int _type = STATE_VECTOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1443:13: ( 'statevector' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1443:15: 'statevector'
            {
            match("statevector"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STATE_VECTOR"

    // $ANTLR start "TRANSITION"
    public final void mTRANSITION() throws RecognitionException {
        try {
            int _type = TRANSITION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1444:11: ( 'transition' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1444:13: 'transition'
            {
            match("transition"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRANSITION"

    // $ANTLR start "LABEL"
    public final void mLABEL() throws RecognitionException {
        try {
            int _type = LABEL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1445:6: ( 'label' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1445:8: 'label'
            {
            match("label"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LABEL"

    // $ANTLR start "PRESET"
    public final void mPRESET() throws RecognitionException {
        try {
            int _type = PRESET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1446:7: ( 'preset' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1446:9: 'preset'
            {
            match("preset"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PRESET"

    // $ANTLR start "POSTSET"
    public final void mPOSTSET() throws RecognitionException {
        try {
            int _type = POSTSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1447:8: ( 'postset' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1447:10: 'postset'
            {
            match("postset"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "POSTSET"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1448:5: ( 'true' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1448:7: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1449:6: ( 'false' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1449:8: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1452:5: ( '+' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1452:7: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1453:6: ( '-' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1453:8: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "TIMES"
    public final void mTIMES() throws RecognitionException {
        try {
            int _type = TIMES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1454:6: ( '*' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1454:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TIMES"

    // $ANTLR start "DIV"
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1455:4: ( '/' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1455:6: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIV"

    // $ANTLR start "MOD"
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1456:4: ( '%' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1456:6: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOD"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1457:7: ( '=' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1457:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1460:8: ( '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1460:10: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1461:5: ( '<' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1461:7: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "GREATER_EQUAL"
    public final void mGREATER_EQUAL() throws RecognitionException {
        try {
            int _type = GREATER_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1462:14: ( '>=' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1462:16: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER_EQUAL"

    // $ANTLR start "LESS_EQUAL"
    public final void mLESS_EQUAL() throws RecognitionException {
        try {
            int _type = LESS_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1463:11: ( '<=' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1463:13: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_EQUAL"

    // $ANTLR start "EQUIV"
    public final void mEQUIV() throws RecognitionException {
        try {
            int _type = EQUIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1464:6: ( '==' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1464:8: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUIV"

    // $ANTLR start "NOT_EQUIV"
    public final void mNOT_EQUIV() throws RecognitionException {
        try {
            int _type = NOT_EQUIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1465:10: ( '!=' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1465:12: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT_EQUIV"

    // $ANTLR start "NEGATION"
    public final void mNEGATION() throws RecognitionException {
        try {
            int _type = NEGATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1468:9: ( '!' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1468:11: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEGATION"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1469:4: ( '&&' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1469:6: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1470:3: ( '||' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1470:5: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "IMPLICATION"
    public final void mIMPLICATION() throws RecognitionException {
        try {
            int _type = IMPLICATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1471:12: ( '->' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1471:14: '->'
            {
            match("->"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPLICATION"

    // $ANTLR start "BITWISE_NEGATION"
    public final void mBITWISE_NEGATION() throws RecognitionException {
        try {
            int _type = BITWISE_NEGATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1474:17: ( '~' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1474:19: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_NEGATION"

    // $ANTLR start "BITWISE_AND"
    public final void mBITWISE_AND() throws RecognitionException {
        try {
            int _type = BITWISE_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1475:12: ( '&' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1475:14: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_AND"

    // $ANTLR start "BITWISE_OR"
    public final void mBITWISE_OR() throws RecognitionException {
        try {
            int _type = BITWISE_OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1476:11: ( '|' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1476:13: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_OR"

    // $ANTLR start "BITWISE_XOR"
    public final void mBITWISE_XOR() throws RecognitionException {
        try {
            int _type = BITWISE_XOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1477:12: ( '^' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1477:14: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_XOR"

    // $ANTLR start "BITWISE_LSHIFT"
    public final void mBITWISE_LSHIFT() throws RecognitionException {
        try {
            int _type = BITWISE_LSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1478:15: ( '<<' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1478:17: '<<'
            {
            match("<<"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_LSHIFT"

    // $ANTLR start "BITWISE_RSHIFT"
    public final void mBITWISE_RSHIFT() throws RecognitionException {
        try {
            int _type = BITWISE_RSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1479:15: ( '>>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1479:17: '>>'
            {
            match(">>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISE_RSHIFT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1489:16: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1489:18: ( 'a' .. 'z' | 'A' .. 'Z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1490:15: ( '0' .. '9' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1490:17: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "FILE"
    public final void mFILE() throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:14: ( ( LETTER | DIGIT ) ( ( '_' )? ( LETTER | DIGIT ) )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:16: ( LETTER | DIGIT ) ( ( '_' )? ( LETTER | DIGIT ) )*
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:33: ( ( '_' )? ( LETTER | DIGIT ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:34: ( '_' )? ( LETTER | DIGIT )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:34: ( '_' )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0=='_') ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1491:34: '_'
            	            {
            	            match('_'); 

            	            }
            	            break;

            	    }

            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "FILE"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:4: ( ( '-' )? ( DIGIT )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:6: ( '-' )? ( DIGIT )+
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:6: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:6: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:11: ( DIGIT )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1492:11: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:3: ( LETTER ( ( UNDERSCORE )? ( LETTER | DIGIT ) )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:5: LETTER ( ( UNDERSCORE )? ( LETTER | DIGIT ) )*
            {
            mLETTER(); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:12: ( ( UNDERSCORE )? ( LETTER | DIGIT ) )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')||(LA6_0>='A' && LA6_0<='Z')||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:13: ( UNDERSCORE )? ( LETTER | DIGIT )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:13: ( UNDERSCORE )?
            	    int alt5=2;
            	    int LA5_0 = input.LA(1);

            	    if ( (LA5_0=='_') ) {
            	        alt5=1;
            	    }
            	    switch (alt5) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1493:13: UNDERSCORE
            	            {
            	            mUNDERSCORE(); 

            	            }
            	            break;

            	    }

            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "PATH"
    public final void mPATH() throws RecognitionException {
        try {
            int _type = PATH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:5: ( ( ( LETTER ':' ) | '/' )? ( FILE ( '/' | '\\\\' ) )* FILE '.lpn' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:7: ( ( LETTER ':' ) | '/' )? ( FILE ( '/' | '\\\\' ) )* FILE '.lpn'
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:7: ( ( LETTER ':' ) | '/' )?
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>='A' && LA7_0<='Z')||(LA7_0>='a' && LA7_0<='z')) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==':') ) {
                    alt7=1;
                }
            }
            else if ( (LA7_0=='/') ) {
                alt7=2;
            }
            switch (alt7) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:8: ( LETTER ':' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:8: ( LETTER ':' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:9: LETTER ':'
                    {
                    mLETTER(); 
                    match(':'); 

                    }


                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:23: '/'
                    {
                    match('/'); 

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:29: ( FILE ( '/' | '\\\\' ) )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1494:30: FILE ( '/' | '\\\\' )
            	    {
            	    mFILE(); 
            	    if ( input.LA(1)=='/'||input.LA(1)=='\\' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            mFILE(); 
            match(".lpn"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PATH"

    // $ANTLR start "MEMBER"
    public final void mMEMBER() throws RecognitionException {
        try {
            int _type = MEMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1495:7: ( ID '.' ID )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1495:9: ID '.' ID
            {
            mID(); 
            match('.'); 
            mID(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MEMBER"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1496:3: ( ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1496:5: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1496:5: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\t' && LA9_0<='\n')||(LA9_0>='\f' && LA9_0<='\r')||LA9_0==' ') ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);

            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1497:8: ( '//' ( . )* ( '\\n' | '\\r' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1497:10: '//' ( . )* ( '\\n' | '\\r' )
            {
            match("//"); 

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1497:15: ( . )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='\n'||LA10_0=='\r') ) {
                    alt10=2;
                }
                else if ( ((LA10_0>='\u0000' && LA10_0<='\t')||(LA10_0>='\u000B' && LA10_0<='\f')||(LA10_0>='\u000E' && LA10_0<='\uFFFF')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1497:15: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "MULTILINECOMMENT"
    public final void mMULTILINECOMMENT() throws RecognitionException {
        try {
            int _type = MULTILINECOMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1498:17: ( '/*' ( . )* '*/' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1498:19: '/*' ( . )* '*/'
            {
            match("/*"); 

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1498:24: ( . )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0=='*') ) {
                    int LA11_1 = input.LA(2);

                    if ( (LA11_1=='/') ) {
                        alt11=2;
                    }
                    else if ( ((LA11_1>='\u0000' && LA11_1<='.')||(LA11_1>='0' && LA11_1<='\uFFFF')) ) {
                        alt11=1;
                    }


                }
                else if ( ((LA11_0>='\u0000' && LA11_0<=')')||(LA11_0>='+' && LA11_0<='\uFFFF')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1498:24: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            match("*/"); 

            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTILINECOMMENT"

    // $ANTLR start "XMLCOMMENT"
    public final void mXMLCOMMENT() throws RecognitionException {
        try {
            int _type = XMLCOMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:11: ( ( '<' '!' '-' '-' ) ( . )* ( '-' '-' '>' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:13: ( '<' '!' '-' '-' ) ( . )* ( '-' '-' '>' )
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:13: ( '<' '!' '-' '-' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:14: '<' '!' '-' '-'
            {
            match('<'); 
            match('!'); 
            match('-'); 
            match('-'); 

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:31: ( . )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='-') ) {
                    int LA12_1 = input.LA(2);

                    if ( (LA12_1=='-') ) {
                        int LA12_3 = input.LA(3);

                        if ( (LA12_3=='>') ) {
                            alt12=2;
                        }
                        else if ( ((LA12_3>='\u0000' && LA12_3<='=')||(LA12_3>='?' && LA12_3<='\uFFFF')) ) {
                            alt12=1;
                        }


                    }
                    else if ( ((LA12_1>='\u0000' && LA12_1<=',')||(LA12_1>='.' && LA12_1<='\uFFFF')) ) {
                        alt12=1;
                    }


                }
                else if ( ((LA12_0>='\u0000' && LA12_0<=',')||(LA12_0>='.' && LA12_0<='\uFFFF')) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:31: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:34: ( '-' '-' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1499:35: '-' '-' '>'
            {
            match('-'); 
            match('-'); 
            match('>'); 

            }

            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XMLCOMMENT"

    // $ANTLR start "IGNORE"
    public final void mIGNORE() throws RecognitionException {
        try {
            int _type = IGNORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1500:7: ( '<' '?' ( . )* '?' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1500:9: '<' '?' ( . )* '?' '>'
            {
            match('<'); 
            match('?'); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1500:17: ( . )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0=='?') ) {
                    int LA13_1 = input.LA(2);

                    if ( (LA13_1=='>') ) {
                        alt13=2;
                    }
                    else if ( ((LA13_1>='\u0000' && LA13_1<='=')||(LA13_1>='?' && LA13_1<='\uFFFF')) ) {
                        alt13=1;
                    }


                }
                else if ( ((LA13_0>='\u0000' && LA13_0<='>')||(LA13_0>='@' && LA13_0<='\uFFFF')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1500:17: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            match('?'); 
            match('>'); 
            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IGNORE"

    @Override
	public void mTokens() throws RecognitionException {
        // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:8: ( T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | LPAREN | RPAREN | QMARK | COLON | SEMICOLON | PERIOD | UNDERSCORE | COMMA | QUOTE | MODULE | NAME | INPUT | OUTPUT | INTERNAL | MARKING | STATE_VECTOR | TRANSITION | LABEL | PRESET | POSTSET | TRUE | FALSE | PLUS | MINUS | TIMES | DIV | MOD | EQUALS | GREATER | LESS | GREATER_EQUAL | LESS_EQUAL | EQUIV | NOT_EQUIV | NEGATION | AND | OR | IMPLICATION | BITWISE_NEGATION | BITWISE_AND | BITWISE_OR | BITWISE_XOR | BITWISE_LSHIFT | BITWISE_RSHIFT | INT | ID | PATH | MEMBER | WS | COMMENT | MULTILINECOMMENT | XMLCOMMENT | IGNORE )
        int alt14=75;
        alt14 = dfa14.predict(input);
        switch (alt14) {
            case 1 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:10: T__60
                {
                mT__60(); 

                }
                break;
            case 2 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:16: T__61
                {
                mT__61(); 

                }
                break;
            case 3 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:22: T__62
                {
                mT__62(); 

                }
                break;
            case 4 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:28: T__63
                {
                mT__63(); 

                }
                break;
            case 5 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:34: T__64
                {
                mT__64(); 

                }
                break;
            case 6 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:40: T__65
                {
                mT__65(); 

                }
                break;
            case 7 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:46: T__66
                {
                mT__66(); 

                }
                break;
            case 8 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:52: T__67
                {
                mT__67(); 

                }
                break;
            case 9 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:58: T__68
                {
                mT__68(); 

                }
                break;
            case 10 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:64: T__69
                {
                mT__69(); 

                }
                break;
            case 11 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:70: T__70
                {
                mT__70(); 

                }
                break;
            case 12 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:76: T__71
                {
                mT__71(); 

                }
                break;
            case 13 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:82: T__72
                {
                mT__72(); 

                }
                break;
            case 14 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:88: T__73
                {
                mT__73(); 

                }
                break;
            case 15 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:94: T__74
                {
                mT__74(); 

                }
                break;
            case 16 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:100: T__75
                {
                mT__75(); 

                }
                break;
            case 17 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:106: T__76
                {
                mT__76(); 

                }
                break;
            case 18 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:112: T__77
                {
                mT__77(); 

                }
                break;
            case 19 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:118: T__78
                {
                mT__78(); 

                }
                break;
            case 20 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:124: T__79
                {
                mT__79(); 

                }
                break;
            case 21 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:130: T__80
                {
                mT__80(); 

                }
                break;
            case 22 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:136: T__81
                {
                mT__81(); 

                }
                break;
            case 23 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:142: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 24 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:149: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 25 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:156: QMARK
                {
                mQMARK(); 

                }
                break;
            case 26 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:162: COLON
                {
                mCOLON(); 

                }
                break;
            case 27 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:168: SEMICOLON
                {
                mSEMICOLON(); 

                }
                break;
            case 28 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:178: PERIOD
                {
                mPERIOD(); 

                }
                break;
            case 29 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:185: UNDERSCORE
                {
                mUNDERSCORE(); 

                }
                break;
            case 30 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:196: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 31 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:202: QUOTE
                {
                mQUOTE(); 

                }
                break;
            case 32 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:208: MODULE
                {
                mMODULE(); 

                }
                break;
            case 33 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:215: NAME
                {
                mNAME(); 

                }
                break;
            case 34 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:220: INPUT
                {
                mINPUT(); 

                }
                break;
            case 35 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:226: OUTPUT
                {
                mOUTPUT(); 

                }
                break;
            case 36 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:233: INTERNAL
                {
                mINTERNAL(); 

                }
                break;
            case 37 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:242: MARKING
                {
                mMARKING(); 

                }
                break;
            case 38 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:250: STATE_VECTOR
                {
                mSTATE_VECTOR(); 

                }
                break;
            case 39 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:263: TRANSITION
                {
                mTRANSITION(); 

                }
                break;
            case 40 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:274: LABEL
                {
                mLABEL(); 

                }
                break;
            case 41 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:280: PRESET
                {
                mPRESET(); 

                }
                break;
            case 42 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:287: POSTSET
                {
                mPOSTSET(); 

                }
                break;
            case 43 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:295: TRUE
                {
                mTRUE(); 

                }
                break;
            case 44 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:300: FALSE
                {
                mFALSE(); 

                }
                break;
            case 45 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:306: PLUS
                {
                mPLUS(); 

                }
                break;
            case 46 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:311: MINUS
                {
                mMINUS(); 

                }
                break;
            case 47 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:317: TIMES
                {
                mTIMES(); 

                }
                break;
            case 48 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:323: DIV
                {
                mDIV(); 

                }
                break;
            case 49 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:327: MOD
                {
                mMOD(); 

                }
                break;
            case 50 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:331: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 51 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:338: GREATER
                {
                mGREATER(); 

                }
                break;
            case 52 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:346: LESS
                {
                mLESS(); 

                }
                break;
            case 53 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:351: GREATER_EQUAL
                {
                mGREATER_EQUAL(); 

                }
                break;
            case 54 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:365: LESS_EQUAL
                {
                mLESS_EQUAL(); 

                }
                break;
            case 55 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:376: EQUIV
                {
                mEQUIV(); 

                }
                break;
            case 56 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:382: NOT_EQUIV
                {
                mNOT_EQUIV(); 

                }
                break;
            case 57 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:392: NEGATION
                {
                mNEGATION(); 

                }
                break;
            case 58 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:401: AND
                {
                mAND(); 

                }
                break;
            case 59 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:405: OR
                {
                mOR(); 

                }
                break;
            case 60 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:408: IMPLICATION
                {
                mIMPLICATION(); 

                }
                break;
            case 61 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:420: BITWISE_NEGATION
                {
                mBITWISE_NEGATION(); 

                }
                break;
            case 62 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:437: BITWISE_AND
                {
                mBITWISE_AND(); 

                }
                break;
            case 63 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:449: BITWISE_OR
                {
                mBITWISE_OR(); 

                }
                break;
            case 64 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:460: BITWISE_XOR
                {
                mBITWISE_XOR(); 

                }
                break;
            case 65 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:472: BITWISE_LSHIFT
                {
                mBITWISE_LSHIFT(); 

                }
                break;
            case 66 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:487: BITWISE_RSHIFT
                {
                mBITWISE_RSHIFT(); 

                }
                break;
            case 67 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:502: INT
                {
                mINT(); 

                }
                break;
            case 68 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:506: ID
                {
                mID(); 

                }
                break;
            case 69 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:509: PATH
                {
                mPATH(); 

                }
                break;
            case 70 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:514: MEMBER
                {
                mMEMBER(); 

                }
                break;
            case 71 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:521: WS
                {
                mWS(); 

                }
                break;
            case 72 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:524: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 73 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:532: MULTILINECOMMENT
                {
                mMULTILINECOMMENT(); 

                }
                break;
            case 74 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:549: XMLCOMMENT
                {
                mXMLCOMMENT(); 

                }
                break;
            case 75 :
                // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1:560: IGNORE
                {
                mIGNORE(); 

                }
                break;

        }

    }


    protected DFA8 dfa8 = new DFA8(this);
    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA8_eotS =
        "\6\uffff";
    static final String DFA8_eofS =
        "\6\uffff";
    static final String DFA8_minS =
        "\1\60\1\56\1\60\1\56\2\uffff";
    static final String DFA8_maxS =
        "\4\172\2\uffff";
    static final String DFA8_acceptS =
        "\4\uffff\1\2\1\1";
    static final String DFA8_specialS =
        "\6\uffff}>";
    static final String[] DFA8_transitionS = {
            "\12\1\7\uffff\32\1\6\uffff\32\1",
            "\1\4\1\5\12\3\7\uffff\32\3\1\uffff\1\5\2\uffff\1\2\1\uffff"+
            "\32\3",
            "\12\3\7\uffff\32\3\6\uffff\32\3",
            "\1\4\1\5\12\3\7\uffff\32\3\1\uffff\1\5\2\uffff\1\2\1\uffff"+
            "\32\3",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        @Override
		public String getDescription() {
            return "()* loopback of 1494:29: ( FILE ( '/' | '\\\\' ) )*";
        }
    }
    static final String DFA14_eotS =
        "\1\uffff\1\54\1\71\4\54\4\uffff\1\54\11\uffff\7\54\1\uffff\1\113"+
        "\2\uffff\1\116\1\121\1\126\1\130\1\132\1\134\2\uffff\1\114\1\54"+
        "\1\uffff\1\54\2\uffff\1\54\13\uffff\20\54\23\uffff\1\114\1\54\1"+
        "\177\1\54\1\142\11\uffff\2\54\1\u008c\5\54\1\u0093\4\54\1\u0098"+
        "\6\54\1\uffff\1\54\1\142\1\uffff\1\u00a2\4\uffff\1\u00a7\1\uffff"+
        "\1\u00a9\1\54\1\uffff\6\54\1\uffff\2\54\1\u00b3\1\54\1\uffff\2\54"+
        "\1\u00b7\3\54\1\u00bb\1\57\11\uffff\4\54\1\u00c6\1\u00c7\2\54\1"+
        "\u00ca\1\uffff\3\54\1\uffff\1\u00ce\1\u00cf\1\54\4\uffff\1\u00d4"+
        "\1\u00d5\1\uffff\2\54\1\u00d9\1\54\2\uffff\1\54\1\u00dc\1\uffff"+
        "\1\u00dd\2\54\2\uffff\1\u00e0\6\uffff\1\u00e5\1\u00e6\1\uffff\1"+
        "\u00e7\1\54\2\uffff\2\54\1\uffff\1\u00eb\1\u00ec\1\u00ed\4\uffff"+
        "\3\54\4\uffff\1\u00f3\2\54\2\uffff\1\54\1\u00f8\1\u00f9\1\u00fa"+
        "\3\uffff";
    static final String DFA14_eofS =
        "\u00fb\uffff";
    static final String DFA14_minS =
        "\1\11\1\56\1\52\4\56\4\uffff\1\56\11\uffff\7\56\1\uffff\1\60\2\uffff"+
        "\2\75\1\41\1\75\1\46\1\174\2\uffff\2\56\1\uffff\1\56\1\uffff\1\60"+
        "\1\56\1\uffff\1\101\6\56\3\uffff\20\56\23\uffff\4\56\1\160\1\uffff"+
        "\34\56\1\uffff\1\56\1\156\12\56\1\uffff\6\56\1\uffff\4\56\1\uffff"+
        "\7\56\1\60\1\56\1\uffff\4\56\1\uffff\1\56\1\uffff\11\56\1\uffff"+
        "\3\56\1\uffff\3\56\1\uffff\12\56\2\uffff\2\56\1\uffff\3\56\2\uffff"+
        "\4\56\2\uffff\3\56\1\uffff\2\56\2\uffff\2\56\1\uffff\4\56\3\uffff"+
        "\3\56\3\uffff\5\56\1\uffff\4\56\3\uffff";
    static final String DFA14_maxS =
        "\1\176\6\172\4\uffff\1\172\11\uffff\7\172\1\uffff\1\76\2\uffff\1"+
        "\75\1\76\1\77\1\75\1\46\1\174\2\uffff\2\172\1\uffff\1\172\1\uffff"+
        "\2\172\1\uffff\7\172\3\uffff\20\172\23\uffff\4\172\1\160\1\uffff"+
        "\34\172\1\uffff\1\172\1\156\12\172\1\uffff\6\172\1\uffff\4\172\1"+
        "\uffff\11\172\1\uffff\4\172\1\uffff\1\172\1\uffff\11\172\1\uffff"+
        "\3\172\1\uffff\3\172\1\uffff\12\172\2\uffff\2\172\1\uffff\3\172"+
        "\2\uffff\4\172\2\uffff\3\172\1\uffff\2\172\2\uffff\2\172\1\uffff"+
        "\4\172\3\uffff\3\172\3\uffff\5\172\1\uffff\4\172\3\uffff";
    static final String DFA14_acceptS =
        "\7\uffff\1\11\1\12\1\17\1\20\1\uffff\1\27\1\30\1\31\1\32\1\33\1"+
        "\34\1\35\1\36\1\37\7\uffff\1\55\1\uffff\1\57\1\61\6\uffff\1\75\1"+
        "\100\2\uffff\1\107\1\uffff\1\104\2\uffff\1\105\7\uffff\1\110\1\111"+
        "\1\60\20\uffff\1\74\1\56\1\103\1\67\1\62\1\65\1\102\1\63\1\66\1"+
        "\101\1\112\1\113\1\64\1\70\1\71\1\72\1\76\1\73\1\77\5\uffff\1\106"+
        "\34\uffff\1\26\14\uffff\1\40\6\uffff\1\10\4\uffff\1\44\11\uffff"+
        "\1\4\4\uffff\1\16\1\uffff\1\3\11\uffff\1\41\3\uffff\1\53\3\uffff"+
        "\1\42\12\uffff\1\7\1\14\2\uffff\1\25\3\uffff\1\50\1\54\4\uffff\1"+
        "\13\1\15\3\uffff\1\51\2\uffff\1\23\1\43\2\uffff\1\1\4\uffff\1\45"+
        "\1\5\1\52\3\uffff\1\2\1\21\1\6\5\uffff\1\24\4\uffff\1\47\1\22\1"+
        "\46";
    static final String DFA14_specialS =
        "\u00fb\uffff}>";
    static final String[] DFA14_transitionS = {
            "\2\52\1\uffff\2\52\22\uffff\1\52\1\43\1\24\2\uffff\1\37\1\44"+
            "\1\uffff\1\14\1\15\1\36\1\34\1\23\1\35\1\21\1\2\12\50\1\17\1"+
            "\20\1\42\1\40\1\41\1\16\1\uffff\32\51\1\7\1\uffff\1\10\1\47"+
            "\1\22\1\uffff\1\6\1\51\1\5\1\13\1\51\1\33\2\51\1\1\2\51\1\32"+
            "\1\3\1\25\1\26\1\4\2\51\1\30\1\31\1\51\1\27\4\51\1\11\1\45\1"+
            "\12\1\46",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\15\56\1\53\14\56",
            "\1\70\4\uffff\1\67\12\57\7\uffff\32\57\6\uffff\2\57\1\64\5"+
            "\57\1\61\3\57\1\62\2\57\1\63\3\57\1\66\1\57\1\65\4\57",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\1\72\15\56\1\73\13\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\16\56\1\75\2\56\1\74\10\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\13\56\1\76\2\56\1\77\13\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\21\56\1\100\1\101\7\56",
            "",
            "",
            "",
            "",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\4\56\1\102\25\56",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\1\103\31\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\24\56\1\104\5\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\1\105\31\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\23\56\1\106\6\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\21\56\1\107\10\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\1\110\31\56",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\1\111\31\56",
            "",
            "\12\114\4\uffff\1\112",
            "",
            "",
            "\1\115",
            "\1\117\1\120",
            "\1\124\32\uffff\1\123\1\122\1\uffff\1\125",
            "\1\127",
            "\1\131",
            "\1\133",
            "",
            "",
            "\2\57\12\135\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff"+
            "\32\57",
            "\1\60\1\57\12\56\1\57\6\uffff\32\56\1\uffff\1\57\2\uffff\1"+
            "\55\1\uffff\32\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\2\56\1\136\2\56\1\137\11\56\1\140\12\56",
            "",
            "\12\56\7\uffff\32\56\6\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "",
            "\32\142\6\uffff\13\142\1\141\16\142",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\15\57"+
            "\1\143\14\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\1\145"+
            "\15\57\1\144\13\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\21\57"+
            "\1\146\10\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\13\57"+
            "\1\147\2\57\1\150\13\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\1\151"+
            "\31\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\21\57"+
            "\1\152\10\57",
            "",
            "",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\153\10\56\1\154\10\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\3\56\1\155\26\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\157\11\56\1\156\13\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\160\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\1\161\31\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\162\14\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\6\56\1\163\23\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\164\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\13\56\1\165\16\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\14\56\1\166\15\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\167\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\21\56\1\170\10\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\1\171\31\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\1\172\23\56\1\173\5\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\1\56\1\174\30\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\13\56\1\175\16\56",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\57\12\135\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff"+
            "\32\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\13\56\1\176\16\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\24\56\1\u0080\5\56",
            "\1\u0081",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\2\57"+
            "\1\u0082\27\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\3\57"+
            "\1\u0083\26\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\21\57"+
            "\1\u0084\10\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\16\57"+
            "\1\u0085\13\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\1\u0086"+
            "\31\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\15\57"+
            "\1\u0087\14\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\21\57"+
            "\1\u0088\10\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\1\u0089"+
            "\31\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\u008a\14\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\12\56\1\u008b\17\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\2\56\1\u008d\27\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u008e\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u008f\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u0090\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\3\56\1\u0092\16\56\1\u0091\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u0094\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\1\u0095\31\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u0096\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\17\56\1\u0097\12\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u0099\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\u009a\14\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u009b\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u009c\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u009d\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\24\56\1\u009e\5\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u009f\6\56",
            "\1\u00a0",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\13\57"+
            "\1\u00a1\16\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\12\57"+
            "\1\u00a3\17\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\2\57"+
            "\1\u00a4\27\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00a5\7\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00a6\7\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\15\57"+
            "\1\u00a8\14\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\u00aa\21\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00ab\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00ac\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u00ad\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u00ae\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00af\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\u00b0\21\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\21\56\1\u00b1\10\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\30\56\1\u00b2\1\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\24\56\1\u00b4\5\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00b5\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u00b6\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\13\56\1\u00b8\16\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00b9\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\3\56\1\u00ba\26\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\12\142\7\uffff\32\142\4\uffff\1\142\1\uffff\32\142",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\24\57"+
            "\1\u00bc\5\57",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\10\57"+
            "\1\u00bd\21\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\4\57"+
            "\1\u00be\25\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00bf\7\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\23\57"+
            "\1\u00c0\6\57",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00c1\7\57",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\u00c2\14\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u00c3\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00c4\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00c5\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00c8\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00c9\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00cb\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\25\56\1\u00cc\4\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\u00cd\21\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00d0\25\56",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\3\57"+
            "\1\u00d1\26\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\15\57"+
            "\1\u00d2\14\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00d3\7\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\10\57"+
            "\1\u00d6\21\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\6\56\1\u00d7\23\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\22\56\1\u00d8\7\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00da\6\56",
            "",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\u00db\21\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\4\56\1\u00de\25\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00df\6\56",
            "",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\4\57"+
            "\1\u00e1\25\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\6\57"+
            "\1\u00e2\23\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\22\57"+
            "\1\u00e3\7\57",
            "",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\23\57"+
            "\1\u00e4\6\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\16\56\1\u00e8\13\56",
            "",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\2\56\1\u00e9\27\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\10\56\1\u00ea\21\56",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\10\57"+
            "\1\u00ee\21\57",
            "",
            "",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\u00ef\14\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\23\56\1\u00f0\6\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\16\56\1\u00f1\13\56",
            "",
            "",
            "",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\16\57"+
            "\1\u00f2\13\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\16\56\1\u00f4\13\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\15\56\1\u00f5\14\56",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\15\57"+
            "\1\u00f6\14\57",
            "",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\21\56\1\u00f7\10\56",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "\14\57\7\uffff\32\57\1\uffff\1\57\2\uffff\1\57\1\uffff\32\57",
            "\1\60\1\57\12\56\7\uffff\32\56\1\uffff\1\57\2\uffff\1\55\1"+
            "\uffff\32\56",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        @Override
		public String getDescription() {
            return "1:1: Tokens : ( T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | LPAREN | RPAREN | QMARK | COLON | SEMICOLON | PERIOD | UNDERSCORE | COMMA | QUOTE | MODULE | NAME | INPUT | OUTPUT | INTERNAL | MARKING | STATE_VECTOR | TRANSITION | LABEL | PRESET | POSTSET | TRUE | FALSE | PLUS | MINUS | TIMES | DIV | MOD | EQUALS | GREATER | LESS | GREATER_EQUAL | LESS_EQUAL | EQUIV | NOT_EQUIV | NEGATION | AND | OR | IMPLICATION | BITWISE_NEGATION | BITWISE_AND | BITWISE_OR | BITWISE_XOR | BITWISE_LSHIFT | BITWISE_RSHIFT | INT | ID | PATH | MEMBER | WS | COMMENT | MULTILINECOMMENT | XMLCOMMENT | IGNORE );";
        }
    }
 

}