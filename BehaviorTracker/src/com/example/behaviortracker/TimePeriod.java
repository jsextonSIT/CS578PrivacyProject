package com.example.behaviortracker;

import android.util.Log;

public class TimePeriod{
	public long start;
	public long end;
	public TimePeriod(long s, long e){
			start = s;
			end = e;
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