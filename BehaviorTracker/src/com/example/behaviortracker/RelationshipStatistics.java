package com.example.behaviortracker;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

public class RelationshipStatistics {
	//this class aims to compute two things -  a list of contact names/numbers, and a list of scores associated with
	//them, based on how close they are to the user.
	ArrayList<String> contacts;
	ArrayList<Integer> scores;
	private Uri contactsUri;
	private Uri smsUri;
	private String smsloc = "content://sms/";
	ContentResolver cr;
	Context c;
	
	ArrayList<Contact> cs; 
	ArrayList<Message> ms;
	ArrayList<PCall> ps;
	
	public RelationshipStatistics(int numscores, Context context){
		c = context;
		contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		smsUri = Uri.parse(smsloc);
		cr = c.getContentResolver();
		getDisplay(numscores);
		int i;

		for(i = 0; i < contacts.size(); i++){
			//Log.i("behavior tracker", contacts.get(i) + ": " + scores.get(i));
		}
	}
	
	public void getDisplay(int numScores){
		contacts = new ArrayList<String>();
		scores = new ArrayList<Integer>();
		
		ArrayList<String> c = new ArrayList<String>();
		ArrayList<Integer> s = new ArrayList<Integer>();
		int sz = numScores;
		int i;
		Contact ctmp;
		getAllContacts();
		if(sz > cs.size() || sz < 0){
			sz = cs.size();
		}
		

		for(i = 0; i < cs.size(); i++){
			ctmp = cs.get(i);
			if(contacts.contains(ctmp.name))
				contacts.add(ctmp.name + " (" + ctmp.number +")");
			else if(ctmp.name != null && ctmp.name.length() > 0){
				contacts.add(ctmp.name);
			}
			else
				contacts.add(ctmp.number);
			scores.add(ctmp.score);
		}
		
		sortLists(contacts, scores);
		
		for(i = 0; i < sz; i++){
			ctmp = new Contact();
			ctmp.name = contacts.get(i);
			ctmp.score = scores.get(i);
			c.add(ctmp.name);
			s.add(ctmp.score);
		}
		
		contacts = c;
		scores = s;
	}
	
	public void getAllContacts(){
		Contact tmp;

    	String[] columns = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
		Cursor curs = cr.query(contactsUri, columns, null, null, null);
		
		getAllMessages();
		getAllPhoneCalls();
		
		cs = new ArrayList<Contact>();
		
		if(curs.moveToFirst()){
			do{
				tmp = new Contact();
				tmp.name = curs.getString(curs.getColumnIndex(columns[0]));
				tmp.number = curs.getString(curs.getColumnIndex(columns[1]));
				calculateScore(tmp);
				cs.add(tmp);
			}while (curs.moveToNext());
		}
	}
	
	public void getAllMessages(){
		Message tmp;
		ms = new ArrayList<Message>();
    	String[] columns = {"date", "address", "protocol"};
		Cursor curs = cr.query(smsUri, columns, null, null, null);
		
		if(curs.moveToFirst()){
			do{
				tmp = new Message();
				tmp.msgtime = curs.getLong(curs.getColumnIndex(columns[0]));
				tmp.number = curs.getString(curs.getColumnIndex(columns[1]));
				tmp.prot = curs.getString(curs.getColumnIndex(columns[2]));
				ms.add(tmp);
			}while (curs.moveToNext());
		}
	}
	
	public void getAllPhoneCalls(){
		PCall tmp;

    	String[] columns = {CallLog.Calls.DATE, CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
		Cursor curs = cr.query(CallLog.Calls.CONTENT_URI, columns, null, null, null);
		ps = new ArrayList<PCall>();
		if(curs.moveToFirst()){
			do{
				tmp = new PCall();
				tmp.ptime = curs.getLong(curs.getColumnIndex(columns[0]));
				tmp.number = curs.getString(curs.getColumnIndex(columns[1]));
				tmp.pdur = curs.getLong(curs.getColumnIndex(columns[2]));
				tmp.ptype = curs.getInt(curs.getColumnIndex(columns[3]));
				ps.add(tmp);
			}while (curs.moveToNext());
		}
	}
	
	public void calculateScore(Contact c){
		int sc = 0;
		int numSent, numRec, numMissed;
		numSent = 0;
		numRec = 0;
		String n = c.number;
		int i;
		Message tmpm;
		for(i = 0; i < ms.size(); i++){
			tmpm = ms.get(i);
			if(tmpm.number.equals(n)){
				if(tmpm.prot == null){
					numSent++;
				}
				else{
					numRec++;
				}
			}
		}
		sc += numSent + numRec;
		sc -= (numRec - numSent)/2; //penalty for only receiving and never sending to a person
	
		numSent = 0;
		numRec = 0;
		numMissed = 0;
		PCall tmpp;
		for(i = 0; i < ps.size(); i++){
			tmpp = ps.get(i);
			if(tmpp.number.equals(n)){
				if(tmpp.ptype == CallLog.Calls.INCOMING_TYPE){
					numRec++;
				}
				else if(tmpp.ptype == CallLog.Calls.OUTGOING_TYPE){
					numSent++;
				}
				else if(tmpp.ptype == CallLog.Calls.MISSED_TYPE){
					numMissed++;
				}
			}
		}
		
		sc += numSent + numRec;
		sc -= (numRec - numSent)/4;
		sc -= numMissed/2;
		c.score = sc;
	}
	
	public void sortLists(ArrayList<String> x, ArrayList<Integer> y){
		ArrayList<String> xs = new ArrayList<String>();
		ArrayList<Integer> ys = new ArrayList<Integer>();
		String tmpst;
		Integer tmpin;
		int i, j;
		if(x.size() == y.size()){
			for(i = 0; i < x.size(); i++){
				tmpst = x.get(i);
				tmpin = y.get(i);
				for(j = 0; j < xs.size() && ys.get(j) > tmpin; j++){

				}
				xs.add(j, tmpst);
				ys.add(j, tmpin);
			}
		}
		
		contacts = xs;
		scores = ys;
	}
	
	public class Contact{
		public String name;
		public String number;
		public int score;
	}
	
	public class Message{
		public long msgtime;
		public String number;
		public String prot;
	}
	
	public class PCall{
		public long ptime;
		public long pdur;
		public String number;
		public int ptype;
	}
}
