package com.example.behaviortracker;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class GPSInferAdapter extends ArrayAdapter<InferPoint>{
	private ArrayList<InferPoint> points;
	private Context context;
	public GPSInferAdapter(Context context, ArrayList<InferPoint> points) {
		super(context, R.layout.inference_item, points);
		this.points = points;
		this.context = context;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.inference_item, parent, false);
		}
		Log.i("Type", points.get(position).type);
		Log.i("position", Integer.toString(position));
		InferPoint i = points.get(position);
		TextView type = (TextView) convertView.findViewById(R.id.type);
		//TextView time = (TextView) convertView.findViewById(R.id.time);
		TextView latitude = (TextView) convertView.findViewById(R.id.latitude);
		TextView longitude = (TextView) convertView.findViewById(R.id.longitude);
//		date.setText("d");
//		latitude.setText("lat");
//		longitude.setText("long");
		type.setText(i.type);
		//time.setText(i.time);
		latitude.setText(Double.toString(i.latitude));
		longitude.setText(Double.toString(i.longitude));
		return convertView;
	}
	public void clearAdapter(){
		points.clear();
		notifyDataSetChanged();
	}	
}
