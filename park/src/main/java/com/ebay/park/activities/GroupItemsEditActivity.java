package com.ebay.park.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.GroupItemsEditFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;

public class GroupItemsEditActivity extends BaseSessionActivity {

	public static final String EXTRA_GROUP_ID = "group_id";

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

		if (savedInstanceState == null) {
			final long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
			ScreenManager.showGroupItemsEditFragment(this, groupId);
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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			showConfirmCancelDialog();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		GroupItemsEditFragment fragment = (GroupItemsEditFragment) getSupportFragmentManager().findFragmentById(
				R.id.container);
		if (!fragment.backPressed()) {
			showConfirmCancelDialog();
		}
	}

	private void showConfirmCancelDialog() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(this, R.string.cancel,
				R.string.publish_cancel)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, this));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, this));
		}

		dialog.show();
	}

}
