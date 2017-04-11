package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.BackPressable;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.utils.DeviceUtils;

public class ChatActivity extends BaseSessionActivity {

	public static final String CONVERSATION = "conversation";
	public static final String CONVERSATION_ID = "conversationId";
	public static final String ITEM_ID = "itemId";
	public static final String ROLE = "role";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container);

		if (DeviceUtils.isDeviceLollipopOrHigher()) {
			Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
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

			if (getIntent().hasExtra(ITEM_ID)) {
				ScreenManager.showChatFragment(this, getIntent().getLongExtra(CONVERSATION_ID, -1), getIntent()
						.getLongExtra(ITEM_ID, -1), getIntent().getStringExtra(ROLE));
			} else {
				ScreenManager.showChatFragment(this,
						(ConversationModel) getIntent().getSerializableExtra(CONVERSATION),
						getIntent().getStringExtra(ROLE));
			}

		}
	}

	@Override
	public ViewGroup getCroutonsHolder() {
		//This activity is not getting behind the status bar
		return (ViewGroup) findViewById(R.id.crouton_handle);
	}

	@Override
	public boolean onSupportNavigateUp() {
		boolean fragmentBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
		if (fragmentBack) {
			getSupportFragmentManager().popBackStack();
		} else {
			finish();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
		if (fragment!=null) {
			((BackPressable) fragment).onBackPressed();
		}
		super.onBackPressed();
	}

}
