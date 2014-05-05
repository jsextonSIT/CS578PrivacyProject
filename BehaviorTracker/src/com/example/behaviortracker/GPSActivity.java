package com.example.behaviortracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.maps.MapController;
//import com.google.android.gms.common.GooglePlayServicesClient;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.location.LocationClient;
//import com.google.android.gms.maps.SupportMapFragment;

public class GPSActivity extends ListActivity /*
											 * implements
											 * GooglePlayServicesClient
											 * .ConnectionCallbacks,
											 * GooglePlayServicesClient
											 * .OnConnectionFailedListener
											 */{
	private String currentTripName = "";
	private int tripNum = 0;
	private final DecimalFormat sevenSigDigits = new DecimalFormat("0.#######");
	private static final String tag = "GPSTrackerActivity";

	private ArrayList<GPSPoint> GPSpoints = null;
	private int arraysize;
	private Bundle instanceState;
	private boolean serviceOn = false;
	private GPSTrackerService GPSservice = null;
	private GPSListAdapter adapter = null;
	private GPSInferAdapter iadapter = null;
	private int fragid;
	private String[] types = { "Home", "Work", "Holidays" };
	MenuItem toggle, list;
	private static final int MENU_TOGGLE = 1;
	private static final int MENU_LIST = 2;
	private static final int MENU_CLEAR = 3;
	private static final int MENU_NIGHT = 4;
	private static final int MENU_INFER = 4;
	private static final int MENU_TEST = 5;
	private static final int MENU_EXPORT = 2;
	private double tolerance = .0001;
	Location location;
	int altitudeCorrectionMeters = 20;
	LinearLayout inferLayout, rawLayout, headerLayout;
	double latitude, longitude;
	// private LocationClient mLocationClient;
	// private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	ConnectionResult mConnectionResult;
	MapController mapController;
	GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// check if service is still on
		instanceState = savedInstanceState;
		if (savedInstanceState == null) {
			serviceOn = false;
		} else {
			serviceOn = savedInstanceState.getBoolean("state");
		}
		setContentView(R.layout.fragment_list);

		fragid = 0;
		// inferLayout = (LinearLayout) findViewById(R.id.inference_layout);
		headerLayout = (LinearLayout) findViewById(R.id.inference_header);
		rawLayout = (LinearLayout) findViewById(R.id.raw_gps);
		// map stuff
		// setContentView(R.layout.fragment_map);
		// get map layout
		// map = ((SupportMapFragment)
		// getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		// mapController = map.getController();
		// mapController.setZoom(16);
		// mLocationClient = new LocationClient(this, this, this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		boolean result = super.onCreateOptionsMenu(menu);
		// add start tracking button to menu
		if (serviceOn) {
			toggle = menu.add(ContextMenu.NONE, MENU_TOGGLE, ContextMenu.NONE,
					R.string.menu_GPS_stop).setAlphabeticShortcut('S');
		} else {
			toggle = menu.add(ContextMenu.NONE, MENU_TOGGLE, ContextMenu.NONE,
					R.string.menu_GPS_start).setAlphabeticShortcut('S');
		}
		// menu.add(ContextMenu.NONE, MENU_EXPORT, ContextMenu.NONE,
		// R.string.menu_GPS_export).setAlphabeticShortcut('E');
		list = menu.add(ContextMenu.NONE, MENU_LIST, ContextMenu.NONE,
				R.string.menu_GPS_list).setAlphabeticShortcut('L');
		menu.add(ContextMenu.NONE, MENU_CLEAR, ContextMenu.NONE,
				R.string.menu_GPS_clear).setAlphabeticShortcut('C');
		menu.add(ContextMenu.NONE, MENU_INFER, ContextMenu.NONE,
				R.string.menu_GPS_infer_list).setAlphabeticShortcut('I');
		menu.add(ContextMenu.NONE, MENU_TEST, ContextMenu.NONE,
				R.string.menu_GPS_stock).setAlphabeticShortcut('T');
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ((item.getItemId() != 4) && (fragid == 1)) {
			/* not infer and on infer fragment */
			rawLayout.setVisibility(View.VISIBLE);
			// inferLayout.setVisibility(View.GONE);
			headerLayout.setVisibility(View.GONE);
			fragid = 0;
		}
		switch (item.getItemId()) {

		case MENU_TOGGLE:
			// start/stop tracking service
			toggleService();
			break;
		case MENU_LIST:
			showList(0);
			break;
		case MENU_CLEAR:
			clearTable();
			break;
		case MENU_INFER:
			if (fragid != 1) {
				/* change fragment */
				rawLayout.setVisibility(View.GONE);
				// inferLayout.setVisibility(View.VISIBLE);
				headerLayout.setVisibility(View.VISIBLE);
				fragid = 1;
			}
			showInferences();
			break;
		case MENU_TEST:
			addStockPoints();
			break;
		/*
		 * case MENU_NIGHT: showList(1); break;
		 */
		/*
		 * case MENU_EXPORT: doExport(); break;
		 */
		default:
			// showAlert(R.string.menu_message_unsupported);
			break;
		}
		return true;
	}

	private void showInferences() {
		GPSPoint home, work, holiday;
		InferPoint hom, wor, holi;
		ArrayList<InferPoint> infs = new ArrayList(3);
		/* first make home query */
		home = getHome();
		if (home != null) {
			hom = new InferPoint("Home", home.longitude, home.latitude);

		} else {
			hom = new InferPoint("No Home", 0.0, 0.0);
		}
		infs.add(hom);

		work = getWork();
		
		Log.i("returned", "Work");
		if (work != null/* && !sameLocation(work, home)*/) {
			work.logPoint();
			wor = new InferPoint("Work", work.longitude, work.latitude);
		} else {
			wor = new InferPoint("No Work", 0.0, 0.0);
		}
		infs.add(wor);
		holiday = getHoliday();
		if (holiday != null) {
			if (sameLocation(holiday, home)) {
				holi = new InferPoint("Easter: home", home.longitude,
						home.latitude);
			} else if (sameLocation(holiday, work)) {
				holi = new InferPoint("Easter: work", work.longitude,
						work.latitude);
			} else {
				holi = new InferPoint("Easter", holiday.longitude,
						holiday.latitude);
			}

		} else {
			holi = new InferPoint("No Easter Data", 0.0, 0.0);
		}
		infs.add(holi);

		iadapter = new GPSInferAdapter(this, infs);
		setListAdapter(iadapter);
	}

	private void clearTable() {
		SQLiteDatabase db = openOrCreateDatabase(
				GPSTrackerService.DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE,
				null);
		db.execSQL("DELETE FROM " + GPSTrackerService.POINTS_TABLE_NAME);
		if (db != null && db.isOpen())
			db.close();
		if (GPSpoints != null){
			arraysize = 0;
		GPSpoints.clear();
		}
		if (adapter != null)
			adapter.clearAdapter();
		if (iadapter != null)
			iadapter.clearAdapter();
	}

	/*
	 * private void doExport() { // export the db contents to a kml file
	 * SQLiteDatabase db = null; Cursor cursor = null; try { currentTripName =
	 * "Trip" + tripNum; tripNum++; //Log.i(tag,
	 * "altitude Correction updated to "+altitudeCorrectionMeters); db =
	 * openOrCreateDatabase(GPSTrackerService.DATABASE_NAME,
	 * SQLiteDatabase.OPEN_READWRITE, null); cursor = db.rawQuery("SELECT * " +
	 * " FROM " + GPSTrackerService.POINTS_TABLE_NAME + " ORDER BY ID ASC",
	 * null); int gmtDatestampColumnIndex =
	 * cursor.getColumnIndexOrThrow("GMTDATESTAMP"); int gmtTimestampColumnIndex
	 * = cursor.getColumnIndexOrThrow("GMTTIMESTAMP"); int latitudeColumnIndex =
	 * cursor.getColumnIndexOrThrow("LATITUDE"); int longitudeColumnIndex =
	 * cursor.getColumnIndexOrThrow("LONGITUDE"); int altitudeColumnIndex =
	 * cursor.getColumnIndexOrThrow("ALTITUDE"); int accuracyColumnIndex =
	 * cursor.getColumnIndexOrThrow("ACCURACY"); if (cursor.moveToFirst()) {
	 * StringBuffer fileBuf = new StringBuffer(); String beginTimestamp = null;
	 * String endTimestamp = null; String gmtTimestamp = null;
	 * initFileBuf(fileBuf, initValuesMap()); do { gmtTimestamp =
	 * cursor.getString(gmtTimestampColumnIndex); if (beginTimestamp == null) {
	 * beginTimestamp = gmtTimestamp; } double latitude =
	 * cursor.getDouble(latitudeColumnIndex); double longitude =
	 * cursor.getDouble(longitudeColumnIndex); double altitude =
	 * cursor.getDouble(altitudeColumnIndex) + altitudeCorrectionMeters; double
	 * accuracy = cursor.getDouble(accuracyColumnIndex);
	 * fileBuf.append(sevenSigDigits
	 * .format(longitude)+","+sevenSigDigits.format(
	 * latitude)+","+altitude+"\n"); } while (cursor.moveToNext()); endTimestamp
	 * = gmtTimestamp; closeFileBuf(fileBuf, beginTimestamp, endTimestamp);
	 * String fileContents = fileBuf.toString(); Log.d(tag, fileContents); File
	 * sdDir = new File("/sdcard/GPSTracker"); sdDir.mkdirs(); File file = new
	 * File("/sdcard/GPSTracker/"+currentTripName+".kml"); FileWriter sdWriter =
	 * new FileWriter(file, false); sdWriter.write(fileContents);
	 * sdWriter.close(); Toast.makeText(getBaseContext(), "Export completed!",
	 * Toast.LENGTH_LONG).show(); } else { Toast.makeText(getBaseContext(),
	 * "I didn't find any location points in the database, so no KML file was exported."
	 * , Toast.LENGTH_LONG).show(); } } catch (FileNotFoundException fnfe) {
	 * Toast.makeText(getBaseContext(),
	 * "Error trying access the SD card.  Make sure your handset is not connected to a computer and the SD card is properly installed"
	 * , Toast.LENGTH_LONG).show(); } catch (Exception e) {
	 * Toast.makeText(getBaseContext(), "Error trying to export: " +
	 * e.getMessage(), Toast.LENGTH_LONG).show(); } finally { if (cursor != null
	 * && !cursor.isClosed()) { cursor.close(); } if (db != null && db.isOpen())
	 * { db.close(); } } }
	 */
	private void addStockPoints() {
		double init;
		clearTable();
		if (GPSservice != null) {
			init = GPSservice.getSize();
			SQLiteDatabase db = openOrCreateDatabase(
					GPSTrackerService.DATABASE_NAME,
					SQLiteDatabase.OPEN_READWRITE, null);
			/* add home points */
			for (int i = 0; i < 240; i += 10) {
				StringBuffer queryBuf = makePoint(init, "2014-04-15 " + i / 60
						+ ":" + (((i % 60) == 0) ? "00" : i % 60 + ":00"),
						"40.7459", "-74.0272");
				db.execSQL(queryBuf.toString());
				init++;
				queryBuf = makePoint(init, "2014-04-16 " + i / 60 + ":"
						+ (((i % 60) == 0) ? "00" : i % 60 + ":00"), "40.7459",
						"-74.0272");
				db.execSQL(queryBuf.toString());
				init++;
			}
			for (int i = 1320; i < 1440; i += 10) {
				StringBuffer queryBuf = makePoint(init, "2014-04-15 " + i / 60
						+ ":" + (((i % 60) == 0) ? "00" : i % 60 + ":00"),
						"40.7459", "-74.0272");
				init++;
				db.execSQL(queryBuf.toString());
				queryBuf = makePoint(init, "2014-04-16 " + i / 60 + ":"
						+ (((i % 60) == 0) ? "00" : i % 60 + ":00"), "40.7459",
						"-74.0272");
				db.execSQL(queryBuf.toString());
				init++;
			}
			/* add work points */
			for (int i = 600; i < 960; i += 10) {
				StringBuffer queryBuf = makePoint(init, "2014-04-15 " + i / 60
						+ ":" + (((i % 60) == 0) ? "00" : i % 60 + ":00"),
						"40.7450", "-74.0242");
				db.execSQL(queryBuf.toString());
				init++;
				queryBuf = makePoint(init, "2014-04-16 " + i / 60 + ":"
						+ (((i % 60) == 0) ? "00" : i % 60 + ":00"), "40.7450",
						"-74.0242");
				db.execSQL(queryBuf.toString());
				init++;
			}
			/* add easter points */
			for (int i = 600; i < 960; i += 10) {
				StringBuffer queryBuf = makePoint(init, "2014-04-20 " + i / 60
						+ ":" + (((i % 60) == 0) ? "00" : i % 60 + ":00"),
						"40.7458", "-74.0273");
				db.execSQL(queryBuf.toString());
				init++;
			}
			if (GPSservice != null) {
				GPSservice.addIds(init);
			}
			if (db.isOpen())
				db.close();
		}
	}

	StringBuffer makePoint(double i, String dat, String lat, String lon) {
		StringBuffer queryBuf = new StringBuffer();
		queryBuf.append("INSERT INTO "
				+ "LOCATION_POINTS"
				+ " (ID, GMTDATESTAMP, LATITUDE, LONGITUDE, ALTITUDE, ACCURACY, SPEED, BEARING) VALUES ("
				+ Double.toString(i) + ", " + "'"
				+ dat
				+ "', "
				// + "'"
				// + timestampFormat.format(greg.getTime())
				// + "', "
				+ lat + ", " + lon + ", " + "NULL" + ", " + "NULL" + ", "
				+ "NULL" + ", " + "NULL" + ");");
		return queryBuf;
	}

	private void getList() {
		arraysize = 0;
		GPSpoints = new ArrayList<GPSPoint>();
		SQLiteDatabase db = openOrCreateDatabase(
				GPSTrackerService.DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE,
				null);
		Cursor cursor = db.rawQuery("SELECT * " + " FROM "
				+ GPSTrackerService.POINTS_TABLE_NAME + " ORDER BY ID ASC",
				null);
		// int gmtTimestampColumnIndex =
		// cursor.getColumnIndexOrThrow("GMTTIMESTAMP");
		int gmtDatestampColumnIndex = cursor
				.getColumnIndexOrThrow("GMTDATESTAMP");
		int latitudeColumnIndex = cursor.getColumnIndexOrThrow("LATITUDE");
		int longitudeColumnIndex = cursor.getColumnIndexOrThrow("LONGITUDE");
		// int altitudeColumnIndex = cursor.getColumnIndexOrThrow("ALTITUDE");
		// int accuracyColumnIndex = cursor.getColumnIndexOrThrow("ACCURACY");
		if (cursor.moveToFirst()) {
			do {
				String date = cursor.getString(gmtDatestampColumnIndex);
				// String time = cursor.getString(gmtTimestampColumnIndex);
				Double latitude = Double.parseDouble(cursor
						.getString(latitudeColumnIndex));
				Double longitude = Double.parseDouble(cursor
						.getString(longitudeColumnIndex));
				GPSPoint point = new GPSPoint(latitude, longitude, date);
				// Adding contact to list
				GPSpoints.add(point);
				arraysize++;
				point.logPoint();
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	private GPSPoint getFrequentPoint() {
		GPSPoint ret = null, temp;
		int highnum = 0, num = 0;
		if (arraysize > 0) {
			//Log.i("getFrequentPoint", "" + arraysize);
			for (int i = 0; i < arraysize; i++) {
				temp = GPSpoints.get(i);
				//Log.i("FrequentPoint", " ");
				temp.logPoint();
				num = 0;
				for (int y = 0; y < arraysize; y++) {
					if (y != i) {
						if (sameLocation(temp, GPSpoints.get(y))) {
							num++;
						}
					}
				}
				if (num > highnum) {
					Log.i("Highpoint", ""+highnum);
					ret = temp;
					highnum = num;
				}
			}
		}
		return ret;
	}

	private GPSPoint getHome() {
		GPSPoint home;
		getTimePoints(22, 5);
		home = getFrequentPoint();
		return home;
	}

	private GPSPoint getWork() {
		GPSPoint work;
		getTimePoints(10, 16);
		Log.i("getFrequentPoint", "getWork");
		work = getFrequentPoint();
		return work;
	}

	private GPSPoint getHoliday() {
		GPSPoint holiday;
		Log.i("getDatePoints", "getHoliday");
		getDatePoints("2014-04-20");
		Log.i("getFrequentPoint", "getHoliday");
		holiday = getFrequentPoint();
		return holiday;
	}

	boolean sameLocation(GPSPoint x, GPSPoint y) {
		double xlat, ylat, xlong, ylong;
		if ((x == null) || (y == null)) {
			return false;
		}
		//Log.i("sameLocation", "comparison ");
		xlat = x.latitude;
		ylat = y.latitude;
		xlong = x.longitude;
		ylong = y.longitude;

		if ((ylat > xlat + tolerance) || (ylat < xlat - tolerance)) {
			/* check latitude */
			return false;
		}
		if ((ylong > xlong + tolerance) || (ylong < xlong - tolerance)) {
			/* check longitude */
			return false;
		}
		return true;
	}

	private void getTimePoints(int stime, int etime) {
		/*
		 * returns all points that have times greater than or equal stime and
		 * less than or equal etime
		 */
		arraysize = 0;
		GPSpoints = new ArrayList<GPSPoint>();
		SQLiteDatabase db = openOrCreateDatabase(
				GPSTrackerService.DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE,
				null);
		Cursor cursor = db.rawQuery("SELECT * " + " FROM "
				+ GPSTrackerService.POINTS_TABLE_NAME +
				/*
				 * " WHERE (STRFTIME('%M', GMTDATESTAMP) BETWEEN '" + stime +
				 * "' AND '" + etime + "')",
				 */
				" WHERE '" + stime + "' <= strftime('%H', GMTDATESTAMP)"
				+ " AND ID IN " + "(SELECT ID FROM "
				+ GPSTrackerService.POINTS_TABLE_NAME + " WHERE '" + etime
				+ "' >= strftime('%H', GMTDATESTAMP))", null);
		int gmtDatestampColumnIndex = cursor
				.getColumnIndexOrThrow("GMTDATESTAMP");
		int latitudeColumnIndex = cursor.getColumnIndexOrThrow("LATITUDE");
		int longitudeColumnIndex = cursor.getColumnIndexOrThrow("LONGITUDE");
		// int altitudeColumnIndex = cursor.getColumnIndexOrThrow("ALTITUDE");
		// int accuracyColumnIndex = cursor.getColumnIndexOrThrow("ACCURACY");

		if (cursor.moveToFirst()) {
			do {
				String date = cursor.getString(gmtDatestampColumnIndex);
				// String time = cursor.getString(gmtTimestampColumnIndex);
				Double latitude = Double.parseDouble(cursor
						.getString(latitudeColumnIndex));
				Double longitude = Double.parseDouble(cursor
						.getString(longitudeColumnIndex));
				GPSPoint point = new GPSPoint(latitude, longitude, date);
				// Adding contact to list
				GPSpoints.add(point);
				arraysize++;
				point.logPoint();
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	private void getDatePoints(String day) {
		/*
		 * returns all points that have times greater than or equal stime and
		 * less than or equal etime
		 */
		arraysize = 0;
		GPSpoints = new ArrayList<GPSPoint>();
		SQLiteDatabase db = openOrCreateDatabase(
				GPSTrackerService.DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE,
				null);
		Cursor cursor = db.rawQuery("SELECT * " + " FROM "
				+ GPSTrackerService.POINTS_TABLE_NAME +
				/*
				 * " WHERE (STRFTIME('%M', GMTDATESTAMP) BETWEEN '" + stime +
				 * "' AND '" + etime + "')",
				 */
				" WHERE '" + day + "' = strftime(\"%Y-%m-%d\",  GMTDATESTAMP)",
				null);
		
		int gmtDatestampColumnIndex = cursor.getColumnIndexOrThrow("GMTDATESTAMP");
		int latitudeColumnIndex = cursor.getColumnIndexOrThrow("LATITUDE");
		int longitudeColumnIndex = cursor.getColumnIndexOrThrow("LONGITUDE");
		// int altitudeColumnIndex = cursor.getColumnIndexOrThrow("ALTITUDE");
		// int accuracyColumnIndex = cursor.getColumnIndexOrThrow("ACCURACY");

		if (cursor.moveToFirst()) {
			do {
				String date = cursor.getString(gmtDatestampColumnIndex);
				// String time = cursor.getString(gmtTimestampColumnIndex);
				Double latitude = Double.parseDouble(cursor
						.getString(latitudeColumnIndex));
				Double longitude = Double.parseDouble(cursor
						.getString(longitudeColumnIndex));
				GPSPoint point = new GPSPoint(latitude, longitude, date);
				// Adding contact to list
				GPSpoints.add(point);
				arraysize++;
				point.logPoint();
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	private void showList(int i) {
		/*
		 * if (GPSservice != null && serviceOn){ GPSpoints =
		 * GPSservice.getPoints(); GPSListAdapter adapter = new
		 * GPSListAdapter(this, GPSpoints); setListAdapter(adapter); }
		 */
		switch (i) {
		case 0:
			getList();
			break;
		case 1:
			getTimePoints(18, 4);
			break;
		default:
			getList();
		}
		adapter = new GPSListAdapter(this, GPSpoints);
		setListAdapter(adapter);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			GPSTrackerService.LocalBinder b = (GPSTrackerService.LocalBinder) binder;
			GPSservice = b.getService();
			Toast.makeText(GPSActivity.this, "Connected", Toast.LENGTH_SHORT)
					.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			GPSservice = null;
		}
	};

	private void toggleService() {
		if (serviceOn) {
			// stop the service
			Log.i(tag, "Turning off service");
			unbindService(mConnection);
			stopService(new Intent(GPSActivity.this, GPSTrackerService.class));
			// change menubutton text
			toggle.setTitle(R.string.menu_GPS_start);
			serviceOn = false;
			Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
		} else {
			// stop the service
			Log.i(tag, "Turning on service");
			Intent intent = new Intent(GPSActivity.this,
					GPSTrackerService.class);
			startService(intent);
			bindService(intent, mConnection, GPSActivity.this.BIND_AUTO_CREATE);
			// change menubutton text
			toggle.setTitle(R.string.menu_GPS_stop);
			serviceOn = true;
			Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();

		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// once we have the reference to the client, connect it
		/*
		 * mapif (mLocationClient != null) mLocationClient.connect();
		 */

	}

	@Override
	protected void onResume() {
		super.onResume();
		// onRestoreInstanceState(instanceState);
		if (serviceOn) {
			Intent intent = new Intent(GPSActivity.this,
					GPSTrackerService.class);
			bindService(intent, mConnection, GPSActivity.this.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// onSaveInstanceState(instanceState);
		if (serviceOn) {
			unbindService(mConnection);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		// outState.putParcelableArrayList("key", G);
		savedInstanceState.putBoolean("state", serviceOn);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		serviceOn = savedInstanceState.getBoolean("state");
	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		/* map mLocationClient.disconnect(); */
		super.onStop();
	}

	/*
	 * @Override public void onConnectionFailed(ConnectionResult result) {
	 * 
	 * Google Play services can resolve some errors it detects. If the error has
	 * a resolution, try sending an Intent to start a Google Play services
	 * activity that can resolve error.
	 * 
	 * mConnectionResult = result; if (mConnectionResult.hasResolution()) { try
	 * { // Start an Activity that tries to resolve the error
	 * mConnectionResult.startResolutionForResult(this,
	 * CONNECTION_FAILURE_RESOLUTION_REQUEST);
	 * 
	 * Thrown if Google Play services canceled the original PendingIntent
	 * 
	 * } catch (IntentSender.SendIntentException e) { // Log the error
	 * e.printStackTrace(); } } else {
	 * 
	 * If no resolution is available, display a dialog to the user with the
	 * error.
	 * 
	 * // mConnectionResult.showErrorDialog("No Resolution Available"); }
	 * 
	 * }
	 */

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	/*
	 * @Override public void onConnected(Bundle arg0) { // Display the
	 * connection status Toast.makeText(this, "Connected",
	 * Toast.LENGTH_SHORT).show();
	 * 
	 * }
	 */

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	/*
	 * @Override public void onDisconnected() { // Display the connection status
	 * Toast.makeText(this, "Disconnected. Please re-connect.",
	 * Toast.LENGTH_SHORT).show();
	 * 
	 * }
	 */

	/*
	 * Check if Google Play services are available
	 */

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	private HashMap initValuesMap() {
		HashMap valuesMap = new HashMap();

		valuesMap.put("FILENAME", currentTripName);

		// RadioButton airButton = (RadioButton)findViewById(R.id.RadioAir);
		/*
		 * if (false) { // use air settings valuesMap.put("EXTRUDE", "1");
		 * valuesMap.put("TESSELLATE", "0"); valuesMap.put("ALTITUDEMODE",
		 * "absolute"); } else {
		 */
		// use ground settings for the export
		valuesMap.put("EXTRUDE", "0");
		valuesMap.put("TESSELLATE", "1");
		valuesMap.put("ALTITUDEMODE", "clampToGround");
		// }

		return valuesMap;
	}

	private void initFileBuf(StringBuffer fileBuf, HashMap valuesMap) {
		fileBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fileBuf.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
		fileBuf.append("  <Document>\n");
		fileBuf.append("    <name>" + valuesMap.get("FILENAME") + "</name>\n");
		fileBuf.append("    <description>GPSLogger KML export</description>\n");
		fileBuf.append("    <Style id=\"yellowLineGreenPoly\">\n");
		fileBuf.append("      <LineStyle>\n");
		fileBuf.append("        <color>7f00ffff</color>\n");
		fileBuf.append("        <width>4</width>\n");
		fileBuf.append("      </LineStyle>\n");
		fileBuf.append("      <PolyStyle>\n");
		fileBuf.append("        <color>7f00ff00</color>\n");
		fileBuf.append("      </PolyStyle>\n");
		fileBuf.append("    </Style>\n");
		fileBuf.append("    <Placemark>\n");
		fileBuf.append("      <name>Absolute Extruded</name>\n");
		fileBuf.append("      <description>Transparent green wall with yellow points</description>\n");
		fileBuf.append("      <styleUrl>#yellowLineGreenPoly</styleUrl>\n");
		fileBuf.append("      <LineString>\n");
		fileBuf.append("        <extrude>" + valuesMap.get("EXTRUDE")
				+ "</extrude>\n");
		fileBuf.append("        <tessellate>" + valuesMap.get("TESSELLATE")
				+ "</tessellate>\n");
		fileBuf.append("        <altitudeMode>" + valuesMap.get("ALTITUDEMODE")
				+ "</altitudeMode>\n");
		fileBuf.append("        <coordinates>\n");
	}

	private void closeFileBuf(StringBuffer fileBuf, String beginTimestamp,
			String endTimestamp) {
		fileBuf.append("        </coordinates>\n");
		fileBuf.append("     </LineString>\n");
		fileBuf.append("	 <TimeSpan>\n");
		String formattedBeginTimestamp = zuluFormat(beginTimestamp);
		fileBuf.append("		<begin>" + formattedBeginTimestamp + "</begin>\n");
		String formattedEndTimestamp = zuluFormat(endTimestamp);
		fileBuf.append("		<end>" + formattedEndTimestamp + "</end>\n");
		fileBuf.append("	 </TimeSpan>\n");
		fileBuf.append("    </Placemark>\n");
		fileBuf.append("  </Document>\n");
		fileBuf.append("</kml>");
	}

	private String zuluFormat(String beginTimestamp) {
		// turn 20081215135500 into 2008-12-15T13:55:00Z
		StringBuffer buf = new StringBuffer(beginTimestamp);
		buf.insert(4, '-');
		buf.insert(7, '-');
		buf.insert(10, 'T');
		buf.insert(13, ':');
		buf.insert(16, ':');
		buf.append('Z');
		return buf.toString();
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { // Decide what to do based on the original
	 * request code switch (requestCode) {
	 * 
	 * case CONNECTION_FAILURE_RESOLUTION_REQUEST:
	 * 
	 * If the result code is Activity.RESULT_OK, try to connect again
	 * 
	 * switch (resultCode) { case Activity.RESULT_OK: /* Try the request again
	 * 
	 * 
	 * mLocationClient.connect(); break; } } }
	 * 
	 * private boolean servicesConnected() { // Check that Google Play services
	 * is available int resultCode = GooglePlayServicesUtil
	 * .isGooglePlayServicesAvailable(this); // If Google Play services is
	 * available if (ConnectionResult.SUCCESS == resultCode) { // In debug mode,
	 * log the status Log.d("Location Updates",
	 * "Google Play services is available."); // Continue return true; // Google
	 * Play services was not available for some reason } else { // Get the error
	 * code // int errorCode = connectionResult.getErrorCode(); // Get the error
	 * dialog from Google Play services Dialog errorDialog =
	 * GooglePlayServicesUtil.getErrorDialog( resultCode, this,
	 * CONNECTION_FAILURE_RESOLUTION_REQUEST);
	 * 
	 * // If Google Play services can provide an error dialog if (errorDialog !=
	 * null) { // Create a new DialogFragment for the error dialog
	 * ErrorDialogFragment errorFragment = new ErrorDialogFragment(); // Set the
	 * dialog in the DialogFragment errorFragment.setDialog(errorDialog); //
	 * Show the error dialog in the DialogFragment
	 * //errorFragment.show(getFragmentManager(), "Location Updates"); } return
	 * false; } }
	 */

}
