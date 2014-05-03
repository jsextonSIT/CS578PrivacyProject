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

public class PhoneAndMessageInactivityStatistics {
	Hashtable<Long, ActiveTime> activeTable;
	ArrayList<TimePeriod> inactivities;
	ArrayList<TimePeriod> largestInactivities;
	//Each entry is the start of a quantum of time - for entry 
	long start;
	long end;
	long quantum;
	Resources res;
	TimePeriod commonSubset;
	
	Context context;
	String CONTENT_URL = "content://sms/";
	
	String SHARED_PREFS_NAME;
	SharedPreferences sp;
	SharedPreferences.Editor spe;
	
	ContentResolver cr;
	Uri uri;
	
	public PhoneAndMessageInactivityStatistics(long q, Context c){
		start = 9223372036854775807L;
		end = 0;
		getAllMessages();
		getAllPhoneCalls();
		quantum = q;
		activeTable = new Hashtable<Long, ActiveTime>();
		long i;
		for(i = start; i < end; i += q){
			activeTable.put(i, new ActiveTime());
		}
		inactivities = new ArrayList<TimePeriod>();
		largestInactivities = new ArrayList<TimePeriod>();
		context = c;
		
		res = context.getResources();
		SHARED_PREFS_NAME = res.getString(R.string.SHARED_PREFS_FILENAME);
		sp = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		spe = sp.edit();
		
		cr = context.getContentResolver();
		uri = Uri.parse(CONTENT_URL);
	}
	
	public void getAllMessages(){
		long mt;
		long last;
		String protocol;
		String[] columns = {"date", "protocol"};
		Cursor curs = cr.query(uri, columns, null, null, "date desc");
		if (curs.moveToFirst()){
				if(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))) > end)
					end = Utilities.getPreviousMidnightInMillis(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))));
			
			do{
				mt = Long.parseLong(curs.getString(curs.getColumnIndex(columns[0])));
				protocol = curs.getString(curs.getColumnIndex(columns[1]));
				if(protocol == null){
					//null if it is a sent message
					addMessage(mt);
				}
				last = mt;
			}while(curs.moveToNext());
			if(last < start){
				start = Utilities.getNextMidnightInMillis(last);
			}
		}
	}

	public void getAllPhoneCalls(){
		long dur;
		long ct;
		long last;
		int calltype;
		String[] columns = {CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
		String order = CallLog.Calls.DATE + " DESC";
		Cursor curs = cr.query(CallLog.Calls.CONTENT_URI, columns, null, null, order);
		if(curs.moveToFirst()){
			if(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))) > end)
				end = Utilities.getPreviousMidnightInMillis(Long.parseLong(curs.getString(curs.getColumnIndex(columns[0]))));
			do{
				ct = curs.getLong(curs.getColumnIndex(columns[0]));
				dur = curs.getLong(curs.getColumnIndex(columns[1]));
				calltype = curs.getInt(curs.getColumnIndex(columns[2]));
				
				if(calltype == CallLog.Calls.OUTGOING_TYPE){
					addPhoneCall(ct, dur);
				}
				last = ct;
			}while(curs.moveToNext());

			if(last < start){
				start = Utilities.getNextMidnightInMillis(last);
			}
		}
	}
	
	public void addMessage(long messageTime){
		long index = Utilities.getPreviousQuantumInMillis(messageTime, quantum);
		ActiveTime at = activeTable.get(index);
		at.active = true;
	}
	
	public void addPhoneCall(long callStart, long callDuration){
		long index = Utilities.getPreviousQuantumInMillis(callStart, quantum);
		long i;
		long stop;
		if(callDuration % quantum == 0){
			stop = callDuration;
		}
		else{
			stop = callDuration + (quantum - (callDuration % quantum));
		}
		ActiveTime at = activeTable.get(index);
		at.active = true;
		for(i = 0; i < stop; i += quantum){
			at = activeTable.get(i + callStart);
			at.active = true;
		}
	}
	
	public void findInactiveTimes(){
		inactivities = new ArrayList<TimePeriod>();
		long i;
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
				if(tmp.start >= i && tmp.end <= i + Utilities.MILLIS_PER_DAY && maxInactivity <= curr){
					maxInactivity = curr;
					maxPerDay = tmp;
				}
			}
			largestInactivities.add(maxPerDay);
		}
	}
	
	public void addInactiveTimesForOneDay(ArrayList<TimePeriod> lst, long s){
		long i, e;
		int j;
		e = 0;
		boolean zerostart = false;
		boolean zeroend = false;
		TimePeriod tmp;
		for(i = s; i < s + Utilities.MILLIS_PER_DAY; i += quantum){
			if(!activeTable.get(i).active){
				for(e = i; e < s + Utilities.MILLIS_PER_DAY; e+= quantum){
					if(activeTable.get(e).active){
						if(i == 0)
							zerostart = true;
						if(e == s + Utilities.MILLIS_PER_DAY - quantum)
							zeroend = true;
						lst.add(new TimePeriod(i, e));
						i = e;
						break;
					}
				}
			}
		}
		
		//Handle cyclic possibility
		if(zerostart && zeroend){
			for(j = 0; j < lst.size(); j++){
				tmp = lst.get(j);
				if(tmp.start == s){
					e = tmp.end;
					lst.remove(j);
					break;
				}
			}
			for(j = 0; j < lst.size(); j++){
				tmp = lst.get(j);
				if(tmp.end == s + Utilities.MILLIS_PER_DAY - quantum){
					tmp.end = e;
				}
			}
		}
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
		ArrayList<TimePeriod> reduced = applyThreshholds();
		ArrayList<Long> starts = new ArrayList<Long>();
		ArrayList<Long> ends = new ArrayList<Long>();
		long avgs = 0;
		long avge = 0;
		for(i = 0; i < reduced.size(); i++){
			starts.add(i, reduced.get(i).start);
			ends.add(i, reduced.get(i).end);
		}
		
		for(i = 0; i < reduced.size(); i++){
			avgs += starts.get(i);
			avge += ends.get(i);
		}
		avgs =(long) Math.floor(avgs/reduced.size());
		avge = (long) Math.floor(avge/reduced.size());
		return new TimePeriod(avgs, avge);
	}
	
	public ArrayList<TimePeriod> applyThreshholds(){
		double fourth;
		ArrayList<TimePeriod> threshheld = new ArrayList<TimePeriod>();
		anonymizeDays();
		orderByDuration(threshheld);
		fourth = threshheld.size()/4;
		for(int i = 0; i < (int) Math.floor(fourth); i++){
			threshheld.remove(i);
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
			lst.add(j, largestInactivities.get(j));
		}
	}
	
	public void anonymizeDays(){
		int i;
		TimePeriod tmp;
		for(i = 0; i < largestInactivities.size(); i++){
			tmp = largestInactivities.get(i);
			tmp.start %= Utilities.MILLIS_PER_DAY;
			tmp.end %= Utilities.MILLIS_PER_DAY;
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
		findLargestInactiveTimes();
		TimePeriod sleep = makeAverageSleepTime();
		ret = new DisplayTimeStrings(sleep.start, sleep.end);
		return ret;
	}
	public class DisplayTimeStrings{
		public String start;
		public String end;
		public DisplayTimeStrings(long s, long e){
			SimpleDateFormat form = new SimpleDateFormat("hh:mm a");
			start = form.format(new Date(s));
			end = form.format(new Date(e));
		}
	}
}
