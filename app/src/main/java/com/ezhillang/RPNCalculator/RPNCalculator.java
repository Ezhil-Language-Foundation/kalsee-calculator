/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */

package com.ezhillang.RPNCalculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

//represent a token within the calculator

public class RPNCalculator {
	boolean DEBUG = !true;
	List<Token> m_toks;
	Stack<Operand> operand_stack;
	Stack<Operator> operator_stack;
	
	public RPNCalculator(List<Token> toks) {
		m_toks = toks;
		operand_stack  = new Stack<Operand>();
		operator_stack  = new Stack<Operator>();		
	}
	
	void display_stacks() {
		if ( !DEBUG )
			return;
		
		System.out.println("#############");
		System.out.println("OPRTOR ="+operator_stack.size());		
		Iterator<Operator> tok = operator_stack.iterator();
		while(tok.hasNext()){
			System.out.println(tok.next());
		}
		System.out.println("OPRND = "+operand_stack.size());
		Iterator<Operand> tok2 = operand_stack.iterator();
		while(tok2.hasNext()){
			System.out.println(tok2.next());
		}		
		System.out.println("#############");
		
		return;
	}
	
	protected void simple_eval() throws Exception {
		display_stacks();
		
		Operator op = operator_stack.pop();
		Token.Kinds op_kind = op.getKind();
			
		Operand op_B = null, op_result = null;
		Operand op_A = operand_stack.pop();
		double dbl_op_A = op_A.getNumericValue(),dbl_op_B = Double.NaN,result = Double.NaN;


        if ( operand_stack.size() == 0) {
            boolean unop_found = false;
            Token.Kinds alt_op = op_kind;
            //maybe unary operator; in that case we change operator
            //and push back both operand and operator on stack for
            //re-eval
            switch(op_kind) {
				case ADD_OP:
                    unop_found = true;
                    //there is no unary plus so we just drop the operator/TOS
                    //but push back operand/TOS
                    break;
                case SUB_OP:
                    unop_found = true;
                    alt_op = Token.Kinds.UNARY_MINUS_OP;
                    operator_stack.push(new Operator(op.getTokenString(),alt_op));
                    break;
            }
            if ( unop_found ) {
                operand_stack.push(op_A);
				//do simple eval
				simple_eval();
				return;
			}
		}

		if ( op_kind == Token.Kinds.ADD_OP ) {
            op_B = operand_stack.pop();
			dbl_op_B = op_B.getNumericValue();
			result = dbl_op_A + dbl_op_B;				
		} else if (  op_kind == Token.Kinds.SUB_OP ) {
			op_B = operand_stack.pop();				
			dbl_op_B = op_B.getNumericValue();
			result =  dbl_op_B - dbl_op_A;
		} else if ( op_kind == Token.Kinds.MUL_OP ) {
			op_B = operand_stack.pop();				
			dbl_op_B = op_B.getNumericValue();
			result = dbl_op_A * dbl_op_B;				
		} else if ( op_kind == Token.Kinds.DIV_OP ) {
			op_B = operand_stack.pop();				
			dbl_op_B = op_B.getNumericValue();
			result = dbl_op_B/dbl_op_A ;
		} else if ( op_kind == Token.Kinds.LOG10_OP ) {
			result = Math.log10(dbl_op_A);
		} else if ( op_kind == Token.Kinds.LOG_OP ) {
			result = Math.log(dbl_op_A);
		} else if ( op_kind == Token.Kinds.EXP_OP ) {
			result = Math.exp(dbl_op_A);
		} else if ( op_kind == Token.Kinds.SIN_OP ) {
			result = Math.sin(dbl_op_A);
		} else if ( op_kind == Token.Kinds.COS_OP ) {
			result = Math.cos(dbl_op_A);
		} else if ( op_kind == Token.Kinds.TAN_OP ) {
			result = Math.tan(dbl_op_A);
		} else if ( op_kind == Token.Kinds.ASIN_OP ) {
			result = Math.asin(dbl_op_A);			
		} else if ( op_kind == Token.Kinds.ACOS_OP ) {
			result = Math.acos(dbl_op_A);
		} else if ( op_kind == Token.Kinds.ATAN_OP ) {
			result = Math.atan(dbl_op_A);
		} else if ( op_kind == Token.Kinds.UNARY_MINUS_OP ) {
			result = -1*dbl_op_A;
		} else if ( op_kind == Token.Kinds.POWER_OP ) {
			op_B = operand_stack.pop();
			dbl_op_B = op_B.getNumericValue();
			result = Math.pow(dbl_op_B, dbl_op_A);
		} else if ( op_kind == Token.Kinds.PERC_OP ) {
			op_B = operand_stack.pop();
			dbl_op_B = op_B.getNumericValue();
			result = (100.00*dbl_op_B)/dbl_op_A ;
		} else if ( op_kind == Token.Kinds.RPAREN ){
			//RPARENS trigger calculations
			while( operator_stack.peek().getKind() != Token.Kinds.LPAREN ) {
				this.simple_eval();			
			}
			assert operator_stack.pop().getKind() == Token.Kinds.LPAREN;
		} else if ( op_kind == Token.Kinds.LPAREN ) {
			//consume LPARENS
			return;
		} else {
			throw new Exception("Unknown operand kind "+op_kind.toString());
		}
		
		op_result = new Operand(Double.toString(result),result);
		operand_stack.push(op_result);
		
		if ( DEBUG )
			System.out.println("Result => "+operand_stack.peek());
		
		return ;
	}
	
