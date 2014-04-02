package com.example.behaviortracker;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class GPSListAdapter extends ArrayAdapter<GPSPoint>{
	private ArrayList<GPSPoint> points;
	private Context context;
	public GPSListAdapter(Context context, ArrayList<GPSPoint> points) {
		super(context, R.layout.gps_item, points);
		this.points = points;
		this.context = context;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.gps_item, parent, false);
		}
		
		
		TextView date = (TextView) convertView.findViewById(R.id.date);
		TextView latitude = (TextView) convertView.findViewById(R.id.latitude);
		TextView longitude = (TextView) convertView.findViewById(R.id.longitude);
		date.setText(points.get(position).date);
		latitude.setText(Double.toString(points.get(position).latitude));
		longitude.setText(Double.toString(points.get(position).longitude));
		return convertView;
	}
}
