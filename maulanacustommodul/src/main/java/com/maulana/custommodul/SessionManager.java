package com.maulana.custommodul;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context context;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;
	
	// Sharedpref file name
	private static final String PREF_NAME = "GmediaUserFinance";
	
	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";
	public static final String TAG_UID = "id_employee";
	public static final String TAG_EMAIL = "email";
	public static final String TAG_NAMA = "nama";
	public static final String TAG_USERNAME = "username";
	public static final String TAG_PASSWORD = "password";
	public static final String TAG_SAVED = "saved";

	// Constructor
	public SessionManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void createLoginSession(String uid, String email, String nama, String username, String password, String saved){

		editor.putBoolean(IS_LOGIN, true);
		
		editor.putString(TAG_UID, uid);

		editor.putString(TAG_EMAIL, email);

		editor.putString(TAG_NAMA, nama);

		editor.putString(TAG_USERNAME, username);

		editor.putString(TAG_PASSWORD, password);

		editor.putString(TAG_SAVED, saved);
		// commit changes
		editor.commit();
	}
	
	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// user nik
		user.put(TAG_UID, pref.getString(TAG_UID, null));

		user.put(TAG_EMAIL, pref.getString(TAG_EMAIL, null));

		user.put(TAG_NAMA, pref.getString(TAG_NAMA, null));

		user.put(TAG_USERNAME, pref.getString(TAG_USERNAME, null));

		user.put(TAG_PASSWORD, pref.getString(TAG_PASSWORD, null));

		user.put(TAG_SAVED, pref.getString(TAG_SAVED, null));

		// return user
		return user;
	}

	public String getUserInfo(String key){
		return pref.getString(key, null);
	}

	public String getNama(){
		return pref.getString(TAG_NAMA, null);
	}

	public String getUsername(){
		return pref.getString(TAG_USERNAME, null);
	}

	public String getPassword(){
		return pref.getString(TAG_PASSWORD, null);
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser(Intent logoutIntent){

		// Clearing all data from Shared Preferences
		try {
			editor.clear();
			editor.commit();
		}catch (Exception e){
			e.printStackTrace();
		}

		logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(logoutIntent);
		((Activity)context).finish();
		((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	/**
	 * Quick check for login
	 * **/
	// Get Login State

	public boolean isSaved(){
		if(getUserDetails().get(TAG_SAVED) != null && getUserDetails().get(TAG_SAVED).equals("1")){

			return true;
		}else{
			return false;
		}
	}

}
