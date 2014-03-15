package com.example.behaviortracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneReceiver extends BroadcastReceiver{
	//This method is invoked upon completed startup of phone, and upon change in phone state (start/end of call).
	public void onReceive(Context context, Intent intent){
		Intent i = new Intent(context, PhoneTrackerService.class);
		context.startService(i);
	}
}
