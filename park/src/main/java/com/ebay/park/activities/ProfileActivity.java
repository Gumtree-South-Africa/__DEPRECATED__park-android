package com.ebay.park.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.utils.DeviceUtils;

import java.util.Map;

/**
 * Activity to show users profile.
 * 
 * @author federico.perez
 * @author Nicol�s Mat�as Fern�ndez
 * 
 */
public class ProfileActivity extends BaseSessionActivity implements FilterableActivity {

	public static final String USERNAME_EXTRA = "username";
	public static final String CONTENT_EXTRA = "content_extra";
	
	public static final int NEGOCIATION_AS_BUYER = 1;
	public static final int NEGOCIATION_AS_SELLER = 2;
	public static final int PROFILE_EDIT = 3;
	public static final int FOLLOWERS = 4;
	public static final int FOLLOWING = 5;

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

		if (getSupportActionBar()!=null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState == null) {
			String username = null;
			if (getIntent().hasExtra(USERNAME_EXTRA)) {
				username = getIntent().getStringExtra(USERNAME_EXTRA);
			}
			if (getIntent().hasExtra(CONTENT_EXTRA)){
				switch (getIntent().getExtras().getInt(CONTENT_EXTRA)) {
				case NEGOCIATION_AS_BUYER:
					ScreenManager.showConversationListAsBuyerFragment(false, this);
					break;
				case NEGOCIATION_AS_SELLER:
					ScreenManager.showConversationListAsSellerFragment(false, this);
					break;
				case PROFILE_EDIT:
					ScreenManager.showProfileEditFragment(false, this);
					break;
				case FOLLOWERS:
					ScreenManager.showFollowersFragment(false, this, username);
					break;
				case FOLLOWING:
					ScreenManager.showFollowingFragement(false, this, username);
					break;				
				default:
					onBackPressed();
					break;
				}
			}else{
				if (username != null) {
					ScreenManager.showUserProfileFragment(this, username,
							false);
				} else {
					ScreenManager.showCurrentUserProfileFragment(this);
				}
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

	@Override
	protected void onNewIntent(Intent aIntent) {
		super.onNewIntent(aIntent);
		handleIntent(aIntent);
	}

	private void handleIntent(Intent aIntent) {

		if (Intent.ACTION_SEARCH.equals(aIntent.getAction())) {
			final String query = aIntent.getStringExtra(SearchManager.QUERY);
			((SearchableFragment) getSupportFragmentManager().findFragmentById(R.id.container)).searchQuery(query);
		}
	}

	@Override
	public void onFiltersConfirmed(Map<String, String> filters) {
		((SearchableFragment) getSupportFragmentManager().findFragmentById(R.id.container)).onFilterSelected(filters, "");
	}

	@Override
	public boolean onSupportNavigateUp() {
		boolean fragmentBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
		if (fragmentBack) {
			getSupportFragmentManager().popBackStack();
			return true;
		} else {
			onBackPressed();
			return true;
		}
	}

}
