package com.example.behaviortracker;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.location.LocationListener;

public class GPSTrackerService extends Service implements LocationListener {
	private boolean mLoggingState;
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public synchronized void startLogging()
	   {//check if not logging
	      if (!mLoggingState)
	      {
	         //startNewTrack();
	         //sendRequestLocationUpdatesMessage();
	         //sendRequestStatusUpdateMessage();
	         mLoggingState = true;//start logging
	         //updateWakeLock();
	         //startNotification();
	         //crashProtectState();
	         //broadCastLoggingState();
	      }
	   }
	protected boolean isLogging()
	   {
	      return this.mLoggingState;
	   }

}
