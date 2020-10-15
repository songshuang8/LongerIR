
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include "Ant1Freeze.h"

// OPerations
#define OP_PLUS          0
#define OP_MINUS         1
#define OP_MULTIPLY      2
#define OP_DIVIDE        3
#define OP_MOD		     4
#define OP_POWER         5
#define OP_UMINUS        6
#define OP_SIN           7
#define OP_COS           8
#define OP_TG            9
#define OP_CTG           10
#define OP_ARCSIN        11
#define OP_ARCCOS        12
#define OP_ARCTG         13
#define OP_ARCCTG        14
#define OP_SH            15
#define OP_CH            16
#define OP_TH            17
#define OP_CTH           18
#define OP_EXP           19
#define OP_LG            20
#define OP_LN            21
#define OP_SQRT          22
#define OP_IN            23
#define OP_TURN        24


#define F_G              9.81
#define H_LEET           1337

TCALCNode *TCALC::CreateNode(int _value, TCALCNode *_left, TCALCNode *_right)
{
	TCALCNode *pNode = new TCALCNode(_value, _left, _right);
	return pNode;
}

void TCALC::SendError(int errNum) 
{
	TError error("err",3);

	root = NULL;

	throw error;

	return;
}


bool TCALC::GetToken(void)
{
	*curToken = '\0';

	while(expr[pos]==' ') pos++;

		if(expr[pos]=='\0')
		{
			curToken[0] = '\0';
			typToken = CALC_END;
			return true;
		}
	else
			if(IsDelim())
				{
					curToken[0] = expr[pos++];
					curToken[1] = '\0';
						switch(*curToken)
							{
								case '+':
									typToken = CALC_PLUS;
									return true;

								case '-':
									typToken = CALC_MINUS;
									return true;

								case '*':
									typToken = CALC_MULTIPLY;
									return true;

								case '/':
									typToken = CALC_DIVIDE;
									return true;

								case '%':
									typToken = CALC_MOD;
									return true;

								case '^':
									typToken = CALC_POWER;
									return true;

								case '[': 

								case '(':
									typToken = CALC_L_BRACKET;
									return true;

								case ']': 

								case ')':
									typToken = CALC_R_BRACKET;
									return true;
							}
				}
	else if(IsLetter())
	{
		int i=0;
		while(IsLetter()) curToken[i++] = expr[pos++];
		curToken[i] = '\0';

		int len = strlen(curToken);
		for(i=0; i<len; i++)
			if(curToken[i]>='A' && curToken[i]<='Z')
				curToken[i] += 'a' - 'A';

		if(!strcmp(curToken, "x"))

			{
				typToken = CALC_X;
				return true;
			}

		else if(!strcmp(curToken, "mod"))

			{
				typToken = CALC_MOD;
				return true;
			}

		else if(!strcmp(curToken, "not"))

			{
				typToken = CALC_TURN;
				return true;
			}

		else if(!strcmp(curToken, "MOD")) 

			{
				typToken = CALC_MOD;
				return true;
			}


		else if(!strcmp(curToken, "pi"))
			
			{
				typToken = CALC_PI;
				return true;
			}

		else if(!strcmp(curToken, "NOT"))
			{
				typToken = CALC_TURN;
				return true;
			}

		else if(!strcmp(curToken, "sh"))
			
			{
				typToken = CALC_SH;
				return true;
			}

		else if(!strcmp(curToken, "ch"))

			{
				typToken = CALC_CH;
				return true;
			}

		else if(!strcmp(curToken, "th"))
			
			{	
				typToken = CALC_TH;
				return true;
			}

		else if(!strcmp(curToken, "cth"))
			
			{
				typToken = CALC_CTH;
				return true;
			}

		else if(!strcmp(curToken, "exp"))
			
			{
				typToken = CALC_EXP;
				return true;
			}

		else if(!strcmp(curToken, "lg"))
			
			{
				typToken = CALC_LG;
				return true;
			}

		else if(!strcmp(curToken, "ln"))
			
			{
				typToken = CALC_LN;
				return true;
			}

		else if(!strcmp(curToken, "sqrt"))
		
			{
				typToken = CALC_SQRT;
				return true;
			}

		else SendError(0);
	}

	else if(IsDigit() || IsPoint())
	{
		int i=0;
		while(IsDigit()) curToken[i++] = expr[pos++];
		if(IsPoint())
			{
				curToken[i++] = expr[pos++];
				while(IsDigit()) curToken[i++] = expr[pos++];
			}
		curToken[i] = '\0';
		typToken = CALC_NUMBER;
		return true;
	}

	else
	{
		curToken[0] = expr[pos++];
		curToken[1] = '\0';
		SendError(1);
	}

	return false;
}      

bool TCALC::Compile(char *_expr)
{

	pos = 0;
	expr = _expr;
	*curToken = '\0';
	if(root!=NULL)
		{
			DelTree(root);
			root = NULL;
		}

	GetToken();
	if(typToken==CALC_END) SendError(2);

	root = Expr();
	if(typToken!=CALC_END) SendError(3);
	return true;
}

