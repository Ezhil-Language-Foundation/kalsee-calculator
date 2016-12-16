package com.urbantamil.kalsee;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */

public class KalseeSettings {
    final static String KALSEE_LONG_PRESS_MODE = "LongPressMode";
    final static String KALSEE_FONT_NAME = "FontName";
    final static String KALSEE_AUDIO_MODE = "AudioMode";
    private static String KALSEE_TAMIL_MENU_MODE="TamilMenuMode";

    final int KALSEE_FONT_SIZE = 22;// in pts

    static void init(Activity activity) {
        //tbd - initialize all the preferences if non exist before
    }

    /// Audio mode
    static void toggleAudioMode(Activity activity) {
        KalseeSettings.setAudioMode(activity,
                !KalseeSettings.getAudioMode(activity));
        return;
    }

    static void setTamilMenuMode(Activity activity,boolean isTamil_value) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putBoolean(KALSEE_TAMIL_MENU_MODE,isTamil_value);
        ed.commit();
    }

    static boolean getTamilMenuMode(Activity activity) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        // default mode is English menu :-)
        return sharedPrefs.getBoolean(KALSEE_TAMIL_MENU_MODE,false);
    }

    static void setAudioMode(Activity activity,boolean value) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putBoolean(KALSEE_AUDIO_MODE,value);
        ed.commit();
    }

    // return if the long-press mode is supposed to work
    static boolean getAudioMode(Activity activity) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);

        return sharedPrefs.getBoolean(KALSEE_AUDIO_MODE, true);
    }

    /// font
    static String getFontName(Activity activity) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        return sharedPrefs.getString(KALSEE_FONT_NAME,"Catamaran_Regular.ttf");
    }

    static void setFontName(Activity activity,String font) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putString(KALSEE_FONT_NAME,font);
        ed.commit();
    }

    /// click mode
    static void setLongPressMode(Activity activity, boolean value){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putBoolean(KALSEE_LONG_PRESS_MODE,value);
        ed.commit();
    }

    // return if the long-press mode is supposed to work
    static boolean getLongPressMode(Activity activity) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(activity);

        return sharedPrefs.getBoolean(KALSEE_LONG_PRESS_MODE, false);
    }

    public static void audioOn(KalseeActivity kalseeActivity) {
        KalseeSettings.setAudioMode(kalseeActivity,
                true);
    }

    public static void audioOff(KalseeActivity kalseeActivity) {
        KalseeSettings.setAudioMode(kalseeActivity,
                false);
    }

    // toggle it
    public static void toggleTanglishMode(KalseeActivity kalseeActivity) {
        setTamilMenuMode(kalseeActivity,
                    !getTamilMenuMode(kalseeActivity));
    }
}
