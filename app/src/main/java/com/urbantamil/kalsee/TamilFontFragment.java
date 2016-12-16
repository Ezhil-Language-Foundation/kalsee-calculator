package com.urbantamil.kalsee;

/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TamilFontFragment extends DialogFragment {
    public static final String TAG = "TamilFontFragment";
    public Activity ref_activity;
    TextView textView = null;
    RadioButton[] rbs = {null, null, null};
    int choice = 0; //default
    String fontname = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View tfont = inflater.inflate(R.layout.tamilfont_fragment,container,false);
        textView = (TextView) tfont.findViewById(R.id.textView);
        textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        textView.setText(R.string.tamilfont_cinderella_story);

        Spinner font_chooser = (Spinner) tfont.findViewById(R.id.spinner);
        List<String> font_choices = new ArrayList<String>();
        font_choices.add("Catamaran_Regular.ttf");
        for (int i = 1; i <= 10; i++) {
            java.util.Formatter f = new java.util.Formatter();
            font_choices.add(f.format("Uni Ila.Sundaram-%02d.ttf", i).toString());
            break;
        }
        java.util.Collections.sort( font_choices );

        if ( savedInstanceState != null ) {
            if (savedInstanceState.containsKey(KalseeSettings.KALSEE_FONT_NAME)) {
                fontname = (String) savedInstanceState.get(KalseeSettings.KALSEE_FONT_NAME);
            }
        } else {
            ref_activity = getActivity();
        }

        Log.d(TAG,"activity =>"+ref_activity.getLocalClassName());
        Log.d(TAG,"Fontname =>"+fontname);

        //final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
        //        android.R.layout.simple_spinner_item,font_choices );
        final FontViewAdapter dataAdapter = new FontViewAdapter(font_choices);
        font_chooser.setAdapter(dataAdapter);
        font_chooser.setOnItemSelectedListener(dataAdapter);

        Button btn_dismiss = (Button) tfont.findViewById(R.id.tamilfont_view_dismiss);
        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // forward the bundle info into GUI choice
        int pos = java.util.Collections.binarySearch(font_choices,fontname);
        font_chooser.setSelection( ( pos < 0 ) ? choice : pos);
        Log.d(TAG,"Set default font @ position =>"+String.valueOf(pos));

        return tfont;
    }

    private Typeface getTypeface(Context ctx, String fontname) {
        Typeface typeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/" + fontname);
        return typeface;
    }

    // this adapter also shows the widgets in their native typeface - a small enhancement
    private class FontViewAdapter extends ArrayAdapter<String> implements AdapterView.OnItemSelectedListener {
        public FontViewAdapter(List<String> font_choices) {
            super(ref_activity.getApplicationContext(),
                    android.R.layout.simple_spinner_item, font_choices);
            setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parentGroup) {
            View rval = super.getView(pos, convertView, parentGroup);

            TextView tv = (TextView) rval.findViewById(android.R.id.text1);
            try {
                String fontname = (String) getItem(pos);
                if (tv != null)
                    tv.setTypeface(getTypeface(getActivity().getApplicationContext(), fontname));
            }catch (Exception e) {
                e.printStackTrace();
            }
            return rval;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String fontname = (String) getItem(position);
            Typeface typeface = getTypeface(ref_activity.getApplicationContext(), fontname);
            textView.setTypeface(typeface);
            textView.invalidate();
            Log.d(TAG, "We choose font => " + fontname);
            KalseeSettings.setFontName(ref_activity, fontname);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //pass
        }
    }
}