TCALCNode *TCALC::Expr(void)
{
	TCALCNode *temp = Expr1();

	while(1)
	{
		if(typToken == CALC_PLUS)
			{
				GetToken();
				temp = CreateNode(OP_PLUS, temp, Expr1());
			}

		else if(typToken==CALC_MINUS)
			{
				GetToken();
				temp = CreateNode(OP_MINUS, temp, Expr1());
			}
		else break;
	}


	return temp;
}



TCALCNode *TCALC::Expr1(void)
{
	TCALCNode *temp = Expr2();

	while(1)
	{
		if(typToken==CALC_MULTIPLY)
			{
				GetToken();
				temp = CreateNode(OP_MULTIPLY, temp, Expr2());
			}

		else if(typToken==CALC_DIVIDE)
			{
				GetToken();
				temp = CreateNode(OP_DIVIDE, temp, Expr2());
			}

		else if(typToken==CALC_MOD)
			{
				GetToken();
				temp = CreateNode(OP_MOD, temp, Expr2());
			}
		else break;
	}

	return temp;
}

TCALCNode *TCALC::Expr2(void)
{
	TCALCNode *temp = Expr3();

	while(1)
	{

		if(typToken==CALC_POWER)
			{
				GetToken();
				temp = CreateNode(OP_POWER, temp, Expr2());
			}

		else break;
	}

	return temp;
}

TCALCNode *TCALC::Expr3(void)
{
	TCALCNode *temp;

	if(typToken==CALC_PLUS)
	{
		GetToken();
		temp = Expr4();
	}


	else if(typToken==CALC_MINUS)
	{
		GetToken();
		temp = CreateNode(OP_UMINUS, Expr4());
	}
	else if(typToken==CALC_TURN)
	{
		GetToken();
		temp = CreateNode(OP_TURN, Expr4());
	}

	else
		temp = Expr4();

	return temp;      
}

TCALCNode *TCALC::Expr4(void)
{
	TCALCNode *temp;

	if(typToken >= CALC_SIN && typToken<=CALC_X)
		{
			temp = CreateNode(OP_SIN-CALC_SIN+typToken);
			GetToken();

			if(typToken!=CALC_L_BRACKET)
				SendError(4);
				GetToken();

			temp->left = Expr();

			if(typToken!=CALC_R_BRACKET)
				SendError(5);
				GetToken();
		}

	else
		temp = Expr5();

	return temp;
}

TCALCNode *TCALC::Expr5(void)
{
	TCALCNode *temp;

	switch(typToken)
		{
			case CALC_NUMBER:
				temp = CreateNode((int)atoi(curToken));
				GetToken();
			break;

			case CALC_PI:
				temp = CreateNode((int)M_PI);
				GetToken();
				break;

			case CALC_G:
				temp =CreateNode((int)F_G);
				GetToken();
				break;

			case CALC_L_BRACKET:
				GetToken();
				temp = Expr();
				if(typToken!=CALC_R_BRACKET) SendError(5);
				GetToken();
				break;

			case CALC_EXP1:
				temp = CreateNode((int) M_E);
				GetToken();
				break;

			case CALC_LEET:
				temp = CreateNode((int) H_LEET);
				GetToken();
				break;

			default:
				SendError(6);
	}

	return temp;         
}

int TCALC::Evaluate(void)
{
	result = CalcTree(root);
	return result;
}

int TCALC::CalcTree(TCALCNode *tree) 
{

	static int temp;

	if(tree->left==NULL && tree->right==NULL)
		return tree->value;  
	
	else
		switch((int)tree->value)
	{
		case OP_PLUS:
			return CalcTree(tree->left)+CalcTree(tree->right);
			// "(tree->left)+(tree->right)"

		case OP_MINUS:
			return CalcTree(tree->left)-CalcTree(tree->right);
			// "(tree->left)-(tree->right)"

		case OP_MULTIPLY:
			return CalcTree(tree->left)*CalcTree(tree->right);
			// "(tree->left)*(tree->right)"

		case OP_DIVIDE:
			return CalcTree(tree->left)/CalcTree(tree->right);	
			// "(tree->left)/(tree->right)"

		case OP_MOD:
			return (int)CalcTree(tree->left)%(int)CalcTree(tree->right);	
			// "(tree->left)%(tree->right)"

		case OP_POWER:
			return (int)pow((double)CalcTree(tree->left),CalcTree(tree->right));
			// "(tree->left)^(tree->right)"

		case OP_UMINUS:
			return -CalcTree(tree->left);
			// "-(tree->left)"

		case OP_TURN:	
			return ~(CalcTree(tree->left));

		case OP_IN:
			return 1;

	}

	return 0;
}

void TCALC::DelTree(TCALCNode *tree)
{
	if(tree==NULL) return;

	DelTree(tree->left);
	DelTree(tree->right);

	delete tree;

	return;
}