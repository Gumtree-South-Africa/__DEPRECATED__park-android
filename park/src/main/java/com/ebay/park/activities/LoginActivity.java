package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.BackPressable;
import com.ebay.park.utils.DeviceUtils;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;


public class LoginActivity extends BaseActivity {

	public static final String EXTRA_FOR_REGISTRATION = "EXTRA_REGISTRATION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container);

		if (DeviceUtils.isDeviceLollipopOrHigher()) {
			Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
			parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
			parkToolbar.setNavigationIcon(R.drawable.icon_white_back);
			setSupportActionBar(parkToolbar);
		} else {
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			getSupportActionBar().setCustomView(R.layout.actionbar);
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_white_back);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().hide();

		if (savedInstanceState == null) {
			if (!isRegistration(getIntent())){
				ScreenManager.showLoginFragment(this);
			}else{
				ScreenManager.showUserRegisterFragmentWithoutStack(this);
			}
		}
	}

	@Override
	public ViewGroup getCroutonsHolder() {
		ViewGroup view = (ViewGroup) findViewById(R.id.crouton_handle);
		if (view != null) {
			view.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		}
		return view;
	}

	private static final boolean isRegistration(Intent aIntent) {
		return aIntent.hasExtra(EXTRA_FOR_REGISTRATION) && aIntent.getBooleanExtra(EXTRA_FOR_REGISTRATION, false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		fragment.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null){
			LoginManager.getInstance().logOut();
		}
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
		if (fragment!=null) {
			((BackPressable) fragment).onBackPressed();
		}
		super.onBackPressed();
	}

	@Override
	public boolean onSupportNavigateUp() {
		if (!hasStackedFragments(getSupportFragmentManager())){
			finish();
		}
		return true;
	}

	public static boolean hasStackedFragments(FragmentManager aManager){
		boolean hasStackedFragments = aManager.getBackStackEntryCount() > 0;
		if (hasStackedFragments){
			aManager.popBackStack();
		}
		return hasStackedFragments;
	}

}