package gmedia.net.id.pullens.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class ServerLocalManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "GmediaPullensServer";

	// All Shared Preferences Keys
	private static final String TAG_SERVER = "serverurl";

	// Constructor
	public ServerLocalManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create session
	 * */
	public void saveServer(String server){

		editor.putString(TAG_SERVER, server);
		// commit changes
		editor.commit();
	}

	/**
	 * Get information
	 * */
	public HashMap<String, String> getServerDetails(){
		HashMap<String, String> servers = new HashMap<String, String>();

		servers.put(TAG_SERVER, pref.getString(TAG_SERVER, null));

		return servers;
	}

	public String getServer(){
		return pref.getString(TAG_SERVER, null);
	}
}
