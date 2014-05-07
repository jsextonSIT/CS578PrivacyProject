package com.example.behaviortracker;
	import java.util.ArrayList;

	import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

	public class AreaCodeAdapter extends ArrayAdapter<String>{
		private ArrayList<String> months;
		private ArrayList<String> states;
		private Context context;
		public AreaCodeAdapter(Context context, ArrayList<String> months, ArrayList<String> states) {
			super(context, R.layout.relationship_item, months);
			this.months = months;
			this.states = states;
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
			String i = months.get(position);
			String c = states.get(position);
			//TextView date = (TextView) convertView.findViewById(R.id.date);
			//TextView time = (TextView) convertView.findViewById(R.id.time);
			TextView ctext = (TextView) convertView.findViewById(R.id.contacts);
			TextView scotext = (TextView) convertView.findViewById(R.id.score);
			scotext.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2.5f));
//			date.setText("d");
//			latitude.setText("lat");
//			longitude.setText("long");
			//date.setText(i.date);
			//time.setText(i.time);
			ctext.setText(i);
			scotext.setText(c);
			return convertView;
		}
		public void clearAdapter(){
			months.clear();
			states.clear();
			notifyDataSetChanged();
		}	
	}
