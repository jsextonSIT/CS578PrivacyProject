package com.example.behaviortracker;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RelationshipAdapter extends ArrayAdapter<Integer>{
	private ArrayList<Integer> scores;
	private ArrayList<String> contacts;
	private Context context;
	public RelationshipAdapter(Context context, ArrayList<Integer> scores, ArrayList<String> contacts) {
		super(context, R.layout.relationship_item, scores);
		this.scores = scores;
		this.contacts = contacts;
		this.context = context;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.relationship_item, parent, false);
		}
		//Log.i("Date", points.get(position).date);
		//Log.i("position", Integer.toString(position));
		int i = scores.get(position);
		String c = contacts.get(position);
		//TextView date = (TextView) convertView.findViewById(R.id.date);
		//TextView time = (TextView) convertView.findViewById(R.id.time);
		TextView ctext = (TextView) convertView.findViewById(R.id.contacts);
		TextView scotext = (TextView) convertView.findViewById(R.id.score);
//		date.setText("d");
//		latitude.setText("lat");
//		longitude.setText("long");
		//date.setText(i.date);
		//time.setText(i.time);
		ctext.setText(c);
		scotext.setText(Integer.toString(i));
		return convertView;
	}
	public void clearAdapter(){
		scores.clear();
		contacts.clear();
		notifyDataSetChanged();
	}	
}
