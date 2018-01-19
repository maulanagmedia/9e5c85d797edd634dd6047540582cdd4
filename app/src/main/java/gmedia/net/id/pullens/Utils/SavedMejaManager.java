package gmedia.net.id.pullens.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SavedMejaManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "GmediaPullensMeja";

	// All Shared Preferences Keys
	private static final String TAG_KDMEJA = "kodemeja";
	private static final String TAG_NAMAMEJA = "namameja";

	// Constructor
	public SavedMejaManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create session
	 * */
	public void saveMeja(String kdmeja, String nmmeja){

		editor.putString(TAG_KDMEJA, kdmeja);
		editor.putString(TAG_NAMAMEJA, nmmeja);
		// commit changes
		editor.commit();
	}

	/**
	 * Get information
	 * */
	public HashMap<String, String> getMejaDetails(){
		HashMap<String, String> servers = new HashMap<String, String>();

		servers.put(TAG_KDMEJA, pref.getString(TAG_KDMEJA, null));
		servers.put(TAG_NAMAMEJA, pref.getString(TAG_NAMAMEJA, null));

		return servers;
	}

	public String getKdMeja(){
		return pref.getString(TAG_KDMEJA, null);
	}
	public String getNamaMeja(){
		return pref.getString(TAG_NAMAMEJA, null);
	}
}
