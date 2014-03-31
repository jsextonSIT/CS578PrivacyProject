package com.example.behaviortracker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	private static final int MENU_TRACKING = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		boolean result = super.onCreateOptionsMenu(menu);
		

		menu.add(ContextMenu.NONE, MENU_TRACKING, ContextMenu.NONE, R.string.menu_MAIN_tracking).setAlphabeticShortcut('M');
		
		return result;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_TRACKING:
				//startGPSActivity();
				Intent intent = new Intent(this, GPSActivity.class);

				startActivity(intent);
				break;
			default:
				//showAlert(R.string.menu_message_unsupported);
				break;
		}
		return true;
	}
	private void startGPSActivity()
	{
		try
		{
			startActivityForClass("com.example.behaviortracker", "com.example.behaviortracker.GPSActivity");
		}
		catch (ActivityNotFoundException e)
		{
			//Log.i(TAG, "Cannot find activity for open-gpstracker");
			// No compatible file manager was found: install openintents filemanager.
			//showDialog(DIALOG_INSTALL_OPENGPSTRACKER);
		}

	}
	private void startActivityForClass(String aPackageName, String aClassName) throws ActivityNotFoundException
	{
		Intent intent = new Intent();
		intent.setClassName(aPackageName, aClassName);

		startActivity(intent);
	}
}
