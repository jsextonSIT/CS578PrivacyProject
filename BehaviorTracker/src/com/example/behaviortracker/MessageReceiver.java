
package com.example.behaviortracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver{
	public void onReceive(Context context, Intent intent){
		// Initialize variables - allow for editing of SharedPreferences
		// Again, deliberately not done in constructor - since this is a receiver, accessing the context from the constructor is basically impossible.
		String SHARED_PREFS_NAME = context.getResources().getString(R.string.SHARED_PREFS_FILENAME);
		SharedPreferences sp = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor spe = sp.edit();
		
		
		//Update the count with the service that counts all uncounted sent messages.
		Intent i = new Intent(context, MessageTrackerService.class);
		context.startService(i);
	}
}
