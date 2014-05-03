package com.example.behaviortracker;

import android.util.Log;

public class TimePeriod{
	public long start;
	public long end;
	public TimePeriod(long s, long e){
		if(e >= s){
			start = s;
			end = e;
		}
		else{
			Log.i("TIME_PERIOD", "End is before Start.");
		}
	}
	public long getLength(){
		long l;
		if(start <= end){
			l = end - start;
		}
		else{
			l = Utilities.MILLIS_PER_DAY - (start - end);
		}
		return l;
	}
}