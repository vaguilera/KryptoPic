package com.nocturns.cryptopic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesMgr {

	public static final String PREFS_NAME = "KryptoPic";

    public static byte[] LoadPreferences(Context ac) 
    {
    	SharedPreferences settings = ac.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	String UserMd5 = settings.getString("Password", null);
    	if (UserMd5 == null) 
    		return null; 
    	else
    		return GetBytes(UserMd5);
    }

    public static String GetHexString(byte[] buffer) 
    {
    	final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[buffer.length * 2];
        int v;
        for ( int j = 0; j < buffer.length; j++ ) {
            v = buffer[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] GetBytes(String s) {
       
    	byte[] data = null;
    	try {
	    	int len = s.length();
	        data = new byte[len / 2];
	        for (int i = 0; i < len; i += 2) {
	            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                                 + Character.digit(s.charAt(i+1), 16));
	        }
        } catch (Exception e) {
        	Log.d("Preferences","Error converting String to bytes");
        	return null;
        }
        
        return data;
    }
    
    public static void SavePreferences(Context ac, String field, byte[] value)
    {
    	SharedPreferences settings = ac.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(field, GetHexString(value));
        

        // Commit the edits!
        editor.commit();
    }
    
    public static void ClearPreferences(Context ac)
    {
    	SharedPreferences settings = ac.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        
        // Commit the edits!
        editor.commit();
    }
    
   
}
