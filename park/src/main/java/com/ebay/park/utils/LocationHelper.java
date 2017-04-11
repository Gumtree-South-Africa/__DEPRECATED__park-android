package com.ebay.park.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper implements LocationListener {

	public static final String ZIP_CODE_NOT_USA = "ZCNUS";

	private static final long MIN_TIME_UPDATES = 10 * 1000;
	private static final long MIN_DISTANCE_UPDATES = 100;
	private static final String US_CODE = "US";
	private static final String PR_CODE = "PR";
	private Context mContext;
	private LocationResponseCallback mLocationResponse;
	private LocationManager mLocationManager;
	private String mLatlng;
	private String mProvider;
	private boolean mConnected;

	public LocationHelper(Context aContext, LocationResponseCallback locationResponseCallback) {
		mContext = aContext;
		mLocationResponse = locationResponseCallback;
		initLocationManager(aContext);
	}

	private void initLocationManager(Context aContext) {
		mLocationManager = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		// mProvider = mLocationManager.getBestProvider(criteria, false); //TODO
		// Always returns "gps" instead of "network" when should

		boolean statusOfNet = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (statusOfNet) {
			mProvider = "network";
		} else {
			boolean statusOfGps = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (statusOfGps) {
				mProvider = "gps";
			} else {
				mProvider = null;
			}
		}

		if (mProvider != null) {
			setConnected(true);
		} else {
			setConnected(false);
		}

		requestLocationUpdates();
	}

	public void requestLocationUpdates() {
		if (isConnected()) {
			mLocationManager.requestLocationUpdates(mProvider, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this);
		}
	}

	public void removeUpdates() {
		if (isConnected()) {
			mLocationManager.removeUpdates(this);
			setConnected(false);
		}
	}

	@SuppressLint("NewApi")
	public void getAddress(Location currentLocation) {
		if (Geocoder.isPresent()) {
			new GetAddressTask(mContext).execute(currentLocation);
		}
	}

	protected class GetAddressTask extends AsyncTask<Location, Void, Pair<String, String>> {

		Context localContext;

		public GetAddressTask(Context context) {
			super();
			localContext = context;
		}

		@Override
		protected Pair<String, String> doInBackground(Location... params) {
			Pair<String, String> emptyPair = new Pair<String, String>("", "");
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());
			Location location = params[0];
			List<Address> addresses = null;

			try {
				addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			} catch (IOException exception1) {
				exception1.printStackTrace();
				return emptyPair;
			} catch (IllegalArgumentException exception2) {
				exception2.printStackTrace();
				return emptyPair;
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				if (address.getCountryCode().equals(US_CODE) || address.getCountryCode().equals(PR_CODE)) {
					String addressText;
					if (!TextUtils.isEmpty(address.getAdminArea())) {
						addressText = localContext.getString(R.string.address_output_string, address.getLocality(),
								address.getAdminArea());
					} else {
						addressText = localContext.getString(R.string.address_output_string, address.getLocality(),
								address.getCountryCode());
					}

					return Pair.create(address.getPostalCode(), addressText);
				} else {
					return Pair.create(ZIP_CODE_NOT_USA, ZIP_CODE_NOT_USA);
				}
			} else {
				return emptyPair;
			}
		}

		@Override
		protected void onPostExecute(Pair<String, String> zipCodeAddress) {
			mLocationResponse.setZipCode(zipCodeAddress.first);
			mLocationResponse.setAddress(zipCodeAddress.second);
		}
	}

	public String getLatlng() {
		return mLatlng;
	}

	public void setLatlng(String latlng) {
		this.mLatlng = latlng;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mLatlng == null) {
			mLatlng = getLatLng(location);
			removeUpdates();
			getAddress(location);
		}
	}

	public boolean isConnected() {
		return mConnected;
	}

	public void setConnected(boolean connected) {
		this.mConnected = connected;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	public String getLatLng(Location currentLocation) {
		if (currentLocation != null) {
			return getLatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		} else {
			return "";
		}
	}

	private String getLatLng(double lat, double lng) {
		return lat + "," + lng;
	}

	public interface LocationResponseCallback {
		void setAddress(String address);

		void setZipCode(String zipCode);
	}
}