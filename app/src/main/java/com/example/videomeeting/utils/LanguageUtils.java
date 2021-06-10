package com.example.videomeeting.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageUtils {

    /**
     * Changes the language of the app
     * @param context
     * @param languageCode Two letters that will indicate the Locale code of the language
     */
    public static void setLocale(Context context, String languageCode) {
        Resources activityRes = context.getResources();
        Configuration activityConf = activityRes.getConfiguration();
        Locale newLocale = new Locale(languageCode);
        activityConf.setLocale(newLocale);
        activityRes.updateConfiguration(activityConf, activityRes.getDisplayMetrics());

        Resources applicationRes = context.getResources();
        Configuration applicationConf = applicationRes.getConfiguration();
        applicationConf.setLocale(newLocale);
        applicationRes.updateConfiguration(applicationConf, applicationRes.getDisplayMetrics());
    }

}