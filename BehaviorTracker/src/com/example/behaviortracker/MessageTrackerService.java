package com.example.behaviortracker;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MessageTrackerService extends Service {

	// Returned in onBind(Intent) method
	public final IBinder m = new LocalBinder(); 
	
	//Resources and String constants
	private Resources res;
	private String SHARED_PREFS_NAME;
	private String CONTENT_URL = "content://sms/";
	
	//To retrieve and edit information from shared preferences
	private SharedPreferences sp; 
	private SharedPreferences.Editor spe; 
	
	//Used for locating and watching the Message Logs.
	//Can only watch the main messaging app's logs - cannot find logs sent by other apps.
	private ContentResolver cr;
	private MessageLogObserver mlo;
	private Uri uri;

	//Constructor
	public MessageTrackerService() {
		super();	
	}

	//Maintain reference to IBinder object to keep this service bound
	//Currently unused
	public IBinder onBind(Intent intent) {
		return m;
	}

	public void onCreate() {
		//Put these here instead of in the Constructor on purpose - the context apparently isn't created until the onCreate method is called, as found through trial and error.
		//Initialization of variables
		res = getResources();
		SHARED_PREFS_NAME = res.getString(R.string.SHARED_PREFS_FILENAME);
		sp = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		spe = sp.edit();
		
		cr = getContentResolver();
		mlo = new MessageLogObserver(new Handler());
		uri = Uri.parse(CONTENT_URL);
		
		cr.registerContentObserver(uri, true, mlo);
		onMessageLogChanged();
	}

	public void onDestroy() {
		cr.unregisterContentObserver(mlo);
	}

	public void onMessageLogChanged(){
		String[] columns = {"date", "protocol"};
		Cursor curs = cr.query(uri, columns, null, null, null);
		
	}

	// To initialize IBinder
	public class LocalBinder extends Binder {
		MessageTrackerService getService() {
			return MessageTrackerService.this;
		}
	}

	private class MessageLogObserver extends ContentObserver {
		public MessageLogObserver(Handler h) {
			super(h);
		}

		public boolean deliverSelfNotifications() {
			return false;
		}

		//Called whenever a change happens to the message log AND the service is active.
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			onMessageLogChanged();
		}
	}
	
}