	public double eval() throws Exception {
        // crude hack to handle situations like "<UNARY_OP><NUMBER>" only
		if ( m_toks.size() == 2 ) {
            Token tok0 = m_toks.get(0);
            Token tok1 = m_toks.get(1);
            if ( tok0.getKind() == Token.Kinds.ADD_OP ||
                 tok0.getKind() == Token.Kinds.SUB_OP ||
                 tok0.getKind() == Token.Kinds.UNARY_MINUS_OP ) {
                if ( tok1.getKind() == Token.Kinds.NUMBER) {
                    if ( tok0.getKind() != Token.Kinds.ADD_OP ) {
                        return -1.0*tok1.getNumericValue();
                    }
                    return tok1.getNumericValue();
                }
            }
        }

		Iterator<Token> tok_itr = m_toks.iterator();
		while( tok_itr.hasNext() ) {
			Token tok = tok_itr.next();
			if ( tok.getKind() == Token.Kinds.NUMBER ) {
				operand_stack.push(new Operand(tok));
				continue;
			}

			//no precedence yet. this tok is an operator
			// *, /,% have higher precedence over +,-
			if ( !operator_stack.isEmpty() ) {
				Operator top_operator = operator_stack.peek();
				while ( operator_stack.size() > 0 						
						&& Token.isPrecedent(top_operator,tok)) {
					//System.out.println("Precedence --->");					
					this.simple_eval();					
				}
			} 
			
			operator_stack.push(new Operator(tok));			
			
			if ( !operator_stack.isEmpty() && tok.getKind() == Token.Kinds.RPAREN ) {
				while(  operator_stack.size() > 0
						&& operator_stack.peek().getKind() != Token.Kinds.LPAREN ) {
					this.simple_eval();
				}
				assert operator_stack.pop().getKind() == Token.Kinds.LPAREN;
			}
		}
		
		while( !operator_stack.isEmpty() ) {
			this.simple_eval();
		}
		
		//overflow/underflow of stack indicates error in expr
		assert operand_stack.size() == 1;
		assert operator_stack.size() == 0;
		
		Operand val = operand_stack.pop();
		return val.getNumericValue();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {		
		
		//basic trig: C^(0) + S^(0) =  1
		String pi2str = Double.toString(Math.PI);
		String quart_pi2str = Double.toString(Math.PI/4);
						
		test_patterns();
		passing_tests();
	}
	
	public static void test_patterns() throws Exception {
		Pattern p = Pattern.compile("\\s+");
		String [] chunks = p.split("123 + 456 - 10 ^ 2");
		for( String s : chunks ) {
			System.out.println(s+"|"+s.length());
		}		
		test_pair(chunks,479.00,true);
		//test_pair(chunks,100,true); 
	}
	
	public static void passing_tests() throws Exception {
		String pi2str = Double.toString(Math.PI);
		String quart_pi2str = Double.toString(Math.PI/4);

        /// fails +8
        test_pair( new String [] {"+","8"},8.0,true);

		// 1 + 2^5 - 2 = 1  + 32 - 2 = 31
		test_pair( new String [] {"1","+","2","^","5","-","2"},31);
		
		//associativity of power should add up
		test_pair( new String [] {"2","^","3"},8.0);
		test_pair( new String [] {"2","^","3","+","-1"},7.0);
		test_pair( new String [] {"3","^","2"},9.0);
		test_pair( new String [] {"3","^","2","+","1"},10.0);
		test_pair( new String [] {"-","4","*","9"},-36.0);

		//tan
		test_pair( new String [] {"tan",quart_pi2str,"*","tan",quart_pi2str},1.0,!true);//verbose		
		test_pair( new String [] {"tan",pi2str},0.0,false);//verbos
		
		//basic trig: C^(0) + S^(0) =  1		
		test_pair( new String [] {"sin",pi2str,"*","sin",pi2str},0.0);//verbose
		test_pair( new String [] {"sin",pi2str,"*","sin",pi2str,"+","cos","3.1415","*","cos","3.1415"},1.0,false);//verbose
		
		//test brackets
		//test_pair(new String [] {"(","5","-","3",")","*","2"},4);
		test_pair(new String [] {"log10","10"},1);
		test_pair( new String [] {"log",Double.toString(Math.E)}, 1.0 );
				
		test_pair( new String [] {"55","+","95","-","10"}, 140.0 );
		test_pair( new String [] {"55","%","100.0","-","10"},45.0);
		test_pair( new String [] {"5","*","4","-","3"}, +17.0);
		test_pair( new String [] {"-37","-","10","-","5"},-52.0);
		test_pair( new String [] {"100","/","10","/","5"},2.0);
		test_pair(new String [] {"100","/","10","-","1","+", "1","/","5"},9.2);

		//sub is left associative
		test_pair(new String [] {"5","-","6","*","7","-","10"}, -47); 
		//String [] chunks = {"(","5","-","6",")","*","(","7","-","10",")"}; //3
		
		//% % and / are also left associative
		test_pair(new String [] {"5","%","10","%","20"}, 250);	
	}
	
	static void test_pair(String [] chunks,double expected_val) throws Exception {
		test_pair(chunks,expected_val,false);
	}
	
	static void test_pair(String [] chunks,double expected_val,boolean verbose) throws Exception {
		List<Token> toks = new ArrayList<Token>();
		for(String chunk:chunks) {
			toks.add( Token.parseToken(chunk) );
			if ( verbose )
				System.out.println(toks.get(toks.size()-1).toString());
		}
		RPNCalculator rpnc = new RPNCalculator(toks);
		double actual = rpnc.eval();
		if ( verbose || true )
			System.out.println("Testing => "+chunks[0]+chunks[1]+"|actual =>"+actual+"| expected => "+expected_val);
		
		boolean isExact = Double.compare(actual, expected_val)==0;
		boolean isAppx = (Math.abs(expected_val-actual) < 1e-6);
		boolean neither_appx_nor_exact = !(isExact || isAppx);
		
		if ( neither_appx_nor_exact ) {
			throw new Exception("Failed testing => "+chunks[0]+chunks[1]+"|actual =>"+actual+"| expected => "+expected_val);
		}
	}

}
