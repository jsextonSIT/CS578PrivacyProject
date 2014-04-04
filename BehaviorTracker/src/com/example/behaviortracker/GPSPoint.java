package com.example.behaviortracker;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GPSPoint implements Parcelable{
	double latitude;
	double longitude;
	String date;
	public GPSPoint(double lat, double lon, String d){
		latitude = lat;
		longitude = lon;
		date = d;
	}

	    private GPSPoint(Parcel in) {
	        latitude = in.readDouble();
	        longitude = in.readDouble();
	        date = in.readString();
	    }

	    public int describeContents() {
	        return 0;
	    }

	    public void writeToParcel(Parcel out, int flags) {
	        out.writeDouble(latitude);
	        out.writeDouble(longitude);
	        out.writeString(date);
	    }

	    public static final Parcelable.Creator<GPSPoint> CREATOR = new Parcelable.Creator<GPSPoint>() {
	        public GPSPoint createFromParcel(Parcel in) {
	            return new GPSPoint(in);
	        }

	        public GPSPoint[] newArray(int size) {
	            return new GPSPoint[size];
	        }
	    };
	    public void logPoint(){
	    	Log.i("New GPS Point at:", date);
	    	Log.i("latitude", Double.toString(latitude));
	    	Log.i("longitude", Double.toString(longitude));
	    }
}
