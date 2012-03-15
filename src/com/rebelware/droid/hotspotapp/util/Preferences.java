package com.rebelware.droid.hotspotapp.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class Preferences
{
	private static final String TAG = "Preferences";
	
	public static void clear(Context context, String pref)
	{
		SharedPreferences preferences = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		//if (preferences != null && preferences.getAll().size() == 0)
		if (preferences != null)
		{
			// first time, so setup default values
			Editor prefEditor = preferences.edit();			
			prefEditor.clear();
			prefEditor.commit();
		}
	}
	
	public static void setPreferences(Context context, String pref, String name, String value)
	{
		SharedPreferences preferences = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		//if (preferences != null && preferences.getAll().size() == 0)
		if (preferences != null)
		{
			// first time, so setup default values
			Editor prefEditor = preferences.edit();
			prefEditor.putString(name, value);
			
			prefEditor.commit();
		}
	}
	
	public static String getPreferences(Context context, String pref, String name)
	{
		String value = null;
		SharedPreferences preferences = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
    	if (preferences != null && preferences.getAll() != null && preferences.getAll().size() > 0)
    	{    		
    		Map settingsMap = preferences.getAll();

    		if (settingsMap.get(name) != null)
    		{
    			value = settingsMap.get(name).toString();
    		}    		
    	}
    	else
    	{
    		Log.e(TAG, "SharedPreferences not found");
    	}
    	
    	return value;
	}
}

