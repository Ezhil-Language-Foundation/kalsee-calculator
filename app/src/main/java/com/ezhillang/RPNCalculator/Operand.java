package com.ezhillang.RPNCalculator;

/*
 * This code is distributed under Apache License.
 * (C) 2016 Ezhil Language Foundation
 */

public class Operand extends Token {
	public Operand(String raw, double val) {
		super(raw,val);
	}

	public Operand(Token tok) {
		super(tok);
		Kinds kind = tok.getKind();
		assert kind == Kinds.NUMBER;
	}
}
