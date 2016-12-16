/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */

package com.urbantamil.kalsee;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.ezhillang.RPNCalculator.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by muthu on 10/9/2016.
 */ /* Logical model */
public class Calculator {
    private String entry = null;
	public static final String TAG = "Kalsee.Calculator";
	String m_err_state = "";
    double m_result = 0.0;

	//clear or eval will update history
	private List<String> history = null;

	Calculator() {
	    entry = "";
		history = new ArrayList<String>();
        m_result = 0.0;
	}

    public void reset() {
        entry = "";
        m_err_state = "";
        m_result = 0.0;
    }

	public void addHistory(String expr) {
		history.add(expr);
	}

	// get last error message
	public String getErrMsg() {
		return "";
	}

	// evaluate the string
	public String eval(String input) throws Exception {
        // build tokens out of the input
        input = input.replace("x","*");
		Log.d(TAG,"Calculating expression =>"+input);
		SimpleExprParser parser = new SimpleExprParser(input);
		List<Token> parsed_tokens = parser.parse();
        Log.d(TAG,"Parsing completed");
		RPNCalculator calc = new RPNCalculator(parsed_tokens);
		double rval = calc.eval();
        Log.d(TAG,"RPN Calc yielded =>"+rval);

        // preserve result for later access
        m_result = rval;

        // when the result is integral we try to ensure we are in epsilon ballpark
		long long_rval = (long) rval;
		final double min_eps = 100.0*Double.MIN_VALUE;
		final DecimalFormat fmt5places = new DecimalFormat("#.#####");
        boolean isIntegerSufficient = Math.abs((double) long_rval - rval) <= min_eps;
        if ( isIntegerSufficient ) {
			return String.valueOf(long_rval);
		}

        String s_rval = fmt5places.format(rval).toString();
        Log.d(TAG,"return value from calculator => "+s_rval);
        return s_rval;
	}

    public double result() {
        return m_result;
    }

    public static ArrayList<String> getHistoryFromJSON(String str) {
        ArrayList<String> res = new ArrayList<String>();
        try {
            JSONObject jos = new JSONObject(str);
            Iterator<String> key = jos.keys();
            while (key.hasNext())
                res.add( jos.getString(key.next())) ;
        } catch(Exception e) {
            //pass
        }
        Log.d(TAG,"de-serialized elements total = "+res.size());
        return res;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public String getHistoryAsJSON() {
        JSONObject jos = new JSONObject();
        int index = 0;
        try {
            for (String hist : history) {
                jos.put("pos_"+String.valueOf(index++),hist);
            }
        } catch( Exception e) {
            //pass
        }

        Log.d(TAG,"History as JSON = \n\t  "+jos.toString());
        Log.d(TAG,"Total elements =\n\t"+index);
        return jos.toString();
    }

    public boolean hasHistory() {
        Log.d(TAG,"History size = "+history.size());
        return !history.isEmpty();
    }
}
