package com.example.behaviortracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final int MENU_TRACKING = 1;
	private static final int MENU_ASSUMPTIONS = 2;
	private static final int MENU_RELATION = 3;
	private static final int MENU_INACTIVE = 4;
	private ArrayList<Integer> relationscores;
	private ArrayList<String> relationcontacts;
	private RelationshipAdapter radapter;
	private LinearLayout rheader = null, iheader = null, inact = null;
	private ScrollView assumptions;
	private ListView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rheader = (LinearLayout) findViewById(R.id.layout_relationships);
		iheader = (LinearLayout) findViewById(R.id.layout_inactive);
		inact = (LinearLayout) findViewById(R.id.layout_inactive2);
		assumptions = (ScrollView) findViewById(R.id.layout_assumptions);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		boolean result = super.onCreateOptionsMenu(menu);
		

		menu.add(ContextMenu.NONE, MENU_TRACKING, ContextMenu.NONE, R.string.menu_MAIN_tracking).setAlphabeticShortcut('M');
		menu.add(ContextMenu.NONE, MENU_ASSUMPTIONS, ContextMenu.NONE, R.string.menu_MAIN_assumptions).setAlphabeticShortcut('A');
		menu.add(ContextMenu.NONE, MENU_RELATION, ContextMenu.NONE, R.string.menu_MAIN_relationships).setAlphabeticShortcut('R');
		menu.add(ContextMenu.NONE, MENU_INACTIVE, ContextMenu.NONE, R.string.menu_MAIN_inactive).setAlphabeticShortcut('I');
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
			case MENU_ASSUMPTIONS:
				
				assumptions.setVisibility(View.VISIBLE);
				rheader.setVisibility(View.GONE);
				iheader.setVisibility(View.GONE);
				inact.setVisibility(View.GONE);
				if (radapter != null)
					radapter.clearAdapter();
				break;
			case MENU_RELATION:
				/* hide assumptions and show relationships*/
				assumptions.setVisibility(View.GONE);
				rheader.setVisibility(View.VISIBLE);
				iheader.setVisibility(View.GONE);
				inact.setVisibility(View.GONE);
				/* get relations */
				RelationshipStatistics x = new RelationshipStatistics(20, this);
				relationscores = x.scores;
				relationcontacts = x.contacts;
				radapter = new RelationshipAdapter(this, relationscores, relationcontacts);
				lv = (ListView)findViewById(R.id.main_list);
				lv.setAdapter(radapter);
				break;
			case MENU_INACTIVE:
				if (radapter != null)
					radapter.clearAdapter();
				assumptions.setVisibility(View.GONE);
				rheader.setVisibility(View.GONE);
				iheader.setVisibility(View.VISIBLE);
				inact.setVisibility(View.VISIBLE);
				PhoneAndMessageInactivityStatistics pm = new PhoneAndMessageInactivityStatistics(15000, this);
				PhoneAndMessageInactivityStatistics.DisplayTimeStrings strs = pm.getAverageSleepTime();
				TextView start = (TextView) findViewById(R.id.inact_start);
				TextView end = (TextView) findViewById(R.id.inact_end);
				start.setText(strs.start);
				end.setText(strs.end);
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
