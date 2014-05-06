package com.example.behaviortracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class AreaCodeStatistics {
	ArrayList<String> monthList;
	ArrayList<String> highestAreaCodeList;
	ArrayList<String> stateList;
	
	ArrayList<String> allCommedAreaCodes;
	ArrayList<String> allCommedStates;
	
	private Uri smsUri;
	private String smsloc = "content://sms/";
	
	ContentResolver cr;
	
	Context context;
	public AreaCodeStatistics(Context c){
		smsUri = Uri.parse(smsloc);
		context = c;
		cr = context.getContentResolver();
		doForLastXMonths(20);
	}
	
	public void getAllCommedAreaCodes(){
		allCommedAreaCodes = new ArrayList<String>();
    	String[] Mcolumns = {"address"};
		Cursor curs = cr.query(smsUri, Mcolumns, null, null, null);
		String area;
		String num;
		if(curs.moveToFirst()){
			do{
				num = curs.getString(curs.getColumnIndex(Mcolumns[0]));
				area = getAreaCode(num);
				if(area != "" && !allCommedAreaCodes.contains(area)){
					allCommedAreaCodes.add(area);
				}
				//Log.i("behavior", area);
			}while (curs.moveToNext());
		}
		String[] PColumns = {CallLog.Calls.NUMBER};
		curs = cr.query(CallLog.Calls.CONTENT_URI, PColumns, null, null, null);
		if(curs.moveToFirst()){
			do{
				num = curs.getString(curs.getColumnIndex(PColumns[0]));
				area = getAreaCode(num);
				if(!area.equals("") && !allCommedAreaCodes.contains(area)){
					allCommedAreaCodes.add(area);
				}
				//Log.i("behavior", area);
			}while (curs.moveToNext());
		}
	}
	public String getAreaCode(String num){
		String area;

		//Log.i("num", num);
		PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
		PhoneNumber nn;
		if(num.length() < 10 || containsLetters(num)){
			area = "";
		}
		else if(num.length() == 10|| !num.startsWith("+")){
			area = num.substring(0, 3);
		}
		
		else{
			area = "";
			try{
				nn = pnu.parse(num, "US");
				area = pnu.format(nn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL).substring(1, 4);
				
			}catch(Exception e){
				System.out.println(e.toString());
			}
			
		}


		//Log.i("area", area);
		return area;
	}
	public boolean containsLetters(String test){
		boolean ret = false;
		String check = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ@&^$;:[]{}*!#";
		for(int i = 0; i < check.length(); i++){
			if(test.contains("" + check.charAt(i))){
				ret = true;
			}
		}
		return ret;
	}
	public void makeStateList(){
		int i;
		String area;
		String st;
		getAllCommedAreaCodes();
		allCommedStates = new ArrayList<String>();
		for (i = 0; i < allCommedAreaCodes.size(); i++){
			area = allCommedAreaCodes.get(i);
			//Log.i("DEB", area);
			st = GetStateFromAreaCode.getState(Integer.parseInt(area));
			if(st != null && !allCommedStates.contains(st)){
				allCommedStates.add(st);
				//Log.i("HERE", "HERE");
			}
		}
	}
	public void doForLastXMonths(int x){
		int i,j, mi;
		mi = 0;

		stateList = new ArrayList<String>();
		monthList = new ArrayList<String>();
		Calendar curr = Calendar.getInstance(TimeZone.getDefault());
		Calendar tmp = Calendar.getInstance(TimeZone.getDefault());
		makeStateList();
		String[] mcol = {"date", "address"};
		String[] pcol = {CallLog.Calls.DATE, CallLog.Calls.NUMBER};
		Cursor cm = cr.query(smsUri, mcol, null, null, null);
		Cursor cp = cr.query(CallLog.Calls.CONTENT_URI, pcol, null, null, null);
		long t;
		String area;
		String state;
		ArrayList<Integer> sums;
		int max = 0;
		for(i = 0; i < x; i++){
			sums = new ArrayList<Integer>();
			for(j = 0; j < allCommedStates.size(); j++){
				sums.add(0);
			}
			if(cm.moveToFirst()){
				do{
					t = cm.getLong(cm.getColumnIndex(mcol[0]));
					area = getAreaCode(cm.getString(cm.getColumnIndex(mcol[1])));
					if(allCommedAreaCodes.contains(area)){
						state = GetStateFromAreaCode.getState(Integer.parseInt(area));
						if(allCommedStates.contains(state)){
							tmp.setTimeInMillis(t);
							if(tmp.get(Calendar.YEAR) == curr.get(Calendar.YEAR) && tmp.get(Calendar.MONTH) == curr.get(Calendar.MONTH))
								sums.set(allCommedStates.indexOf(state), sums.get(allCommedStates.indexOf(state)) + 1);
						}
					}
					
				}while(cm.moveToNext());
			}
			if(cp.moveToFirst()){
				do{
					t = cp.getLong(cp.getColumnIndex(pcol[0]));
					area = getAreaCode(cp.getString(cp.getColumnIndex(pcol[1])));
					if(allCommedAreaCodes.contains(area)){
						state = GetStateFromAreaCode.getState(Integer.parseInt(area));
						if(allCommedStates.contains(state)){
							tmp.setTimeInMillis(t);
							if(tmp.get(Calendar.YEAR) == curr.get(Calendar.YEAR) && tmp.get(Calendar.MONTH) == curr.get(Calendar.MONTH))
								sums.set(allCommedStates.indexOf(state), sums.get(allCommedStates.indexOf(state)) + 1);
						}
					}			
				}while(cp.moveToNext());				
			}
			max = 0;
			mi = 0;
			for(j = 0; j < sums.size(); j++){
				if(sums.get(j) > max){
					max = sums.get(j);
					mi = j;
				}
			}
			//Log.i("behavior max",""+ max);
			//Log.i("behavior state", allCommedStates.get(mi));
			//Log.i("behavior month year", makeMonthYearString(curr.get(Calendar.MONTH), curr.get(Calendar.YEAR)));
			//Log.i("size","" + i);
			if(max > 0)
				stateList.add(i, allCommedStates.get(mi));
			else
				stateList.add(i, "Unknown - no data available");
			monthList.add(i, makeMonthYearString(curr.get(Calendar.MONTH), curr.get(Calendar.YEAR)));
			curr.add(Calendar.MONTH, -1);
		}
	}
	public String makeMonthYearString(int month, int year){
		String ret = "";
		switch(month + 1){
			case 1: 
				ret = ret + "Jan";
				break;
			case 2:
				ret = ret + "Feb";
				break;
			case 3: 
				ret = ret + "Mar";
				break;
			case 4:
				ret = ret + "Apr";
				break;
			case 5: 
				ret = ret + "May";
				break;
			case 6:
				ret = ret + "Jun";
				break;
			case 7: 
				ret = ret + "Jul";
				break;
			case 8: 
				ret = ret + "Aug";
				break;
			case 9:
				ret = ret + "Sep";
				break;
			case 10:
				ret = ret + "Oct";
				break;
			case 11: 
				ret = ret + "Nov";
				break;
			case 12: 
				ret = ret + "Dec";
				break;
		}
		ret = ret + " ";
		ret = ret + year;
		return ret;
	}
}
