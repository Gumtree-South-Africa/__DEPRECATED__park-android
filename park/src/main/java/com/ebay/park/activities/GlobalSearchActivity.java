package com.ebay.park.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.GlobalSearchFragment;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.SwrveEvents;
import com.swrve.sdk.SwrveSDK;

import java.util.Map;

public class GlobalSearchActivity extends BaseActivity implements FilterableActivity {

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
		getSupportActionBar().setElevation(0);

		if (savedInstanceState == null) {
			ScreenManager.showGlobalSearchFragment(this);
		}
		
		handleIntent(getIntent());
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
	public void onBackPressed() {
		SwrveSDK.event(SwrveEvents.SAVE_SEARCH_CANCEL);
		super.onBackPressed();
	}

	@Override
	protected void onNewIntent(Intent aIntent) {
		super.onNewIntent(aIntent);
		handleIntent(aIntent);
	}

	private void handleIntent(Intent aIntent) {
		Fragment aSearchableFragment = getSupportFragmentManager().findFragmentByTag(GlobalSearchFragment.TAG);
		if (aSearchableFragment != null){
			((SearchableFragment) aSearchableFragment).searchQuery(aIntent.getStringExtra(SearchManager.QUERY));
		}
	}

	@Override
	public void onFiltersConfirmed(Map<String, String> filters) {
		Fragment aSearchableFragment = getSupportFragmentManager().findFragmentByTag(GlobalSearchFragment.TAG);
		if (aSearchableFragment != null){
			((SearchableFragment) aSearchableFragment).onFilterSelected(filters, "");
		}
	}
	
}
