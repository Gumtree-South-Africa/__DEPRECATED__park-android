package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ebay.park.R;
import com.ebay.park.base.BaseItemListFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.ItemModel;
import com.ebay.park.requests.GroupRemoveItemsRequest;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.MessageUtil;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.List;

public class GroupItemsEditFragment extends BaseItemListFragment {

	private static final String EXTRA_ID = "group_id";
	private static final int BATCH_SIZE = 5;

	private long mGroupId = -1;
	private List<ItemModel> mItemsToRemove = new ArrayList<ItemModel>();
	private MenuItem mMenuDone;
	private boolean mBatchFailed = false;
	private String mErrorMessage;

	public static GroupItemsEditFragment forGroup(long groupId){
		GroupItemsEditFragment fragment = new GroupItemsEditFragment();
		Bundle args = new Bundle();
		args.putLong(EXTRA_ID, groupId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mGroupId = getArguments().getLong(EXTRA_ID, -1);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_done, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_done:
				confirmItemsRemoval();
				return true;
			case android.R.id.home:
				if(!confirmCancelation()){
					getBaseActivity().finish();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		this.mMenuDone = menu.findItem(R.id.action_done);
	}

	private void confirmItemsRemoval(){
		if(this.getRemovedItems() == null || this.getRemovedItems().isEmpty()){
			this.getBaseActivity().finish();
			return;
		}

		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.delete,
				R.string.group_delete_items_confirm, R.drawable.delete_xs)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doRemoveItems();
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
		}

		dialog.show();
	}

	private void doRemoveItems(){
		this.mItemsToRemove = new ArrayList<ItemModel>(this.getRemovedItems());
		if(this.mItemsToRemove == null || this.mItemsToRemove.isEmpty()){
			this.getBaseActivity().finish();
			return;
		} else {
			startNextBatchRequest();
		}
	}

	private void startNextBatchRequest(){
		if(this.mItemsToRemove == null || this.mItemsToRemove.isEmpty()){
			hideProgress();
			if(mBatchFailed){
				MessageUtil.showError(getBaseActivity(), mErrorMessage, getBaseActivity().getCroutonsHolder());
				this.getBaseActivity().finish();
			} else {
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.group_items_removed_success),
						getBaseActivity().getCroutonsHolder());
				this.getBaseActivity().finish();
			}
			return;
		}

		showProgress();
		if (this.mMenuDone != null) {
			this.mMenuDone.setVisible(false);
		}
		String[] itemsIds = new String[this.mItemsToRemove.size() > BATCH_SIZE ?
				BATCH_SIZE : this.mItemsToRemove.size()];
		for(int i = 0 ; i < itemsIds.length ; i++){
			itemsIds[i] = String.valueOf(this.mItemsToRemove.get(0).getId());
			this.mItemsToRemove.remove(0);
		}

		mSpiceManager.execute(new GroupRemoveItemsRequest(mGroupId, itemsIds, false), new GroupRemoveItemsListener());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		setTitle(R.string.group_items_edit);
	}

	private boolean confirmCancelation(){
		if(this.getRemovedItems() != null && !this.getRemovedItems().isEmpty()){
			new AlertDialog.Builder(getBaseActivity()).setTitle(R.string.cancel).setMessage(R.string.publish_cancel)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							getBaseActivity().finish();
						}
					}).setNegativeButton(R.string.no, null).show();
			return true;
		} else {
			return false;
		}
	}

	public boolean backPressed() {
		return confirmCancelation();
	}

	@Override
	protected void getDataFromServer() {
		showProgress();

		ItemListRequest.Builder builder = new ItemListRequest.Builder().page(page.longValue())
				.pageSize(((Integer) PAGE_SIZE).longValue()).forGroup(mGroupId);

		mSpiceManager.execute(builder.build(), new ItemListListener());
	}

	@Override
	protected boolean forEdition() {
		return true;
	}

	@Override
	public void onBackPressed() {
	}

	private class ItemListListener extends ListListener {}

	private class GroupRemoveItemsListener extends BaseNeckRequestListener<BaseParkResponse> {

		@Override
		public void onRequestError(Error error) {
			mBatchFailed = true;
			mErrorMessage = error.getMessage();
			startNextBatchRequest();
		}

		@Override
		public void onRequestSuccessfull(BaseParkResponse t) {
			startNextBatchRequest();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			mBatchFailed = true;
			mErrorMessage = exception.getMessage();
			startNextBatchRequest();
		}

	}

}
