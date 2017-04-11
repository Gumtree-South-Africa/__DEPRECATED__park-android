package com.ebay.park.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.ebay.park.base.BaseActivity;

import java.io.IOException;
import java.util.List;

public class LocationUtil {

	private LocationUtil() {
	}

	public static Location getLocation(BaseActivity aActivity) {
		Location aLocation = null;
		if (aActivity != null){
			LocationManager aLocationManager = (LocationManager) aActivity.getSystemService(Context.LOCATION_SERVICE);
            if (aLocationManager != null) {
                aLocation = retrieveLocation(aLocationManager, LocationManager.GPS_PROVIDER);
                if (aLocation == null) {
                    aLocation = retrieveLocation(aLocationManager, LocationManager.NETWORK_PROVIDER);
                    if (aLocation == null) {
                        aLocation = retrieveLocation(aLocationManager, LocationManager.PASSIVE_PROVIDER);
                    }
                }
            }
		}
		return aLocation;
	}

	public static Location getLocation(Context context) {
		Location aLocation = null;
		if (context != null){
			LocationManager aLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			if (aLocationManager != null) {
				aLocation = retrieveLocation(aLocationManager, LocationManager.GPS_PROVIDER);
				if (aLocation == null) {
					aLocation = retrieveLocation(aLocationManager, LocationManager.NETWORK_PROVIDER);
					if (aLocation == null) {
						aLocation = retrieveLocation(aLocationManager, LocationManager.PASSIVE_PROVIDER);
					}
				}
			}
		}
		return aLocation;
	}

	private static Location retrieveLocation(LocationManager aLocationManager, String aLocationProvider) {
		Location aLocation = null;
		if (aLocationManager.isProviderEnabled(aLocationProvider)){
			aLocation = aLocationManager.getLastKnownLocation(aLocationProvider);
		}
		return aLocation;
	}

	static public interface LocationResolverCallback {
		public void onLocationResolved(Location location);
	}
	
	static public class LocationResolverTask extends AsyncTask<Void, Void, Boolean> {

		private Context context;
		private Location location;
		private LocationResolverCallback callback;
		
		public LocationResolverTask(Context context, Location location, LocationResolverCallback callback) {
			this.context = context;
			this.location = location;
			this.callback = callback;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Geocoder geocoder = new Geocoder(context);
			boolean useLocation = true;
			try {
				List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);
				if(addresses == null || addresses.isEmpty()){
					useLocation = false;
				} else if(!TextUtils.equals(addresses.get(0).getCountryCode(), "US")) {
					useLocation = false;
				}
			} catch (IOException e) {
				useLocation = false;
			}
			return useLocation;
		}
		
		@Override
		protected void onPostExecute(Boolean useLocation) {
			if(callback != null){
				callback.onLocationResolved(useLocation ? location : null);				
			}
		}
		
	}

}
