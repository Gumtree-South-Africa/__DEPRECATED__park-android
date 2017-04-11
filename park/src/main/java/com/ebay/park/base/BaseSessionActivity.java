package com.ebay.park.base;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.LogoutModel;
import com.ebay.park.requests.LogoutRequest;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

/**
 * Base activity for authenticated screens.
 * 
 * @author federico.perez
 * 
 */
public abstract class BaseSessionActivity extends BaseActivity {

	protected NeckSpiceManager mSpiceManager = new NeckSpiceManager();

	private ProgressDialog mLogoutDialog;
	private Tracker mGoogleAnalyticsTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerReceiver(broadcastReceiver, new IntentFilter(Constants.LOGOUT_BROADCAST));
		mGoogleAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSpiceManager.start(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mSpiceManager.shouldStop();
	}

	public void onResume() {
		super.onResume();
		ParkApplication.sCurrentContext = this;
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.LOGOUT_BROADCAST)) {
				finish();
			}
		}
	};

	/**
	 * Fires a {@link LogoutRequest} that will log the user out of the app on
	 * success.
	 */
	public void logout(boolean fromForceLogout) {
		mLogoutDialog = buildLogoutDialog();
		mLogoutDialog.show();
		// Facebook:
		//Check if user is currently logged in
		if (AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null){
			LoginManager.getInstance().logOut();
		}
		FacebookUtil.logoutFromFacebook(BaseSessionActivity.this);
        if (!fromForceLogout) {
            mSpiceManager.executeCacheRequest(new LogoutRequest(GCMUtils.getRegistrationId(BaseSessionActivity.this)),
                    new LogoutListener());
        } else {
            if (mLogoutDialog != null && mLogoutDialog.isShowing()) {
                mLogoutDialog.dismiss();
            }
            ScreenManager.logout(BaseSessionActivity.this);
        }
    }

	private ProgressDialog buildLogoutDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(R.string.logout);
		dialog.setMessage(getString(R.string.loggingout));
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		return dialog;
	}

	private class LogoutListener extends BaseNeckRequestListener<LogoutModel> {

		@Override
		public void onRequestError(Error error) {
			mGoogleAnalyticsTracker.send(new HitBuilders.EventBuilder()
					.setCategory("Server action")
					.setAction("Logout fail")
					.setLabel("The logout call returned error").build());

			if (mLogoutDialog != null && mLogoutDialog.isShowing()) {
				mLogoutDialog.dismiss();
				switch (error.getErrorCode()) {
				case ResponseCodes.Signout.EMPTY_TOKEN:
				case ResponseCodes.Signout.USER_NOT_FOUND:
				case ResponseCodes.Signout.USER_UNAUTHORIZED:
					Logger.warn(error.getMessage());
					break;
				default:
					Logger.warn("Unknown error code: " + error.getErrorCode());
					break;
				}
				ScreenManager.logout(BaseSessionActivity.this);
			}
		}

		@Override
		public void onRequestSuccessfull(LogoutModel t) {
			mGoogleAnalyticsTracker.send(new HitBuilders.EventBuilder()
					.setCategory("Server action")
					.setAction("Logout success")
					.setLabel("The logout call succeed").build());

			SwrveSDK.event(SwrveEvents.LOGOUT_SUCCESS);
			if (mLogoutDialog != null && mLogoutDialog.isShowing()) {
				mLogoutDialog.dismiss();
			}
			ScreenManager.logout(BaseSessionActivity.this);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			mGoogleAnalyticsTracker.send(new HitBuilders.EventBuilder()
					.setCategory("Server action")
					.setAction("Logout fail")
					.setLabel("The logout call returned error").build());

			if (mLogoutDialog != null && mLogoutDialog.isShowing()) {
				mLogoutDialog.dismiss();
			}
			exception.printStackTrace();
			MessageUtil.showError(BaseSessionActivity.this, getString(R.string.error_no_internet),
					getCroutonsHolder());
		}

	}
}
