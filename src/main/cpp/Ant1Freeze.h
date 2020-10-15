
#ifndef ANT1FREEZE_h
#define ANT1FREEZE_h

#include <string.h>

// Defines
#define MAX_EXPR_LEN   255
#define MAX_TOKEN_LEN  80

struct TCALCNode
{
	int value;

	TCALCNode *left;

	TCALCNode *right;

	TCALCNode(int _value=0.0, TCALCNode *_left=NULL, TCALCNode *_right=NULL)
	{
		value = _value;
		left = _left;
		right = _right; 
	}
};

struct TError
{
	char *error;
	int pos;

	TError() 	{	};

	TError(char *_error, int _pos) 
	{
		error=_error;
		pos=_pos+1; 
	}
};

class TCALC
{
private:

	TCALCNode *root;
	char *expr;
	char curToken[MAX_TOKEN_LEN];

	enum {
		CALC_PLUS, CALC_MINUS, CALC_MULTIPLY, CALC_DIVIDE, CALC_MOD, CALC_POWER,
		CALC_SIN, CALC_COS, CALC_TG, CALC_CTG, CALC_TURN, CALC_ARCCOS, CALC_ARCTG, 		
		CALC_ARCCTG, CALC_SH, CALC_CH, CALC_TH, CALC_CTH, CALC_EXP, CALC_LG, 			
		CALC_LN, CALC_SQRT, CALC_X, CALC_L_BRACKET, CALC_R_BRACKET, CALC_E, 			
		CALC_PI, CALC_NUMBER, CALC_END, CALC_G, CALC_EXP1,CALC_LEET
	     } typToken; 

	int pos;
	int result;

	

private:

	TCALCNode *CreateNode(int _value=0.0, TCALCNode *_left=NULL, TCALCNode *_right=NULL);

	TCALCNode *Expr(void);
	
	TCALCNode *Expr1(void);
	
	TCALCNode *Expr2(void);
	
	TCALCNode *Expr3(void);
	
	TCALCNode *Expr4(void);
	
	TCALCNode *Expr5(void);


	bool GetToken(void);

	bool IsDelim(void)

	{
		return (strchr("+-*/%^()[]", expr[pos])!=NULL);   
	}   

	bool IsLetter(void)

	{
		return ((expr[pos]>='a' && expr[pos]<='z') ||    
		(expr[pos]>='A' && expr[pos]<='Z'));             
	}       

	bool IsDigit(void) 

	{
		return (expr[pos]>='0' && expr[pos]<='9');     
	} 

	bool IsPoint(void)

	{ 
		return (expr[pos]=='.');                  
	}                        

	int CalcTree(TCALCNode *tree);

	void  DelTree(TCALCNode *tree);

	void SendError(int errNum);


public:

	TCALC() 

	{ 
		result = 0;
		root = NULL; 
	}
	


	~TCALC() 

	{
		DelTree(root);
		root=NULL; 
	}

	bool Compile(char *expr);
	
	void Decompile() 

	{ 
		DelTree(root);
		root=NULL; 
	}

	int Evaluate();
	 
	int GetResult(void) 

	{
		return result; 
	}
};


#endif

