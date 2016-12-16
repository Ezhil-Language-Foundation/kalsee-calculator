package com.urbantamil.kalsee;

/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */

public class ResIDObj {
	public int resID;
	public String fname;
	ResIDObj(int resID,String fname) {
		this.resID = resID;
		this.fname = fname;
	}

	@Override
	public String toString() {
		return fname;
	}
}
