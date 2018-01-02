grammar Property;


options {
  language = Java;
  output = AST;
  ASTLabelType = CommonTree;

}

@header {
  package lpn.parser.properties;
  //import lpn.parser.LhpnFile;
  //package antlrPackage;
}

@lexer::header { 
  package lpn.parser.properties;
  //package antlrPackage;
}



program
	:property
	;
	
property
: 'property'^ ID LCURL! (declaration)* (statement)* RCURL!
;
	
declaration
  :BOOLEAN^ ID (COMMA! ID)* SEMICOL!
  | REAL^ ID (COMMA! ID)* SEMICOL!
  | INTEGER^ ID (COMMA! ID)* SEMICOL!
  ;
  
SENALWAYS
  : 'senalways'
  ;

ALWAYS
: 'always'
;

BOOLEAN
:'boolean'
;

REAL
:'real'
;

INTEGER
:'int'
;


WAIT
:'wait'
;

WAIT_DELAY
:'waitDelay'
;
    
NOT
: '~'
;

MOD
:'%'
;


AND
:'&'
;

OR
:'|'
;


ASSERT
:'assert'
;


IF
:'if'
;


END
:'end'
;


ELSEIF
:'else if'
;

ELSE
:'else'
;

WAIT_STABLE
:'waitStable'
;

ASSERT_STABLE
:'assertStable'
;

ASSERT_UNTIL
:'assertUntil'
;


WAIT_POSEDGE
: 'waitPosedge'
;


ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :	'0'..'9'+
    
    ;
FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        |'\r\n'
        ) {$channel=HIDDEN;}
    ;
    


STRING
    :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    ;


fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;




PLUS 
:'+'
;

MINUS
: '-'
;


MULT
:'*'
;

DIV
:'/'
;

DASH
:'***'
;
EQUAL
:'='
;


NOT_EQUAL
:'!='
;

GET
:'>'
;

LET
:'<'
;

GETEQ
:'>='
;
LETEQ
:'<='
;


SAMEAS
:'=='
;



LPARA
:'('
;

RPARA
:')'
;

LCURL
:'{'
;

RCURL
:'}'
;



SEMICOL
:';'
;

COMMA
:','
;
UNIFORM
:'uniform('INT','INT')'
;

booleanNegationExpression
: (NOT^)* constantValue
//: (NOT^)*  (LPARA!)* relationalExpression (RPARA!)*
;
  
always_statement
  : ALWAYS^ LCURL! (statement)* RCURL!  //SEMICOL!
  ;

senalways_statement
  : SENALWAYS^ (sensitivityList)? LCURL! (statement)* RCURL!
  ;

sensitivityList
  : (LPARA^ ID (COMMA! ID)* RPARA!)
  ;

signExpression
:(PLUS^|MINUS^)*  booleanNegationExpression
//:(PLUS^|MINUS^)* constantValue
;
multiplyingExpression
  : signExpression ((MULT^|DIV^|MOD^) signExpression)*
  ;



addingExpression
  : multiplyingExpression ((PLUS^|MINUS^) multiplyingExpression)*
  ;
  

relationalExpression
  : addingExpression ((EQUAL^|NOT_EQUAL^|GET^|GETEQ^|LET^|LETEQ^|SAMEAS^) addingExpression)*
  ;

logicalExpression
   : relationalExpression ((AND^|OR^) relationalExpression)*
	 ;
//logicalExpression
 //  : relationalExpression ((AND^|OR^) relationalExpression)*
//   ;	 

unaryExpression
  : (NOT^)*  LPARA! logicalExpression RPARA! 
 // : (NOT^)*   logicalExpression 
  ;

combinationalExpression
: unaryExpression ((AND^|OR^) unaryExpression)*
;
expression
	//: relational
  //:constantValue
  //|primitiveElement
  //|addingExpression
  //|multiplyingExpression
  //:  unaryExpression 
 // | logicalExpression
  :combinationalExpression
  | logicalExpression
   ;


constantValue
	: INT | ID | UNIFORM
	;

wait_statement
	: WAIT^ LPARA! expression RPARA! SEMICOL!
//  | WAIT^ LPARA! expression COMMA!  expression (GET expression)* RPARA! SEMICOL!
  | WAIT^ LPARA! expression COMMA!  expression RPARA! SEMICOL!
	;
		
wait_delay_statement
  : WAIT_DELAY^ LPARA! expression RPARA! SEMICOL!
  ;

assert_statement
	: ASSERT^ LPARA! expression COMMA! expression RPARA! SEMICOL!
	;
	
if_statement
  : IF^ if_part
  ;
  
if_part
 	: LPARA!expression RPARA! LCURL! (statement)* RCURL! (else_if)* (else_part)*
  ;

else_if
	: ELSEIF^  LPARA!expression RPARA!  LCURL! (statement)*  RCURL! 
	;
	
else_part
	:ELSE^  LCURL! (statement)*  RCURL!
	;	
	
waitStable_statement
:WAIT_STABLE^ LPARA! expression COMMA! expression RPARA! SEMICOL!
;
assertUntil_statement
:ASSERT_UNTIL^ LPARA! expression COMMA! expression RPARA! SEMICOL!
;

edge_statement
: WAIT_POSEDGE^ LPARA! expression RPARA! SEMICOL!
;
assertStable_statement
:ASSERT_STABLE^ LPARA! expression COMMA! expression RPARA! SEMICOL!
;


//waitPosedge_statement
//:WAIT_POSEDGE^ LPARA! expression RPARA! SEMICOL!
//;

statement
	:wait_statement
	|wait_delay_statement
	|assert_statement
	|if_statement
	|waitStable_statement
	|assertUntil_statement
	|always_statement
	|assertStable_statement
	|edge_statement
	|senalways_statement
	; 
	

