package com.urbantamil.kalsee;

/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class KalseeHistoryFragment extends DialogFragment {
    public static final String TAG = "Kalsee.HistoryFragment";
    public static final String HISTORY = "HISTORY";
    public Activity ref_activity;
    RadioButton[] rbs = {null, null, null};
    int choice = 0; //default
    String fontname = "";
    HistoryUpdateListener m_update_listener;

    public interface HistoryUpdateListener {
        public void onHistoryUpdate(String history_expr,View frag);
    }

    void setHistoryListener(HistoryUpdateListener hul) {
        m_update_listener = hul;
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<String> history = null;
        String json_history = getArguments().getString(HISTORY);
        history = Calculator.getHistoryFromJSON(json_history);

        View tfont = inflater.inflate(R.layout.history_fragment,container,false);

        TextView tv = (TextView) tfont.findViewById(R.id.history_textview);
        tv.setText( getString(R.string.history_select_any)+" ("+history.size() +")");
        final ListView history_chooser = (ListView) tfont.findViewById(R.id.history_listview);

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item,history );
        history_chooser.setAdapter(dataAdapter);
        history_chooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // invoke parent and update history
                // using data adapter
                Log.d(TAG,"Selected history item at position = "+position);
                if ( m_update_listener!=null && position >= 0 && position < dataAdapter.getCount()) {
                    m_update_listener.onHistoryUpdate(dataAdapter.getItem(position), view);
                    Toast.makeText(getActivity(),R.string.history_selected,Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
        Button btn_dismiss = (Button) tfont.findViewById(R.id.tamilfont_view_dismiss);
        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return tfont;
    }
}
