package com.example.behaviortracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.util.Log;

public class PhoneAndMessageInactivityStatistics {
	Hashtable<Long, ActiveTime> activeTable;
	ArrayList<TimePeriod> inactivities;
	ArrayList<TimePeriod> largestInactivities;
	//Each entry is the start of a quantum of time - for entry 
	long start;
	long end;
	long quantum;
	long cap;
	Resources res;
	TimePeriod commonSubset;
	
	Context context;
	String CONTENT_URL = "content://sms/";
	
	String SHARED_PREFS_NAME;
	SharedPreferences sp;
	SharedPreferences.Editor spe;
	
	ContentResolver cr;
	Uri uri;
	
	public PhoneAndMessageInactivityStatistics(long q, Context c, long numdays){
		start = 9223372036854775807L;
		end = 0;
		cap = numdays;
		quantum = q;
		activeTable = new Hashtable<Long, ActiveTime>();
		long i;
		
		inactivities = new ArrayList<TimePeriod>();
		largestInactivities = new ArrayList<TimePeriod>();
		context = c;
		
		res = context.getResources();
		SHARED_PREFS_NAME = res.getString(R.string.SHARED_PREFS_FILENAME);
		sp = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		spe = sp.edit();
		
		cr = context.getContentResolver();
		uri = Uri.parse(CONTENT_URL);
		getStartTimeP();
		if(numdays > 0){
			end = start + numdays * Utilities.MILLIS_PER_DAY;
		}
		//Log.i("start", start + "");
		//Log.i("end", end + "");
		for(i = start; i < end; i += q){
			activeTable.put(i, new ActiveTime());
		}
		getAllMessages();
		getAllPhoneCalls();
		
	}
	
	public void getAllMessages(){
		long mt;
		String protocol;
		String[] columns = {"date", "protocol"};
		Cursor curs = cr.query(uri, columns, null, null, "date desc");
		if (curs.moveToFirst()){
			do{
				mt = Long.parseLong(curs.getString(curs.getColumnIndex(columns[0])));
				protocol = curs.getString(curs.getColumnIndex(columns[1]));
				if(protocol == null){
					//null if it is a sent message
					addMessage(mt);
				}
			}while(curs.moveToNext());
		}
	}

