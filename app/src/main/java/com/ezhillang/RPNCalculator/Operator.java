package com.ezhillang.RPNCalculator;

/*
 * This code is distributed under Apache License.
 * (C) 2016 Ezhil Language Foundation
 */

public class Operator extends Token {
	public Operator(String raw,Kinds kind) {
			super(raw,kind);
			this.m_val = Double.NaN;
	}

	public Operator(Token tok) {
		super(tok);

		Kinds kind = tok.getKind();
		//token kind cannot be number
		assert kind != Kinds.NUMBER;
		this.m_val = Double.NaN;
	}
}
