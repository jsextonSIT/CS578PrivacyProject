package com.example.behaviortracker;

import android.util.Log;

public class Utilities {
	public static final long MILLIS_PER_DAY = 86400000;
	public static long getNextMidnightInMillis(long time){
		long ret = 0;
		if(time > 0){
			long x = time % MILLIS_PER_DAY;
			ret = time - x;
			if(x > 0)
				ret += MILLIS_PER_DAY;
		}
		return ret;
	}
	
	public static long getPreviousMidnightInMillis(long time){
		long ret = 0;
		if(time > 0){
			long x = time % MILLIS_PER_DAY;
			ret = time - x;
		}
		return ret;
	}
	
	public static long getPreviousQuantumInMillis(long time, long quantum){
		long ret = 0;
		if(time > 0){
			long x = time % quantum;
			ret = time - quantum;
		}
		return ret;
	}
	
}