	public void getAllPhoneCalls(){
		long dur;
		long ct;
		int calltype;
		String[] columns = {CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
		String order = CallLog.Calls.DATE + " DESC";
		Cursor curs = cr.query(CallLog.Calls.CONTENT_URI, columns, null, null, order);
		if(curs.moveToFirst()){
			do{
				ct = curs.getLong(curs.getColumnIndex(columns[0]));
				dur = curs.getLong(curs.getColumnIndex(columns[1]));
				calltype = curs.getInt(curs.getColumnIndex(columns[2]));
				
				if(calltype == CallLog.Calls.OUTGOING_TYPE){
					addPhoneCall(ct, dur);
				}
			}while(curs.moveToNext());
		}
	}
	public void getStartTimeC(){
		long ct;
		long last;
		String[] columns = {"date", "protocol"};
		Cursor curs = cr.query(uri, columns, null, null, "date desc");
		if(curs.moveToFirst()){
			if(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))) > end)
				end = Utilities.getPreviousMidnightInMillis(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))));
			do{
				ct = curs.getLong(curs.getColumnIndex(columns[0]));
				last = ct;
			}while(curs.moveToNext());

			if(last < start){
				start = Utilities.getNextMidnightInMillis(last);
			}
		}
	}
	public void getStartTimeP(){
		long ct;
		long last;
		getStartTimeC();
		String[] columns = {CallLog.Calls.DATE};
		String order = CallLog.Calls.DATE + " DESC";
		Cursor curs = cr.query(CallLog.Calls.CONTENT_URI, columns, null, null, order);
		if(curs.moveToFirst()){
			if(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))) > end)
				end = Utilities.getPreviousMidnightInMillis(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))));
			do{
				ct = curs.getLong(curs.getColumnIndex(columns[0]));
				last = ct;
			}while(curs.moveToNext());

			if(last < start){
				start = Utilities.getNextMidnightInMillis(last);
			}
		}
	}
	public void addMessage(long messageTime){
		long index = Utilities.getPreviousQuantumInMillis(messageTime, quantum);
		////Log.i("messagetime",  messageTime + "");
		////Log.i("index", index + "");
		if(index >= start && index < end){
			ActiveTime at = activeTable.get(index);
			////Log.i("index", index + "");
			at.active = true;
		}
	}
	
	public void addPhoneCall(long callStart, long callDuration){
		long index = Utilities.getPreviousQuantumInMillis(callStart, quantum);
		long i;
		long stop;
		ActiveTime at;
		if(callDuration % quantum == 0){
			stop = callDuration;
		}
		else{
			stop = callDuration + (quantum - (callDuration % quantum));
		}
		if(index >= start && index < end){
			at = activeTable.get(index);
			at.active = true;
		}
		for(i = 0; i < stop; i += quantum){
			if(i >= start && i < end){
				at = activeTable.get(i + callStart);
				at.active = true;
			}
		}
	}
	
	public void findInactiveTimes(){
		inactivities = new ArrayList<TimePeriod>();
		long i;
		int j;
		for(i = start; i < end; i += Utilities.MILLIS_PER_DAY){
			addInactiveTimesForOneDay(inactivities, i);
		}

	}
	
	public void findLargestInactiveTimes(){
		long maxInactivity = 0;
		long curr;
		long i;
		int j;
		TimePeriod tmp;
		TimePeriod maxPerDay = null;
		largestInactivities = new ArrayList<TimePeriod>();

		findInactiveTimes();
		for(i = start; i < end; i += Utilities.MILLIS_PER_DAY){
			for(j = 0; j < inactivities.size(); j++){
				tmp = inactivities.get(j);

				curr = tmp.getLength();
				if(tmp.start >= i && tmp.start <= i + Utilities.MILLIS_PER_DAY && tmp.end >= i && tmp.end <= i + Utilities.MILLIS_PER_DAY && maxInactivity <= curr){
					maxInactivity = curr;
					maxPerDay = tmp;
					
				}
			}
			if(maxPerDay == null){
				////Log.i("Help", "HELP");
			}
			else{
				largestInactivities.add(maxPerDay);
				//Log.i("maxIn", "" + maxInactivity);
				maxInactivity = 0;
				maxPerDay = null;
			}
		}
		DisplayTimeStrings x;
		for(j = 0; j < largestInactivities.size(); j++){
			x = new DisplayTimeStrings(largestInactivities.get(j).start, largestInactivities.get(j).end);
			
			//Log.i("hhh", x.start + " - " + x.end);
		}
		
	}
	
	public void addInactiveTimesForOneDay(ArrayList<TimePeriod> lst, long s){
		long i, e;
		int j;
		e = 0;
		boolean zerostart = false;
		boolean zeroend = false;
		TimePeriod tmp;

		////Log.i("EDDDDD", "ED");
		for(i = s; i < s + Utilities.MILLIS_PER_DAY; i += quantum){
			if(!activeTable.get(i).active){
				for(e = i; e < s + Utilities.MILLIS_PER_DAY; e+= quantum){
					if(activeTable.get(e).active || (e + quantum  == s + Utilities.MILLIS_PER_DAY)){
						if(i == s){
							zerostart = true;
						}
						if(e == s + Utilities.MILLIS_PER_DAY - quantum){
							zeroend = true;
						}
						lst.add(new TimePeriod(i, e));
						i = e;
						////Log.i("ADDED", "heyyy");
						break;
						
					}
				}

				////Log.i("EDD4DDD", "ED");
			}
		}
		
		//Handle cyclic possibility
		if(zerostart && zeroend){
			////Log.i("HERE", "HEREE");
			e = -1;
			for(j = 0; j < lst.size(); j++){
				tmp = lst.get(j);
				////Log.i("tmp start", "" + tmp.start);
				if(tmp.start == s){
					e = tmp.end;
					lst.remove(j);
					break;
				}
			}
			if(e > 0){
				for(j = 0; j < lst.size(); j++){
					tmp = lst.get(j);
					if(tmp.end == s + Utilities.MILLIS_PER_DAY - quantum){
						tmp.end = e;
						break;
					}
				}
			}
			
		}

		////Log.i("45", "ED");
	}
	
	//Note: If the subject's sleep patterns are completely random on a daily basis,
	//this algorithm cannot guarantee anything other than an average sleeping time.
	//This is revised from the idea of the "common subset", because if sleep durations remained the same, while
	//the start time changed, eventually there could be no common subset, even after removing outliers.
	//However, this algorithm can, to a point, deal with slight shifts in sleep time, by providing the average
	//of a dataset.
	//It should not significantly change the case where a user has consistent sleep periods.
	public TimePeriod makeAverageSleepTime(){
		int i;
		DisplayTimeStrings x;
		ArrayList<TimePeriod> reduced = applyThreshholds();
		ArrayList<Long> starts = new ArrayList<Long>();
		ArrayList<Long> ends = new ArrayList<Long>();
		long avgs = 0;
		long avge = 0;
		for(i = 0; i < reduced.size(); i++){
			starts.add(i, reduced.get(i).start);
			ends.add(i, reduced.get(i).end);
			x = new DisplayTimeStrings(starts.get(i), ends.get(i));
		}
		
		for(i = 0; i < reduced.size(); i++){
			avgs += starts.get(i);
			avge += ends.get(i);
		}
		
		avgs =(long) Math.floor(avgs/reduced.size());
		avge = (long) Math.floor(avge/reduced.size());

		avgs = starts.get(i/2);
		avge = ends.get(i/2);
		x = new DisplayTimeStrings(avgs, avge);
		return new TimePeriod(avgs, avge);
	}
	
	public ArrayList<TimePeriod> applyThreshholds(){
		
		TimePeriod tmp;
		ArrayList<TimePeriod> threshheld = new ArrayList<TimePeriod>();
		anonymizeDays();
		orderByDuration(threshheld);
		int numlengths = 0;
		/*
		ArrayList<Long> sizes = new ArrayList<Long>();
		for(int i = 0; i < threshheld.size(); i++){
			if(!sizes.contains(threshheld.get(i).getLength())){
				numlengths++;
				sizes.add(threshheld.get(i).getLength());
			}
		}
		double fourth = numlengths/4;
		int j;
		for(int i = 0; i < (int) Math.floor(fourth); i++){
			tmp = threshheld.get(0);
			for(j = 0; j < threshheld.size();j++){
				if(tmp.getLength() == threshheld.get(j).getLength()){
					threshheld.remove(j);
				}
			}
		}
		*/
		for(int i = 0; i < threshheld.size(); i++){
			if(threshheld.get(i).getLength() > 15 * 3600 * 1000){
				threshheld.remove(i);
				i--;
			}
			else if(threshheld.get(i).getLength() < 3 * 3600 * 1000){
				threshheld.remove(i);
				i--;
			}
		}
		DisplayTimeStrings x;
		int j;
		for(j = 0; j < threshheld.size(); j++){
			x = new DisplayTimeStrings(threshheld.get(j).start, threshheld.get(j).end);
			
			//Log.i("hhh", x.start + " - " + x.end);
			//Log.i("length", threshheld.get(j).getLength() + "");
		}
		return threshheld;
	}
	
	public void orderByDuration(ArrayList<TimePeriod> lst){
		int i,j;
		long s;
		for(i = 0; i < largestInactivities.size(); i++){
			s = largestInactivities.get(i).getLength();
			for(j = 0; j < lst.size() && lst.get(j).getLength() < s; j++){
			}

			////Log.i("starts", largestInactivities.get(i).start + "");
			////Log.i("ends", largestInactivities.get(i).end + "");
			lst.add(j, largestInactivities.get(i));
			////Log.i("lst.get", lst.get(j).start + "");
		}
	}
	
	public void anonymizeDays(){
		int i;
		TimePeriod tmp;
		for(i = 0; i < largestInactivities.size(); i++){
			////Log.i("i", i + "");
			////Log.i("size", largestInactivities.size() +"");
			
			tmp = largestInactivities.get(i);
			if(tmp == null){
				////Log.i("HELP", "help");
			}
			DisplayTimeStrings x;
			x = new DisplayTimeStrings(tmp.start, tmp.end);
			
			//Log.i("rrr", x.start + " - " + x.end);
	
			tmp.start %= Utilities.MILLIS_PER_DAY;
			tmp.end %= Utilities.MILLIS_PER_DAY;
			tmp.start += 3600 * 1000;
			tmp.end += 3600 * 1000;
		
		}
	}

	/************
	 * CALL THIS METHOD TO GET THE TWO STRINGS.
	 * @return
	 */
	public DisplayTimeStrings getAverageSleepTime(){
		DisplayTimeStrings ret;
		getAllMessages();
		getAllPhoneCalls();

		////Log.i("EDDDDD", "ED");
		findLargestInactiveTimes();
		////Log.i("EDDDDD", "ED");
		TimePeriod sleep = makeAverageSleepTime();
		////Log.i("start", sleep.start + "");
		////Log.i("end", sleep.end + "");
		ret = new DisplayTimeStrings(sleep.start, sleep.end);
		return ret;
	}
	public class DisplayTimeStrings{
		public String start;
		public String end;
		public DisplayTimeStrings(long s, long e){
			SimpleDateFormat form = new SimpleDateFormat("hh:mm a", context.getResources().getConfiguration().locale);
			start = form.format(new Date(s));
			end = form.format(new Date(e));
		}
	}
}
