package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.DeviceUtils;

/**
 * Social Networks Activity
 * 
 * @author Nicolás Matias Fernández
 * 
 */

public class SocialNetworksActivity extends BaseSessionActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_social_networks);

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

		if (savedInstanceState == null) {
			ScreenManager.showSocialNetworksConfigScreen(this);
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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		ScreenManager.showSocialNetworksConfigScreen(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BaseFragment fragment = (BaseFragment) getSupportFragmentManager()
				.findFragmentById(R.id.container_social_networks);
		fragment.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}		

	@Override
	public void onBackPressed() {
		finish();
	}

}
