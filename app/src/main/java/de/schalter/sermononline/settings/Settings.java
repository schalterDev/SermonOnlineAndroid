package de.schalter.sermononline.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * Created by martin on 29.11.17.
 */

public class Settings {

    public static final String SHOW_ADS = "showAds";
    public static final boolean SHOW_ADS_DEFAULT = false;

    private static SharedPreferences settings;

    public static boolean getBoolean(String tag, boolean defaultBoolean) {
        return settings.getBoolean(tag, defaultBoolean);
    }

    public static String getString(String tag, String defaultString) {
        return settings.getString(tag, defaultString);
    }

    public static int getInt(String tag, int defaultInt) {
        return settings.getInt(tag, defaultInt);
    }

    public static void initSettings(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getSystemLanguageCode() {
        String language = Locale.getDefault().getLanguage();
        if(language.equals("de")) {
            return "de";
        } else {
            return "en";
        }
    }
}
