package com.example.behaviortracker;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GPSTrackerService extends Service {
	private SQLiteDatabase db;
	private LocationManager lm;
	private LocationListener locationListener;
	public ArrayList<GPSPoint> GPSpoints;
	// database variables
	public static final String DATABASE_NAME = "GPSTRACKERDB";
	public static final String POINTS_TABLE_NAME = "LOCATION_POINTS";
	public static final String TRIPS_TABLE_NAME = "TRIPS";
	private final DecimalFormat sevenSigDigits = new DecimalFormat("0.#######");
	private final DateFormat datestampFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final DateFormat timestampFormat = new SimpleDateFormat(
			"HH:mm:ss");
	
	// variables for time/distance between logging location
	private static long minTimeMillis = 10000;
	private static long minDistanceMeters = 0;//was 10
	private static float minAccuracyMeters = 50;//was 35
	// possibly add options in future to change these

	private int lastStatus = 0;
	private static boolean showingDebugToast = true;

	private static final String tag = "GPSTrackerService";
	private double id;
	private void startTrackerService() {
		Log.i(tag, "startTrackerService");
		// ---use the LocationManager class to obtain GPS locations---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationListener = new MyLocationListener();

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMillis,
				minDistanceMeters, locationListener);
		initDatabase();
	}
	public void addIds(double i){
		this.id += i;
	}
	private void initDatabase() {
		db = this.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.OPEN_READWRITE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + POINTS_TABLE_NAME
				+ " (ID INT NOT NULL, GMTDATESTAMP DATETIME, LATITUDE REAL, LONGITUDE REAL,"
				+ " ALTITUDE REAL, ACCURACY REAL, SPEED REAL, BEARING REAL, PRIMARY KEY(ID) );");
		db.close();
		Log.i(tag, "Database opened ok");
		id = 0;
	}

	private void shutdownTrackerService() {
		lm.removeUpdates(locationListener);
	}
	
	
	
	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			Log.i("location changed", "called");
			if (loc != null) {
				boolean pointIsRecorded = false;
				try {
					if (loc.hasAccuracy()
							&& loc.getAccuracy() <= minAccuracyMeters) {
						pointIsRecorded = true;
						GregorianCalendar greg = new GregorianCalendar();
						greg.setTimeZone(TimeZone.getTimeZone("GMT"));
						TimeZone tz = greg.getTimeZone();
						int offset = tz.getOffset(System.currentTimeMillis());
						greg.add(Calendar.SECOND, (offset / 1000) * -1);
						StringBuffer queryBuf = new StringBuffer();
							
						//String date = timestampFormat2.format(greg.getTime());
						
						//GPSPoint newPoint = new GPSPoint(loc.getLatitude(), loc.getLongitude(), date);
						//newPoint.logPoint();
						//GPSpoints.add(newPoint);
						queryBuf.append("INSERT INTO "
								+ POINTS_TABLE_NAME
								+ " (ID, GMTDATESTAMP, LATITUDE, LONGITUDE, ALTITUDE, ACCURACY, SPEED, BEARING) VALUES ("
								+ Double.toString(id)
								+ ", "
								+"'"
								+ datestampFormat.format(greg.getTime())
								+ "', "
								//+ "'"
								//+ timestampFormat.format(greg.getTime())
								//+ "', "
								+ loc.getLatitude()
								+ ", "
								+ loc.getLongitude()
								+ ", "
								+ (loc.hasAltitude() ? loc.getAltitude()
										: "NULL")
								+ ", "
								+ (loc.hasAccuracy() ? loc.getAccuracy()
										: "NULL")
								+ ", "
								+ (loc.hasSpeed() ? loc.getSpeed() : "NULL")
								+ ", "
								+ (loc.hasBearing() ? loc.getBearing() : "NULL")
								+ ");");
						Log.i(tag, queryBuf.toString());
						db = openOrCreateDatabase(DATABASE_NAME,
								SQLiteDatabase.OPEN_READWRITE, null);
						db.execSQL(queryBuf.toString());
					}
				} catch (Exception e) {
					Log.e(tag, e.toString());
				} finally {
					if (db.isOpen())
						db.close();
				}
				if (pointIsRecorded) {
					Log.i("ID", String.valueOf(id));
					id++;
					if (showingDebugToast)
						Toast.makeText(
								getBaseContext(),
								"Location stored: \nLat: "
										+ sevenSigDigits.format(loc
												.getLatitude())
										+ " \nLon: "
										+ sevenSigDigits.format(loc
												.getLongitude())
										+ " \nAlt: "
										+ (loc.hasAltitude() ? loc
												.getAltitude() + "m" : "?")
										+ " \nAcc: "
										+ (loc.hasAccuracy() ? loc
												.getAccuracy() + "m" : "?"),
								Toast.LENGTH_SHORT).show();
				} else {
					if (showingDebugToast)
						Toast.makeText(
								getBaseContext(),
								"Location not accurate enough: \nLat: "
										+ sevenSigDigits.format(loc
												.getLatitude())
										+ " \nLon: "
										+ sevenSigDigits.format(loc
												.getLongitude())
										+ " \nAlt: "
										+ (loc.hasAltitude() ? loc
												.getAltitude() + "m" : "?")
										+ " \nAcc: "
										+ (loc.hasAccuracy() ? loc
												.getAccuracy() + "m" : "?"),
								Toast.LENGTH_SHORT).show();
				}
			}
		}

		public void onProviderDisabled(String provider) {
			if (showingDebugToast)
				Toast.makeText(getBaseContext(),
						"onProviderDisabled: " + provider, Toast.LENGTH_SHORT)
						.show();

		}

		public void onProviderEnabled(String provider) {
			if (showingDebugToast)
				Toast.makeText(getBaseContext(),
						"onProviderEnabled: " + provider, Toast.LENGTH_SHORT)
						.show();

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			String showStatus = null;
			if (status == LocationProvider.AVAILABLE)
				showStatus = "Available";
			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
				showStatus = "Temporarily Unavailable";
			if (status == LocationProvider.OUT_OF_SERVICE)
				showStatus = "Out of Service";
			if (status != lastStatus && showingDebugToast) {
				Toast.makeText(getBaseContext(), "new status: " + showStatus,
						Toast.LENGTH_SHORT).show();
			}
			lastStatus = status;
		}

	}

	// Below is the service framework methods

	private NotificationManager mNM;

	@Override
	public void onCreate() {
		super.onCreate();
		GPSpoints = new ArrayList<GPSPoint>();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		id = 0;
		startTrackerService();

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
		Log.i("onCreate", "showNotification");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		shutdownTrackerService();

		// Cancel the persistent notification.
		mNM.cancel(R.string.GPS_service_started);

		// Tell the user we stopped.
		Toast.makeText(this, R.string.GPS_service_stopped, Toast.LENGTH_SHORT)
				.show();
	}
	
	public ArrayList<GPSPoint> getPoints(){
		return GPSpoints;
	}
	public double getSize(){
		return id;
	}
	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		Log.i("showNotification?", "");
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.GPS_service_started);

		// Set the icon, scrolling text and timestamp
		
		
		// The PendingIntent to launch our activity if the user selects this
		// notification
		int requestID = (int) System.currentTimeMillis();
		PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,
				new Intent(this, GPSActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification.Builder(this)
		.setSmallIcon(R.drawable.gpslogger16)
		.setContentTitle(text)
		.setContentIntent(contentIntent)
		.build();
		// Set the info for the views that show in the notification panel.
		/*notification.setLatestEventInfo(this, getText(R.string.GPS_name), text,
				contentIntent);*/

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.GPS_service_started, notification);
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public static void setMinTimeMillis(long _minTimeMillis) {
		minTimeMillis = _minTimeMillis;
	}

	public static long getMinTimeMillis() {
		return minTimeMillis;
	}

	public static void setMinDistanceMeters(long _minDistanceMeters) {
		minDistanceMeters = _minDistanceMeters;
	}

	public static long getMinDistanceMeters() {
		return minDistanceMeters;
	}

	public static float getMinAccuracyMeters() {
		return minAccuracyMeters;
	}

	public static void setMinAccuracyMeters(float minAccuracyMeters) {
		GPSTrackerService.minAccuracyMeters = minAccuracyMeters;
	}

	public static void setShowingDebugToast(boolean showingDebugToast) {
		GPSTrackerService.showingDebugToast = showingDebugToast;
	}

	public static boolean isShowingDebugToast() {
		return showingDebugToast;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		GPSTrackerService getService() {
			return GPSTrackerService.this;
		}
	}

}
