package com.urbantamil.kalsee;

/**
 * This code is distributed under Apache License.
 *
 * (C) 2016 Muthiah Annamalai <ezhillang@gmail.com>
 * (C) 2016 Ezhil Language Foundation
 */

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ezhillang.RPNCalculator.RPNCalculator;
import com.tamil.Numeral;
import com.tamil.NumeralInfo;

public class KalseeActivity extends Activity implements SoundPlayNext.OnLoopCompletionListener, EditText.OnTouchListener,
    KalseeHistoryFragment.HistoryUpdateListener {
    public static String verb_ERROR = "error";
    public static String verb_CLEAR_DIGIT = "clear_digit";
    public static String verb_DELETE = "delete";
    public static String verb_MODULO = "modulo";
    public static String verb_PRODUCT = "perukkal";
    public static String verb_DIVISION = "vaguthal";
    public static String verb_SUBTRACTION = "kazhithal";
    public static String verb_ADDITION = "kootal";
    public static String verb_WRONG_INPUT = "wrong_input";
    public static String verb_PULLI = "pulli";
    public static String verb_RESULT = "result";
    public static String verb_PERC = "sathavigitham";
    public static String verb_ZERO = "units_0";
    public static String verb_ONE = "units_1";
    public static String verb_TWO = "units_2";
    public static String verb_THREE = "units_3";
    public static String verb_FOUR = "units_4";
    public static String verb_FIVE = "units_5";
    public static String verb_SIX = "units_6";
    public static String verb_SEVEN = "units_7";
    public static String verb_EIGHT = "units_8";
    public static String verb_NINE = "units_9";
    public static String verb_INFINITE = "infinity";
    public static String verb_NAN = "nan";

     AudioManager m_am;
     EditText edit = null;
     Button [] digit_btn = new Button [11];
     Button [] actn_btn = new Button [8];
     DigitCallback ref_digit_cb = null;
     SymbolCallback ref_sym_cb = null;
     PlayCallback ref_play_cb = null;
     boolean m_audio = true;
     SoundPlayNext m_playback;
     MediaPlayer mpl = null;
     Queue<ResIDObj> m_tmp_q = null;

    final String TAG = "KalseeActivity";
	 Calculator calc = new Calculator();
    private boolean m_enabled;

    public boolean useAudio() {
        boolean not_ringer_mute = m_am.getRingerMode() != AudioManager.RINGER_MODE_SILENT;
        boolean normal_volume = m_am.getMode() == AudioManager.MODE_NORMAL;
        return m_audio && (not_ringer_mute || normal_volume);
    }

    boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    void setEnabled(boolean status) {
        m_enabled = status;
    }

    @Override
    public void onLoopComplete(SoundPlayNext obj) {
        setEnabled(true);
    }

    public void showEmptyAlertBox() {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(R.string.kalsee)
                .setMessage(R.string.empty_history)
                .setPositiveButton(R.string.tamil_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).create();
        ad.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( v.getId() != R.id.calc_edit_text)
            return v.onTouchEvent(event);
        if (  event.getAction() != MotionEvent.ACTION_DOWN )
            return v.onTouchEvent(event);

        // if history is empty
        if ( !calc.hasHistory() ) {
            showEmptyAlertBox();
            return true;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            //run fragment for font choice
            FragmentManager fm = null;

            fm = getFragmentManager();
            // update fragment with copy of the desired fragment
            KalseeHistoryFragment fragment = new KalseeHistoryFragment();
            Bundle args = new Bundle();
            fragment.setHistoryListener(this);

            args.putString(KalseeHistoryFragment.HISTORY, calc.getHistoryAsJSON());
            fragment.setArguments(args);
            Log.d(TAG, "making fragment and completed adding font information bundle");

            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(TamilFontFragment.TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ft.commit();
            Log.d(TAG, "Removed any possible old fragments floating around");

            Log.d(TAG, "Prepare to launch DialogFragment");
            fragment.setCancelable(true);
            fragment.show(fm, TamilFontFragment.TAG);

        } else {
            Toast.makeText(this, R.string.android_version_doesnthavefunction,Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onHistoryUpdate(String history_expr, View frag) {
        Log.d(TAG,"history expr => "+history_expr);
        edit.setText(history_expr);
        edit.postInvalidate();
        return;
    }

    class DigitCallback implements OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View btn_in) {
            Button btn_ref = (Button) btn_in;
            Integer ival = (Integer) btn_ref.getTag();
            edit.postInvalidate();

            if ( ival >= 0 ) {
                // play tune
                play_a_tune(ival.doubleValue());
                //add text to entry at hand
                edit.append( ival.toString() );
            } else if ( ival == -1 ) {
                // play sound "pulli"
                play_a_word(verb_PULLI);
                // -1 is wiring of '.' symbol
                edit.append(".");
            }
            edit.postInvalidate();
            return;
        }

         @Override
         public boolean onLongClick(View v) {
             onClick(v);
             return true;
         }
     }

    // play button ID
    private void playButtonId(int btn_id) {
        if ( !useAudio() ) {
            // no audio senora
            return;
        }

        switch (btn_id) {
            case R.id.calc_btn_add:
                KalseeActivity.this.play_a_word(verb_ADDITION);
                break;
            case R.id.calc_btn_sub:
                KalseeActivity.this.play_a_word(verb_SUBTRACTION);
                break;
            case R.id.calc_btn_mul:
                KalseeActivity.this.play_a_word(verb_PRODUCT);
                break;
            case R.id.calc_btn_div:
                KalseeActivity.this.play_a_word(verb_DIVISION);
                break;
            case R.id.calc_btn_dot:
                KalseeActivity.this.play_a_word(verb_PULLI);
                break;
            case R.id.calc_btn_equals:
                KalseeActivity.this.play_a_word(verb_RESULT);
                break;
            case R.id.calc_btn_perc:
                KalseeActivity.this.play_a_word(verb_PERC);
                break;
            case R.id.calc_btn_AC:
                play_a_word(verb_DELETE);
                break;
            case R.id.calc_btn_pm:
                // +/-
                play_a_word(verb_ADDITION);
                play_a_word(verb_SUBTRACTION);

                break;
            case R.id.calc_btn_0:
                play_a_word(verb_ZERO);
                break;
            case R.id.calc_btn_1:
                play_a_word(verb_ONE);
                break;
            case R.id.calc_btn_2:
                play_a_word(verb_TWO);
                break;
            case R.id.calc_btn_3:
                play_a_word(verb_THREE);
                break;
            case R.id.calc_btn_4:
                play_a_word(verb_FOUR);
                break;
            case R.id.calc_btn_5:
                play_a_word(verb_FIVE);
                break;
            case R.id.calc_btn_6:
                play_a_word(verb_SIX);
                break;
            case R.id.calc_btn_7:
                play_a_word(verb_SEVEN);
                break;
            case R.id.calc_btn_8:
                play_a_word(verb_EIGHT);
                break;
            case R.id.calc_btn_9:
                play_a_word(verb_NINE);
                break;
        }
    }

    class PlayCallback implements OnClickListener, View.OnLongClickListener {

        @Override
        public void onClick(View btn_in) {
            playButtonId(btn_in.getId());
        }

        @Override
        public boolean onLongClick(View v) {
            onClick(v);
            return true;
        }
    }

    class SymbolCallback implements OnClickListener, View.OnLongClickListener

    {
        @Override
        public void onClick(View btn_in) {
            Button btn_ref = (Button) btn_in;
            String strval = (String) btn_ref.getTag();
            String curr_edit_val = edit.getText().toString();

            Log.d(TAG,"symbol cb-> |"+strval+"|");
            int btn_id = btn_in.getId();

            playButtonId(btn_id);

            if ( btn_id == R.id.calc_btn_AC ) {
                do_clear_action(btn_in,curr_edit_val);
                return;
            }

            if (btn_id == R.id.calc_btn_pm ) {
               do_plus_minus_action(btn_in,curr_edit_val);
               return;
            }

            if ( btn_id == R.id.calc_btn_equals ) {
                do_calculate_action(btn_in,curr_edit_val);
                return;
            }

            Log.d(TAG,"appending to edit ["+ curr_edit_val +"] ->"+strval);
            //add tag to entry at hand
            edit.append( strval );
            edit.postInvalidate();
            return;
        }


         private void do_plus_minus_action(View btn_in, String curr_edit_val) {
             //toggle + -> - for the number
             curr_edit_val = curr_edit_val.trim();
             char [] digits = curr_edit_val.toCharArray();
             //empty widget entry
             if ( digits.length < 1  )
                 return;

             if ( digits[0] == '-' ) {
                 digits[0] = '+';
                 curr_edit_val = String.valueOf(digits);
             } else if ( digits[0] == '+')  {
                 digits[0] = '-';
                 curr_edit_val = String.valueOf(digits);
             } else /* implicit + */ {
                 curr_edit_val = "-"+curr_edit_val;
             }
             Log.d(TAG,"plus minus change =>"+curr_edit_val);
             edit.setText(curr_edit_val);
             edit.postInvalidate();
             return;
         }

         private void do_calculate_action(View btn_in, String curr_edit_val) {
             // force calculation
             calc.addHistory(curr_edit_val);
             Log.d(TAG,curr_edit_val);
             try {
                 edit.setText( calc.eval(curr_edit_val) );
                 play_a_tune(calc.result());
             } catch(Exception e) {
                 if ( useAudio() )
                     play_a_word(verb_WRONG_INPUT);
                 String cannot_calc = getString(R.string.cannot_calculate);
                 String error_reason = getString(R.string.error_reason);
                 //+error_reason+" \n"+e.getMessage()
				 build_and_show(cannot_calc, cannot_calc+" '"+curr_edit_val+"'");
                 calc.reset();
             }
             edit.postInvalidate();
            return;
         }

         private void do_clear_action(View btn_in,String curr_edit_val) {
             // clear action
             calc.reset();
             edit.setText("");
             edit.postInvalidate();
			 return;
         }

        @Override
        public boolean onLongClick(View v) {
            onClick(v);
            return true;
        }
    }
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         //hide title bar : useful in other apps/activities
         //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

         if ( !isTablet() ) {

             setContentView(R.layout.apple_calc_small);

         } else
         setContentView(R.layout.apple_calc);

         m_am = (AudioManager) getSystemService(AUDIO_SERVICE);
         m_enabled = true;
         m_tmp_q = new ArrayDeque<ResIDObj>();

         try {
             RPNCalculator.main(null);
             Log.d(TAG,"RPNCalculator => tests passed");
         } catch(Exception e) {
             Log.d(TAG,"RPNCalculator => tests failed");
             Log.d(TAG,e.getMessage());
         }

		 edit = (EditText) findViewById(R.id.calc_edit_text);
		 edit.setClickable(false);
         edit.setCursorVisible(false);
         edit.setFocusable(false);
         edit.setFocusableInTouchMode(false);
         edit.setText("");
         edit.setOnTouchListener(this);
         edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             //swallow all the letters -> /dev/zero
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                                           KeyEvent event) {
                 edit.setClickable(false);
                 edit.setCursorVisible(false);
                 edit.setFocusable(false);
                 edit.setFocusableInTouchMode(false);

                 return true;
             }
         });

         // hide soft input keyboard
         {
             getWindow().setSoftInputMode(
                     WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
             );
             View view = this.getCurrentFocus();
             if (view != null) {
                 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

             }
         }

         ref_play_cb = new PlayCallback();
         ref_digit_cb = new DigitCallback();
         ref_sym_cb = new SymbolCallback();

         // setup tags and callbacks for calculator
		 for(int num=0; num<10;num++) {
			 digit_btn[num] = (Button) findViewById( getViewIdForName("calc_btn_"+String.valueOf(num)) );
			 digit_btn[num].setTag(Integer.valueOf(num));
			 digit_btn[num].setOnClickListener(ref_digit_cb);
             digit_btn[num].setOnLongClickListener(ref_play_cb);
		 }
		 
		 //fake actn button - DOT
		 digit_btn[10] = (Button) findViewById( R.id.calc_btn_dot );
		 digit_btn[10].setTag(Integer.valueOf(-1));
		 digit_btn[10].setOnClickListener(ref_digit_cb);

		 actn_btn[0] = (Button) findViewById( R.id.calc_btn_AC );
		 actn_btn[0].setTag( new String("AC") );
		 actn_btn[0].setOnClickListener(ref_sym_cb);
				 
		 actn_btn[1] = (Button) findViewById( R.id.calc_btn_add );
		 actn_btn[1].setTag( new String("+") );
		 actn_btn[1].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[2] = (Button) findViewById( R.id.calc_btn_sub );
		 actn_btn[2].setTag( new String("-") );
		 actn_btn[2].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[3] = (Button) findViewById( R.id.calc_btn_mul );
		 actn_btn[3].setTag( new String("x") );
		 actn_btn[3].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[4] = (Button) findViewById( R.id.calc_btn_div );
		 actn_btn[4].setTag( new String("/") );
		 actn_btn[4].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[5] = (Button) findViewById( R.id.calc_btn_perc );
		 actn_btn[5].setTag( new String("%") );
		 actn_btn[5].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[6] = (Button) findViewById( R.id.calc_btn_equals );
		 actn_btn[6].setTag( new String("=") );
		 actn_btn[6].setOnClickListener(ref_sym_cb);
		 
		 actn_btn[7] = (Button) findViewById( R.id.calc_btn_pm );
		 actn_btn[7].setTag( new String("+/-") );
		 actn_btn[7].setOnClickListener(ref_sym_cb);

         for(int i=0;i<actn_btn.length;i++) {
             actn_btn[i].setOnLongClickListener(ref_play_cb);
         }
         updateFonts();
	 }
	 
	 ///////////////  U T I L I T Y //////// F U N C T I O N S ///////////////
	 protected void play_a_tune(double val) {
            if ( !useAudio() )
                 return;

            if ( Double.isInfinite(val) ) {
                play_a_word(verb_INFINITE);
                return;
            } else if ( Double.isNaN(val)) {
                play_a_word(verb_NAN);
                return;
            }

            Queue<ResIDObj> wavQ = new ArrayDeque<ResIDObj>();

            wavQ = queueNumber(val);
            if ( !m_enabled ) {
                m_tmp_q.addAll(wavQ);
                return;
            }

            m_tmp_q.addAll(wavQ);
            wavQ = m_tmp_q;
            m_playback = new SoundPlayNext(wavQ,this);
            m_playback.setOnLoopCompletionListener(this);
            mpl = MediaPlayer.create(this,wavQ.remove().resID);
            mpl.setOnCompletionListener(m_playback);
            setEnabled(false);
            mpl.start();
            //start!
            Log.d(TAG,"Started playback");
    }

    protected void play_a_word(String verb) {
        if ( !useAudio() )
            return;

        Queue<ResIDObj> wavQ = new ArrayDeque<ResIDObj>();

        try {
            wavQ.add(getResourceFile(verb));
        } catch (Exception e) {

        }

        if ( !m_enabled ) {
            m_tmp_q.addAll(wavQ);
            return;
        }

        m_tmp_q.addAll(wavQ);
        wavQ = m_tmp_q;

        m_playback = new SoundPlayNext(wavQ,this);
        m_playback.setOnLoopCompletionListener(this);
        mpl = MediaPlayer.create(this,wavQ.remove().resID);
        mpl.setOnCompletionListener(m_playback);
        setEnabled(false);
        mpl.start();
        //start!
        Log.d(TAG,"Started playback");

        return;
    }

    //public utility function
		public int getViewIdForName(String name)  {
			int btnID = -1;
			try {
			    Class<R.id> res = R.id.class;
			    Field field = res.getField(name);
			    btnID = field.getInt(null);		    
			}
			catch (Exception e) {
			    Log.e(TAG, "Failure to get btn id.", e);		    
			}
			return btnID;
		}

        void build_and_show(final String s_title, final String s_msg) {
            new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                AlertDialog ad = build(s_title, s_msg);
                                ad.show();
                            }
                        }
                    }.run();
        }

        AlertDialog build(String s_title, String s_msg) {
            return new AlertDialog.Builder(this)
                    .setTitle(s_title)
                    .setMessage(s_msg)
                    .setPositiveButton(R.string.tamil_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).create();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ( KalseeSettings.getTamilMenuMode(this) )
            getMenuInflater().inflate(R.menu.settings, menu);
        else
            getMenuInflater().inflate(R.menu.settings_tanglish, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void updateAudio() {
        m_audio = KalseeSettings.getAudioMode(this);
        Log.d(TAG,"Updated Audio mode to =>"+String.valueOf(m_audio));

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(true);
        alertDialog.setTitle(R.string.kalsee);
        alertDialog.setMessage(getResources().getString(R.string.kalsee_audio_settingsupdate));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.kalsee_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //pass
                    }
        });
        if ( !m_audio )
            alertDialog.setIcon(R.drawable.ic_volume_off_black_24dp);
        else
            alertDialog.setIcon(R.drawable.ic_volume_up_black_24dp);
        alertDialog.show();
    }

    public void updateCallbacks() {
        boolean use_long_press = KalseeSettings.getLongPressMode(this);

        //blow away old callback hooks
        for(int i=0; i<digit_btn.length; i++) {
            digit_btn[i].setOnClickListener(null);
            digit_btn[i].setOnLongClickListener(null);
        }
        for(int i=0; i<actn_btn.length; i++) {
            actn_btn[i].setOnClickListener(null);
            actn_btn[i].setOnLongClickListener(null);
        }

        if ( use_long_press ) {
            //set new hooks - long press becomes click mode
            for(int i=0; i<digit_btn.length; i++) {
                digit_btn[i].setOnClickListener(ref_play_cb);
                digit_btn[i].setOnLongClickListener(ref_digit_cb);
            }
            for(int i=0; i<actn_btn.length; i++) {
                actn_btn[i].setOnClickListener(ref_play_cb);
                actn_btn[i].setOnLongClickListener(ref_sym_cb);
            }
        }  else {
            //set new hooks - short press is actual click; long press for audio input
            for(int i=0; i<digit_btn.length; i++) {
                digit_btn[i].setOnClickListener(ref_digit_cb);
                digit_btn[i].setOnLongClickListener(ref_play_cb);
            }
            for(int i=0; i<actn_btn.length; i++) {
                actn_btn[i].setOnClickListener(ref_sym_cb);
                actn_btn[i].setOnLongClickListener(ref_play_cb);
            }
        }
        // tbd
    }

    public void updateFonts() {
        String font_choice = KalseeSettings.getFontName(this);

        // pickup the font
        Typeface ref_font = Typeface.DEFAULT;

        // default font
        if ( font_choice.equals("bold") )
            return;

        try {
            ref_font = Typeface.createFromAsset( getResources().getAssets(), "fonts/"+font_choice);
            Log.d(TAG,"Loaded font => "+font_choice);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"cannot load chosen font ["+font_choice+"]");
        }

        // update action buttons
        for(int i=0; i<actn_btn.length;i++) {
            actn_btn[i].setTypeface(ref_font);
            actn_btn[i].postInvalidate();
        }
        // update digit buttons
        for(int i=0; i<digit_btn.length;i++) {
            digit_btn[i].setTypeface(ref_font);
            digit_btn[i].postInvalidate();
        }
        edit.setTypeface(ref_font);
        edit.postInvalidate();
        Log.d(TAG,"All font faces updated");

        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogInterface.OnClickListener mtDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pass
            }
        };

        switch (item.getItemId()) {
            case R.id.menu_audio_on:
                KalseeSettings.audioOn(this);
                updateAudio();
                Toast.makeText(getApplicationContext(), R.string.kalsee_audio_settingsupdate,Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_audio_off:
                KalseeSettings.audioOff(this);
                updateAudio();
                Toast.makeText(getApplicationContext(),R.string.kalsee_audio_settingsupdate,Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_tanglish:
                boolean isTamilMenuMode = KalseeSettings.getTamilMenuMode(this);
                KalseeSettings.toggleTanglishMode(this);
                AlertDialog ad = null;
                if ( isTamilMenuMode )
                    ad = build(getString(R.string.kalsee),getString(R.string.restart_activity));
                else
                    ad = build(getString(R.string.kalsee),getString(R.string.tanglish_restart_activity));
                ad.show();
                ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // submit pending intent
                        Context context = getApplication();
                        Intent mStartActivity = new Intent(context, KalseeActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

                        finish(); //close parent activity
                    }
                });
                break;
            case R.id.menu_font_name:
                String fontname = KalseeSettings.getFontName(this);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    //run fragment for font choice
                    FragmentManager fm = null;

                    fm = getFragmentManager();
                    // updapte fragment with copy of the desired fragment
                    DialogFragment fragment = new TamilFontFragment();
                    Bundle args = new Bundle();
                    args.putString(KalseeSettings.KALSEE_FONT_NAME, fontname);
                    fragment.setArguments(args);
                    Log.d(TAG, "making fragment and completed adding font information bundle");

                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag(TamilFontFragment.TAG);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    ft.commit();
                    Log.d(TAG, "Removed any possible old fragments floating around");

                    Log.d(TAG, "Prepare to launch DialogFragment");
                    fragment.setCancelable(true);
                    fragment.show(fm, TamilFontFragment.TAG);

                    //return value to be stored in prefs.
                    updateFonts();
                    String fontname2 = KalseeSettings.getFontName(this);
                    //this.getWindow().makeActive();
                    Toast.makeText(getApplicationContext(), getString(R.string.font_updated) + fontname2 + "]",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.android_version_doesnthavefunction,Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_long_press_mode:
                KalseeSettings.setLongPressMode(this,true);
                updateCallbacks();

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setCancelable(true);
                alertDialog.setTitle(R.string.kalsee);
                alertDialog.setIcon(R.drawable.ic_accessible_black_24dp);
                alertDialog.setMessage(getResources().getString(R.string.long_press_mode));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.kalsee_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //pass
                            }
                        });
                alertDialog.show();
                break;
            case R.id.menu_short_press_mode:
                KalseeSettings.setLongPressMode(this,false);
                updateCallbacks();

                AlertDialog salertDialog = new AlertDialog.Builder(this).create();
                salertDialog.setCancelable(true);
                salertDialog.setTitle(R.string.kalsee);
                salertDialog.setIcon(android.R.drawable.ic_popup_reminder);
                salertDialog.setMessage(getResources().getString(R.string.short_press_button));
                salertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.kalsee_ok),
                        mtDialogListener);
                salertDialog.show();
                break;
            case R.id.menu_credits:
                AlertDialog calertDialog = new AlertDialog.Builder(this).create();
                calertDialog.setCancelable(true);
                calertDialog.setTitle(R.string.kalsee);
                calertDialog.setIcon(R.drawable.concept_calculator_logo);
                calertDialog.setMessage(getResources().getString(R.string.credits_message));
                calertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.kalsee_ok),
                        mtDialogListener);
                calertDialog.show();
                break;
        }

        return true;
    }

    ///////////// numeral playback utilities //////////////////
    //public utility function
    private int getRawIDForFileName(String filename) throws Exception {
        try {
            Class<R.raw> res = R.raw.class;
            Field field = res.getField(filename);
            int waveFileID = field.getInt(null);
            return waveFileID;
        }
        catch (Exception e) {
            Log.e(TAG, "Failure to get resource id while playing tune=>"+filename, e);
            throw new Exception(e);
        }
    }
    ///////////////  - queue a number by queueing all the resource files dependent on it -  //////////

    Queue<ResIDObj> queueNumber(double val)  {
        Log.d(TAG,"add the stuff to Q "+val);

        try {
            //enqueue the word numeral form of the file
            double val_pos = Math.abs(val);
            int ndigits = 5; //5 fractional digits is OK
            if (val_pos > 1.0) {
                ndigits = (int)(8 + Math.log10(val_pos));
            }

            NumeralInfo numVal_info = Numeral.num2tamilstr(val_pos);
            // this contains the names of the files
            List<String> wave_file_names = numVal_info.getFilenames();
            Queue<ResIDObj> q = new ArrayDeque<ResIDObj>();
            if ( val < 0.0 ) {
                q.add(getResourceFile(verb_SUBTRACTION));
            }

            for (int idx = 0; idx < wave_file_names.size(); idx++) {
                // get their RAW ID
                String numeral_filename = wave_file_names.get(idx);
                Log.d(TAG, "At idx=" + idx + " file => " + numeral_filename);
                q.add(getResourceFile(numeral_filename));
            }

            if ( q.size() > ndigits ) {
                //drop some excess precision digits
                ArrayDeque adq = (ArrayDeque)q;
                while( adq.size() >= ndigits ) {
                    adq.removeLast();
                }
            }

            return q;
        } catch (Exception e) {
            //pass
            e.printStackTrace();
        }
        return null;
    }

    // queue a "verb" that is present in the resources directory to be played
    // back to the audio stream!-
    public ResIDObj getResourceFile(String verbname) throws Exception {
        int resID = getRawIDForFileName( verbname);
        return new ResIDObj(resID,verbname);
    }
}
