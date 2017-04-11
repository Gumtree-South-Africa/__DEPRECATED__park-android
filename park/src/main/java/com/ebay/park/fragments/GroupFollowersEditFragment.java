package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseUserListFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.FollowerModel;
import com.ebay.park.requests.GroupRemoveFollowersRequest;
import com.ebay.park.requests.GroupRemoveItemsRequest;
import com.ebay.park.requests.SearchUserRequest;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.MessageUtil;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.List;

public class GroupFollowersEditFragment extends BaseUserListFragment {

	private static final String EXTRA_ID = "group_id";
	private static final int BATCH_SIZE = 5;
	
	private List<FollowerModel> mItemsToRemove = new ArrayList<FollowerModel>();
	private MenuItem mMenuDone;
	private boolean mBatchFailed = false;
	private String mErrorMessage;
	
	private long mGroupId = -1;
	
	public static GroupFollowersEditFragment forGroup(long groupId){
		GroupFollowersEditFragment fragment = new GroupFollowersEditFragment();
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
			confirmFollowersRemoval();
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
	
	private void confirmFollowersRemoval(){
		if(this.getRemovedFollowers() == null || this.getRemovedFollowers().isEmpty()){
			this.getBaseActivity().finish();
			return;
		}

		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.delete,
				R.string.group_delete_followers_confirm, R.drawable.delete_xs)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doRemoveFollowers();
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
	
	private void doRemoveFollowers(){
		this.mItemsToRemove = new ArrayList<FollowerModel>(this.getRemovedFollowers());
		if(this.mItemsToRemove == null || this.mItemsToRemove.isEmpty()){
			this.getBaseActivity().finish();
			return;
		} else {
			startNextBatchRequest();
		}
	}
	
	private void startNextBatchRequest(){
		if(this.mItemsToRemove == null || this.mItemsToRemove.isEmpty()){
			this.doRemoveItems();
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
		
		mSpiceManager.execute(new GroupRemoveFollowersRequest(mGroupId, itemsIds), new GroupRemoveFollowersListener());
	}
	
	private void doRemoveItems(){
		this.mItemsToRemove = new ArrayList<FollowerModel>(this.getRemovedFollowers());
		startNextItemsBatchRequest();
	}
	
	private void startNextItemsBatchRequest(){
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
		String[] itemsIds = new String[this.mItemsToRemove.size() > BATCH_SIZE ?
				BATCH_SIZE : this.mItemsToRemove.size()];
		for(int i = 0 ; i < itemsIds.length ; i++){
			itemsIds[i] = String.valueOf(this.mItemsToRemove.get(0).getId());
			this.mItemsToRemove.remove(0);
		}
		
		mSpiceManager.execute(new GroupRemoveItemsRequest(mGroupId, itemsIds, true), new GroupRemoveItemsListener());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		setTitle(R.string.group_followers_edit);
	}
	
	private boolean confirmCancelation(){
		if(this.getRemovedFollowers() != null && !this.getRemovedFollowers().isEmpty()){
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
		
		SearchUserRequest.Builder builder = new SearchUserRequest.Builder(
				ParkApplication.getInstance().getUsername(), "").page(page)
				.pageSize(PAGE_SIZE).forGroup(mGroupId);
		
		mSpiceManager.execute(builder.build(), new UserListener());
	}
	
	@Override
	protected boolean forEdition() {
		return true;
	}

	@Override
	public void onBackPressed() {
	}

	private class UserListener extends ListListener {}
	
	private class GroupRemoveFollowersListener extends BaseNeckRequestListener<BaseParkResponse> {

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
	
	private class GroupRemoveItemsListener extends BaseNeckRequestListener<BaseParkResponse> {

		@Override
		public void onRequestError(Error error) {
			mBatchFailed = true;
			mErrorMessage = error.getMessage();
			startNextItemsBatchRequest();
		}

		@Override
		public void onRequestSuccessfull(BaseParkResponse t) {
			startNextItemsBatchRequest();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			mBatchFailed = true;
			mErrorMessage = exception.getMessage();
			startNextItemsBatchRequest();
		}
		
	}

}
