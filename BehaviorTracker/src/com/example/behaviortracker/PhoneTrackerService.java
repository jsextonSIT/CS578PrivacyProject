package com.example.behaviortracker;



import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

public class PhoneTrackerService extends Service{
	
	//Returned in onBind(Intent) method
	public final IBinder m = new LocalBinder(); 
	
	//Resources and String constants
	private Resources res;
	private String SHARED_PREFS_NAME;
	
	//To retrieve and edit information from shared preferences
	private SharedPreferences sp; 
	private SharedPreferences.Editor spe; 
	
	//Used for locating and watching the Call Logs.
	private ContentResolver cr;
	private CallLogObserver clo;
	
	public PhoneTrackerService(){
		super();
	}
	
	//Maintain reference to IBinder object to keep this service bound
	public IBinder onBind(Intent intent){
		return m;
	}
	
	public void onCreate(){
		//Put these here instead of in the Constructor on purpose - the context apparently isn't created until the onCreate method is called, as found through trial and error.
		res = getResources();
		SHARED_PREFS_NAME = res.getString(R.string.SHARED_PREFS_FILENAME);
		
		sp = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		spe = sp.edit();
		cr = getContentResolver();
		clo = new CallLogObserver(new Handler());
		cr.registerContentObserver(CallLog.Calls.CONTENT_URI, true, clo);
	}
	
	public void onDestroy(){
		cr.unregisterContentObserver(clo);
	}
	
	//This method is called whenever a change happens to the CallLog.
	//If it is called, it checks if there was a new call added to the CallLog, and if so, it adds its duration to the previous total duration.
	public void onCallLogChanged(){

		String[] columns = {CallLog.Calls.DATE, CallLog.Calls.DURATION};
		String order = CallLog.Calls.DATE + " DESC";
		Cursor curs = cr.query(CallLog.Calls.CONTENT_URI, columns, null, null, order);
		
		//Check if CallLog is empty first
		if (curs.moveToFirst()){
		}
		
	}
	
	//To initialize IBinder
	public class LocalBinder extends Binder {
        PhoneTrackerService getService() {
            return PhoneTrackerService.this;
        }
    }
	
	private class CallLogObserver extends ContentObserver{
		public CallLogObserver(Handler h){
			super(h);
		}
		
		public boolean deliverSelfNotifications(){
			return false;
		}
		
		//Called whenever this service is active AND a change happens to the CallLog.
		public void onChange(boolean selfChange){
			super.onChange(selfChange);
			onCallLogChanged();
		}
	}
}
