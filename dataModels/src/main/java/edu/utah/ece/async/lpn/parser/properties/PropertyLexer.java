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
// $ANTLR 3.5 /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g 2016-09-01 11:02:46
 
package edu.utah.ece.async.lpn.parser.properties;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
@SuppressWarnings("all")
public class PropertyLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__54=54;
	public static final int ALWAYS=4;
	public static final int AND=5;
	public static final int ASSERT=6;
	public static final int ASSERT_STABLE=7;
	public static final int ASSERT_UNTIL=8;
	public static final int BOOLEAN=9;
	public static final int COMMA=10;
	public static final int COMMENT=11;
	public static final int DASH=12;
	public static final int DIV=13;
	public static final int ELSE=14;
	public static final int ELSEIF=15;
	public static final int END=16;
	public static final int EQUAL=17;
	public static final int ESC_SEQ=18;
	public static final int EXPONENT=19;
	public static final int FLOAT=20;
	public static final int GET=21;
	public static final int GETEQ=22;
	public static final int HEX_DIGIT=23;
	public static final int ID=24;
	public static final int IF=25;
	public static final int INT=26;
	public static final int INTEGER=27;
	public static final int LCURL=28;
	public static final int LET=29;
	public static final int LETEQ=30;
	public static final int LPARA=31;
	public static final int MINUS=32;
	public static final int MOD=33;
	public static final int MULT=34;
	public static final int NOT=35;
	public static final int NOT_EQUAL=36;
	public static final int OCTAL_ESC=37;
	public static final int OR=38;
	public static final int PLUS=39;
	public static final int RCURL=40;
	public static final int REAL=41;
	public static final int RPARA=42;
	public static final int SAMEAS=43;
	public static final int SEMICOL=44;
	public static final int SENALWAYS=45;
	public static final int STRING=46;
	public static final int UNICODE_ESC=47;
	public static final int UNIFORM=48;
	public static final int WAIT=49;
	public static final int WAIT_DELAY=50;
	public static final int WAIT_POSEDGE=51;
	public static final int WAIT_STABLE=52;
	public static final int WS=53;

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
	@Override public String getGrammarFileName() { return "/Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g"; }

	// $ANTLR start "T__54"
	public final void mT__54() throws RecognitionException {
		try {
			int _type = T__54;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:12:7: ( 'property' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:12:9: 'property'
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
	// $ANTLR end "T__54"

	// $ANTLR start "SENALWAYS"
	public final void mSENALWAYS() throws RecognitionException {
		try {
			int _type = SENALWAYS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:39:3: ( 'senalways' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:39:5: 'senalways'
			{
			match("senalways"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SENALWAYS"

	// $ANTLR start "ALWAYS"
	public final void mALWAYS() throws RecognitionException {
		try {
			int _type = ALWAYS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:3: ( 'always' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:43:3: 'always'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: ( 'boolean' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:47:2: 'boolean'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:51:2: ( 'real' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:51:2: 'real'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:55:2: ( 'int' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:55:2: 'int'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:60:2: ( 'wait' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:60:2: 'wait'
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

	// $ANTLR start "WAIT_DELAY"
	public final void mWAIT_DELAY() throws RecognitionException {
		try {
			int _type = WAIT_DELAY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:64:2: ( 'waitDelay' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:64:2: 'waitDelay'
			{
			match("waitDelay"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WAIT_DELAY"

	// $ANTLR start "NOT"
	public final void mNOT() throws RecognitionException {
		try {
			int _type = NOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:68:3: ( '~' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:68:3: '~'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:72:2: ( '%' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:72:2: '%'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:77:2: ( '&' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:77:2: '&'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:81:2: ( '|' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:81:2: '|'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:86:2: ( 'assert' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:86:2: 'assert'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:91:2: ( 'if' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:91:2: 'if'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:96:2: ( 'end' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:96:2: 'end'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:101:2: ( 'else if' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:101:2: 'else if'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:2: ( 'else' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:105:2: 'else'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:109:2: ( 'waitStable' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:109:2: 'waitStable'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:113:2: ( 'assertStable' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:113:2: 'assertStable'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:117:2: ( 'assertUntil' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:117:2: 'assertUntil'
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

	// $ANTLR start "WAIT_POSEDGE"
	public final void mWAIT_POSEDGE() throws RecognitionException {
		try {
			int _type = WAIT_POSEDGE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:122:3: ( 'waitPosedge' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:122:3: 'waitPosedge'
			{
			match("waitPosedge"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WAIT_POSEDGE"

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			int _type = ID;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:126:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
			loop1:
			while (true) {
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
			}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:129:5: ( ( '0' .. '9' )+ )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:129:7: ( '0' .. '9' )+
			{
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:129:7: ( '0' .. '9' )+
			int cnt2=0;
			loop2:
			while (true) {
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
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:5: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT )
			int alt9=3;
			alt9 = dfa9.predict(input);
			switch (alt9) {
				case 1 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )?
					{
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:9: ( '0' .. '9' )+
					int cnt3=0;
					loop3:
					while (true) {
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
							EarlyExitException eee = new EarlyExitException(3, input);
							throw eee;
						}
						cnt3++;
					}

					match('.'); 
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:25: ( '0' .. '9' )*
					loop4:
					while (true) {
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
					}

					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:37: ( EXPONENT )?
					int alt5=2;
					int LA5_0 = input.LA(1);
					if ( (LA5_0=='E'||LA5_0=='e') ) {
						alt5=1;
					}
					switch (alt5) {
						case 1 :
							// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:133:37: EXPONENT
							{
							mEXPONENT(); 

							}
							break;

					}

					}
					break;
				case 2 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:134:9: '.' ( '0' .. '9' )+ ( EXPONENT )?
					{
					match('.'); 
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:134:13: ( '0' .. '9' )+
					int cnt6=0;
					loop6:
					while (true) {
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
							EarlyExitException eee = new EarlyExitException(6, input);
							throw eee;
						}
						cnt6++;
					}

					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:134:25: ( EXPONENT )?
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0=='E'||LA7_0=='e') ) {
						alt7=1;
					}
					switch (alt7) {
						case 1 :
							// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:134:25: EXPONENT
							{
							mEXPONENT(); 

							}
							break;

					}

					}
					break;
				case 3 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:9: ( '0' .. '9' )+ EXPONENT
					{
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:135:9: ( '0' .. '9' )+
					int cnt8=0;
					loop8:
					while (true) {
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
							EarlyExitException eee = new EarlyExitException(8, input);
							throw eee;
						}
						cnt8++;
					}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
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
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 13, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
					{
					match("//"); 

					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:14: (~ ( '\\n' | '\\r' ) )*
					loop10:
					while (true) {
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
					}

					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:28: ( '\\r' )?
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0=='\r') ) {
						alt11=1;
					}
					switch (alt11) {
						case 1 :
							// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:139:28: '\\r'
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:140:9: '/*' ( options {greedy=false; } : . )* '*/'
					{
					match("/*"); 

					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:140:14: ( options {greedy=false; } : . )*
					loop12:
					while (true) {
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
							// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:140:42: .
							{
							matchAny(); 
							}
							break;

						default :
							break loop12;
						}
					}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:143:5: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' ) )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:143:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
			{
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:143:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\\r\\n' )
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:143:11: ' '
					{
					match(' '); 
					}
					break;
				case 2 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:144:11: '\\t'
					{
					match('\t'); 
					}
					break;
				case 3 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:145:11: '\\r'
					{
					match('\r'); 
					}
					break;
				case 4 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:146:11: '\\n'
					{
					match('\n'); 
					}
					break;
				case 5 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:147:10: '\\r\\n'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:154:5: ( '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\'' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:154:8: '\\'' ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )* '\\''
			{
			match('\''); 
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:154:13: ( ESC_SEQ |~ ( '\\\\' | '\\'' ) )*
			loop15:
			while (true) {
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:154:15: ESC_SEQ
					{
					mESC_SEQ(); 

					}
					break;
				case 2 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:154:25: ~ ( '\\\\' | '\\'' )
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
			}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
			{
			if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:22: ( '+' | '-' )?
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

			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:160:33: ( '0' .. '9' )+
			int cnt17=0;
			loop17:
			while (true) {
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
					EarlyExitException eee = new EarlyExitException(17, input);
					throw eee;
				}
				cnt17++;
			}

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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:163:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:167:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
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
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:167:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:168:9: UNICODE_ESC
					{
					mUNICODE_ESC(); 

					}
					break;
				case 3 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:169:9: OCTAL_ESC
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:174:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
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
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 19, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}

			switch (alt19) {
				case 1 :
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:174:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:175:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
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
					// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:176:9: '\\\\' ( '0' .. '7' )
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:181:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:181:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:187:2: ( '+' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:187:2: '+'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:191:3: ( '-' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:191:3: '-'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:196:2: ( '*' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:196:2: '*'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:200:2: ( '/' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:200:2: '/'
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

	// $ANTLR start "DASH"
	public final void mDASH() throws RecognitionException {
		try {
			int _type = DASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:204:2: ( '***' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:204:2: '***'
			{
			match("***"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DASH"

	// $ANTLR start "EQUAL"
	public final void mEQUAL() throws RecognitionException {
		try {
			int _type = EQUAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:207:2: ( '=' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:207:2: '='
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:212:2: ( '!=' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:212:2: '!='
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:216:2: ( '>' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:216:2: '>'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:220:2: ( '<' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:220:2: '<'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:224:2: ( '>=' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:224:2: '>='
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:227:2: ( '<=' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:227:2: '<='
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:232:2: ( '==' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:232:2: '=='
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:238:2: ( '(' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:238:2: '('
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:242:2: ( ')' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:242:2: ')'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:246:2: ( '{' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:246:2: '{'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:250:2: ( '}' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:250:2: '}'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:256:2: ( ';' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:256:2: ';'
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
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:260:2: ( ',' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:260:2: ','
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

	// $ANTLR start "UNIFORM"
	public final void mUNIFORM() throws RecognitionException {
		try {
			int _type = UNIFORM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:263:2: ( 'uniform(' INT ',' INT ')' )
			// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:263:2: 'uniform(' INT ',' INT ')'
			{
			match("uniform("); 

			mINT(); 

			match(','); 
			mINT(); 

			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNIFORM"

	@Override
	public void mTokens() throws RecognitionException {
		// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:8: ( T__54 | SENALWAYS | ALWAYS | BOOLEAN | REAL | INTEGER | WAIT | WAIT_DELAY | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_STABLE | ASSERT_UNTIL | WAIT_POSEDGE | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | DASH | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA | UNIFORM )
		int alt20=46;
		alt20 = dfa20.predict(input);
		switch (alt20) {
			case 1 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:10: T__54
				{
				mT__54(); 

				}
				break;
			case 2 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:16: SENALWAYS
				{
				mSENALWAYS(); 

				}
				break;
			case 3 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:26: ALWAYS
				{
				mALWAYS(); 

				}
				break;
			case 4 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:33: BOOLEAN
				{
				mBOOLEAN(); 

				}
				break;
			case 5 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:41: REAL
				{
				mREAL(); 

				}
				break;
			case 6 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:46: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 7 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:54: WAIT
				{
				mWAIT(); 

				}
				break;
			case 8 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:59: WAIT_DELAY
				{
				mWAIT_DELAY(); 

				}
				break;
			case 9 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:70: NOT
				{
				mNOT(); 

				}
				break;
			case 10 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:74: MOD
				{
				mMOD(); 

				}
				break;
			case 11 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:78: AND
				{
				mAND(); 

				}
				break;
			case 12 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:82: OR
				{
				mOR(); 

				}
				break;
			case 13 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:85: ASSERT
				{
				mASSERT(); 

				}
				break;
			case 14 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:92: IF
				{
				mIF(); 

				}
				break;
			case 15 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:95: END
				{
				mEND(); 

				}
				break;
			case 16 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:99: ELSEIF
				{
				mELSEIF(); 

				}
				break;
			case 17 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:106: ELSE
				{
				mELSE(); 

				}
				break;
			case 18 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:111: WAIT_STABLE
				{
				mWAIT_STABLE(); 

				}
				break;
			case 19 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:123: ASSERT_STABLE
				{
				mASSERT_STABLE(); 

				}
				break;
			case 20 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:137: ASSERT_UNTIL
				{
				mASSERT_UNTIL(); 

				}
				break;
			case 21 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:150: WAIT_POSEDGE
				{
				mWAIT_POSEDGE(); 

				}
				break;
			case 22 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:163: ID
				{
				mID(); 

				}
				break;
			case 23 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:166: INT
				{
				mINT(); 

				}
				break;
			case 24 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:170: FLOAT
				{
				mFLOAT(); 

				}
				break;
			case 25 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:176: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 26 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:184: WS
				{
				mWS(); 

				}
				break;
			case 27 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:187: STRING
				{
				mSTRING(); 

				}
				break;
			case 28 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:194: PLUS
				{
				mPLUS(); 

				}
				break;
			case 29 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:199: MINUS
				{
				mMINUS(); 

				}
				break;
			case 30 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:205: MULT
				{
				mMULT(); 

				}
				break;
			case 31 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:210: DIV
				{
				mDIV(); 

				}
				break;
			case 32 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:214: DASH
				{
				mDASH(); 

				}
				break;
			case 33 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:219: EQUAL
				{
				mEQUAL(); 

				}
				break;
			case 34 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:225: NOT_EQUAL
				{
				mNOT_EQUAL(); 

				}
				break;
			case 35 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:235: GET
				{
				mGET(); 

				}
				break;
			case 36 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:239: LET
				{
				mLET(); 

				}
				break;
			case 37 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:243: GETEQ
				{
				mGETEQ(); 

				}
				break;
			case 38 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:249: LETEQ
				{
				mLETEQ(); 

				}
				break;
			case 39 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:255: SAMEAS
				{
				mSAMEAS(); 

				}
				break;
			case 40 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:262: LPARA
				{
				mLPARA(); 

				}
				break;
			case 41 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:268: RPARA
				{
				mRPARA(); 

				}
				break;
			case 42 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:274: LCURL
				{
				mLCURL(); 

				}
				break;
			case 43 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:280: RCURL
				{
				mRCURL(); 

				}
				break;
			case 44 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:286: SEMICOL
				{
				mSEMICOL(); 

				}
				break;
			case 45 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:294: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 46 :
				// /Users/myers/research/nobackup/workspace/BioSim/gui/src/lpn/parser/properties/Property.g:1:300: UNIFORM
				{
				mUNIFORM(); 

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

	protected class DFA9 extends DFA {

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
		@Override
		public String getDescription() {
			return "132:1: FLOAT : ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | '.' ( '0' .. '9' )+ ( EXPONENT )? | ( '0' .. '9' )+ EXPONENT );";
		}
	}

	static final String DFA20_eotS =
		"\1\uffff\7\40\4\uffff\2\40\1\55\1\uffff\1\57\4\uffff\1\61\1\63\1\uffff"+
		"\1\65\1\67\7\uffff\7\40\1\77\4\40\13\uffff\6\40\1\112\1\uffff\1\40\1\114"+
		"\7\40\1\124\1\uffff\1\130\1\uffff\1\132\6\40\1\uffff\3\40\3\uffff\3\40"+
		"\1\147\1\152\7\40\1\uffff\2\40\1\uffff\1\164\4\40\1\171\3\40\1\uffff\3"+
		"\40\2\uffff\1\u0080\2\40\1\u0083\2\40\1\uffff\2\40\1\uffff\1\u0088\2\40"+
		"\1\u008b\1\uffff\1\u008c\1\u008d\3\uffff";
	static final String DFA20_eofS =
		"\u008e\uffff";
	static final String DFA20_minS =
		"\1\11\1\162\1\145\1\154\1\157\1\145\1\146\1\141\4\uffff\1\154\1\156\1"+
		"\56\1\uffff\1\52\4\uffff\1\52\1\75\1\uffff\2\75\7\uffff\1\157\1\156\1"+
		"\167\1\163\1\157\1\141\1\164\1\60\1\151\1\144\1\163\1\151\13\uffff\1\160"+
		"\2\141\1\145\2\154\1\60\1\uffff\1\164\1\60\1\145\1\146\1\145\1\154\1\171"+
		"\1\162\1\145\1\60\1\uffff\1\60\1\uffff\1\40\1\157\1\162\1\167\1\163\1"+
		"\164\1\141\1\uffff\1\145\1\164\1\157\3\uffff\1\162\1\164\1\141\2\60\1"+
		"\156\1\154\1\141\1\163\1\155\2\171\1\uffff\1\164\1\156\1\uffff\1\60\1"+
		"\141\1\142\1\145\1\50\1\60\1\163\1\141\1\164\1\uffff\1\171\1\154\1\144"+
		"\2\uffff\1\60\1\142\1\151\1\60\1\145\1\147\1\uffff\2\154\1\uffff\1\60"+
		"\2\145\1\60\1\uffff\2\60\3\uffff";
	static final String DFA20_maxS =
		"\1\176\1\162\1\145\1\163\1\157\1\145\1\156\1\141\4\uffff\2\156\1\145\1"+
		"\uffff\1\57\4\uffff\1\52\1\75\1\uffff\2\75\7\uffff\1\157\1\156\1\167\1"+
		"\163\1\157\1\141\1\164\1\172\1\151\1\144\1\163\1\151\13\uffff\1\160\2"+
		"\141\1\145\2\154\1\172\1\uffff\1\164\1\172\1\145\1\146\1\145\1\154\1\171"+
		"\1\162\1\145\1\172\1\uffff\1\172\1\uffff\1\172\1\157\1\162\1\167\1\163"+
		"\1\164\1\141\1\uffff\1\145\1\164\1\157\3\uffff\1\162\1\164\1\141\2\172"+
		"\1\156\1\154\1\141\1\163\1\155\2\171\1\uffff\1\164\1\156\1\uffff\1\172"+
		"\1\141\1\142\1\145\1\50\1\172\1\163\1\141\1\164\1\uffff\1\171\1\154\1"+
		"\144\2\uffff\1\172\1\142\1\151\1\172\1\145\1\147\1\uffff\2\154\1\uffff"+
		"\1\172\2\145\1\172\1\uffff\2\172\3\uffff";
	static final String DFA20_acceptS =
		"\10\uffff\1\11\1\12\1\13\1\14\3\uffff\1\30\1\uffff\1\32\1\33\1\34\1\35"+
		"\2\uffff\1\42\2\uffff\1\50\1\51\1\52\1\53\1\54\1\55\1\26\14\uffff\1\27"+
		"\1\31\1\37\1\40\1\36\1\47\1\41\1\45\1\43\1\46\1\44\7\uffff\1\16\12\uffff"+
		"\1\6\1\uffff\1\17\7\uffff\1\5\3\uffff\1\7\1\20\1\21\14\uffff\1\3\2\uffff"+
		"\1\15\11\uffff\1\4\3\uffff\1\56\1\1\6\uffff\1\2\2\uffff\1\10\4\uffff\1"+
		"\22\2\uffff\1\24\1\25\1\23";
	static final String DFA20_specialS =
		"\u008e\uffff}>";
	static final String[] DFA20_transitionS = {
			"\2\21\2\uffff\1\21\22\uffff\1\21\1\27\3\uffff\1\11\1\12\1\22\1\32\1\33"+
			"\1\25\1\23\1\37\1\24\1\17\1\20\12\16\1\uffff\1\36\1\31\1\26\1\30\2\uffff"+
			"\32\40\4\uffff\1\40\1\uffff\1\3\1\4\2\40\1\14\3\40\1\6\6\40\1\1\1\40"+
			"\1\5\1\2\1\40\1\15\1\40\1\7\3\40\1\34\1\13\1\35\1\10",
			"\1\41",
			"\1\42",
			"\1\43\6\uffff\1\44",
			"\1\45",
			"\1\46",
			"\1\50\7\uffff\1\47",
			"\1\51",
			"",
			"",
			"",
			"",
			"\1\53\1\uffff\1\52",
			"\1\54",
			"\1\17\1\uffff\12\16\13\uffff\1\17\37\uffff\1\17",
			"",
			"\1\56\4\uffff\1\56",
			"",
			"",
			"",
			"",
			"\1\60",
			"\1\62",
			"",
			"\1\64",
			"\1\66",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\70",
			"\1\71",
			"\1\72",
			"\1\73",
			"\1\74",
			"\1\75",
			"\1\76",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\100",
			"\1\101",
			"\1\102",
			"\1\103",
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
			"\1\104",
			"\1\105",
			"\1\106",
			"\1\107",
			"\1\110",
			"\1\111",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"",
			"\1\113",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\115",
			"\1\116",
			"\1\117",
			"\1\120",
			"\1\121",
			"\1\122",
			"\1\123",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"",
			"\12\40\7\uffff\3\40\1\125\13\40\1\127\2\40\1\126\7\40\4\uffff\1\40\1"+
			"\uffff\32\40",
			"",
			"\1\131\17\uffff\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\133",
			"\1\134",
			"\1\135",
			"\1\136",
			"\1\137",
			"\1\140",
			"",
			"\1\141",
			"\1\142",
			"\1\143",
			"",
			"",
			"",
			"\1\144",
			"\1\145",
			"\1\146",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\12\40\7\uffff\22\40\1\150\1\40\1\151\5\40\4\uffff\1\40\1\uffff\32\40",
			"\1\153",
			"\1\154",
			"\1\155",
			"\1\156",
			"\1\157",
			"\1\160",
			"\1\161",
			"",
			"\1\162",
			"\1\163",
			"",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\165",
			"\1\166",
			"\1\167",
			"\1\170",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\172",
			"\1\173",
			"\1\174",
			"",
			"\1\175",
			"\1\176",
			"\1\177",
			"",
			"",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\u0081",
			"\1\u0082",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\u0084",
			"\1\u0085",
			"",
			"\1\u0086",
			"\1\u0087",
			"",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\1\u0089",
			"\1\u008a",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
			"",
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

	protected class DFA20 extends DFA {

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
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__54 | SENALWAYS | ALWAYS | BOOLEAN | REAL | INTEGER | WAIT | WAIT_DELAY | NOT | MOD | AND | OR | ASSERT | IF | END | ELSEIF | ELSE | WAIT_STABLE | ASSERT_STABLE | ASSERT_UNTIL | WAIT_POSEDGE | ID | INT | FLOAT | COMMENT | WS | STRING | PLUS | MINUS | MULT | DIV | DASH | EQUAL | NOT_EQUAL | GET | LET | GETEQ | LETEQ | SAMEAS | LPARA | RPARA | LCURL | RCURL | SEMICOL | COMMA | UNIFORM );";
		}
	}

}
