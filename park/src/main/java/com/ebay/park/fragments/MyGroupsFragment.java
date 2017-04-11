package com.ebay.park.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseGroupListFragment;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.Subscribable;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.model.UnreadItemsGroupsModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupListRequest;
import com.ebay.park.requests.GroupSearchRequest;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.SendVerificationMailRequest;
import com.ebay.park.requests.UnreadItemsGroupsRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.*;
public class MyGroupsFragment extends BaseTabFragment implements Subscribable {

	public static final String TEST_TAG = MyGroupsFragment.class.getSimpleName();
	public static final String TAG = "TagMyGroups";
	private Boolean mOwnedNewItems = false;
	private Boolean mSubscribedNewItems = false;
	private GroupsTabsAdapter mTabAdapter;

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setTitle(R.string.tab_title_groups);
		mTabAdapter = new GroupsTabsAdapter(getChildFragmentManager());
		super.onViewCreated(rootView, savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT) {
			mPagerChangeListener = new GroupPageChangeListener();
		}
		showCreateGroupButton();
	}

	@Override
	protected TabAdapter getAdapter() {
		return mTabAdapter;
	}

	@Override
	protected void releaseResources() {
		mTabAdapter = null;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!PreferencesUtil.hasShownGroupAccountVerificationMessage(getActivity())) {
			mSpiceManager.execute(new ProfileRequest(ParkApplication.getInstance().getUsername()),
					new VerifiedListener());
		}
		if (ParkApplication.sFgmtOrAct_toGo == UnloggedNavigations.GROUPS){
			mViewPager.setCurrentItem(GroupsTabsAdapter.NEARBY);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mSpiceManager.execute(new UnreadItemsGroupsRequest(), new UnreadItemsGroupsListener());
		if (ParkApplication.sFgmtOrAct_toGo == UnloggedNavigations.GROUPS && !ParkApplication.sJustLogged){
			ParkApplication.sFgmtOrAct_toGo = null;
		}
	}

	@Override
	public void onRefresh() {
	}

	@Override
	protected int getOffsetLimit() {
		return 2;
	}

	@Override
	public void onBackPressed() {
	}

	private class GroupsTabsAdapter extends TabAdapter {

		public static final int SUBSCRIBED = 0;
		public static final int MY_GROUPS = 1;
		public static final int NEARBY = 2;

		private final SparseArray<Fragment> mFragments;

		public GroupsTabsAdapter(FragmentManager fm) {
			super(fm);
			GroupBoldableCallback mCallback = new GroupBoldableCallback();
			mFragments = new SparseArray<>();
			mFragments.put(SUBSCRIBED, BoldableGroupFragment.addCallback(new GroupSubscribedFragment(), mCallback));
			mFragments.put(MY_GROUPS, BoldableGroupFragment.addCallback(new GroupOwnedFragment(), mCallback));
			mFragments.put(NEARBY, new GroupNearestFragment());
			ParkApplication.getInstance().getGroupsWithResetedNotifications().clear();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int titleRes;
			switch (position) {
				case SUBSCRIBED:
					titleRes = R.string.group_tab_subscribed;
					break;
				case MY_GROUPS:
					titleRes = R.string.group_tab_my_groups;
					break;
				case NEARBY:
				default:
					titleRes = R.string.group_tab_nearby;
					break;
			}
			return getActivity().getString(titleRes);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		private class GroupBoldableCallback implements BoldableGroupFragment.BoldableCallback{

			@Override
			public void onUpdate() {
				GroupsTabsAdapter.this.onUpdate();
			}
		}

		public void onUpdate() {
			drawBoldTitle(MY_GROUPS, mOwnedNewItems);
			drawBoldTitle(SUBSCRIBED, mSubscribedNewItems);
		}

	}

	public void notifyGroupSuscribed() {
		switch (mTabLayout.getSelectedTabPosition()) {
			case GroupsTabsAdapter.NEARBY:
				((Subscribable) mTabAdapter.getItem(GroupsTabsAdapter.SUBSCRIBED)).notifyGroupSuscribed();
				break;
			case GroupsTabsAdapter.SUBSCRIBED:
				((Subscribable) mTabAdapter.getItem(GroupsTabsAdapter.NEARBY)).notifyGroupSuscribed();
				break;
			default:
				break;
		}
	}

	public static class GroupNearestFragment extends BaseGroupListFragment implements Subscribable, FragmentLifeCycle {

		private Boolean mFromItem = false;

		@Override
		public void onResume() {
			super.onResume();
			if (!mIsLoading && !mFromItem) {
				clearData();
				getDataFromServer();
			} else {
				if (mFromItem) {
					getDataFromServer();
				}
			}
		}

		@Override
		protected void getDataFromServer() {
			showProgress();
			Location location = LocationUtil.getLocation(getBaseActivity());
			if (location != null) {
				new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationResolverCallback() {
					@Override
					public void onLocationResolved(Location location) {
						startRequest(location);
					}
				}).execute();
			} else {
				startRequest(location);
			}
		}

		@Override
		public Activity getActivityAttached() {
			return getActivity();
		}

		@Override
		public void onBackPressed() {
		}

		private class Filters {
			String mMaxDistance = "20";
			String mOrderBy = "nearest";
		}

		private void startRequest(Location location) {
			Filters filter = new Filters();
			GroupSearchRequest.Builder builder = new GroupSearchRequest.Builder().page(page.longValue())
					.pageSize(((Integer) PAGE_SIZE).longValue()).maxDistance(filter.mMaxDistance).order(filter.mOrderBy);

			if (location != null) {
				builder.withPos(location.getLatitude(), location.getLongitude());
			}

			mSpiceManager.execute(builder.build(), new GroupListListener());
		}

		@Override
		protected void onItemClicked(GroupModel item) {
			if (item != null){
				if (item.getId() >= 0){
					super.onItemClicked(item);
					mFromItem = true;
					ScreenManager.showGroupDetailActivity(getBaseActivity(), item.getId());
				}
			}
		}

		@Override
		protected void onGroupSuscribeClicked(GroupModel aGroup) {
			try {
				((ParkActivity) getActivity()).notifyGroupSuscribed();
			} catch (NullPointerException e) {
			} catch (ClassCastException e) {
			}
		}

		private class GroupListListener extends ListListener {
		}

		@Override
		public void notifyGroupSuscribed() {
			onRefresh();
		}

		@Override
		public void onResumeFragment() {
			onRefresh();
		}

	}

	public static class GroupOwnedFragment extends BoldableGroupFragment implements Subscribable, FragmentLifeCycle {

		private Filters filters = new Filters();

		@Override
		protected boolean isActionEnabled() {
			return false;
		}

		@Override
		protected void onItemClicked(GroupModel item) {
			if (item != null){
				if (item.getId() >= 0){
					super.onItemClicked(item);
					ScreenManager.showGroupDetailActivity(getBaseActivity(), item.getId());
				}
			}
		}

		@Override
		public void getDataFromServer() {

			if (PreferencesUtil.getParkToken(getActivity()) != null) {
				showProgress();

				BaseParkSessionRequest<GroupListResponse> aRequest;
				GroupListRequest.Builder builder = new GroupListRequest.Builder().order(filters.mOrderBy)
						.onlyOwned(true).page(page.longValue()).pageSize(((Integer) PAGE_SIZE).longValue());

				aRequest = builder.build(ParkApplication.getInstance().getUsername());

				mSpiceManager.execute(aRequest, new GroupListListener());
			} else {
				GroupListResponse rta = new GroupListResponse();
				rta.setNoResultsMessage(getString(R.string.unlogged_my_groups));
				rta.setNoResultsHint(getString(R.string.unlogged_subs_or_my_groups_hint));
				rta.setGroups(new ArrayList<GroupModel>());
				loadData(rta);
			}
		}

		@Override
		public Activity getActivityAttached() {
			return getActivity();
		}

		@Override
		public void onBackPressed() {
		}

		private class GroupListListener extends ListListener {
		}

		private class Filters {
			String mOrderBy = "name";
		}

		@Override
		public void notifyGroupSuscribed() {
			onRefresh();
		}

		@Override
		public void onResumeFragment() {
			onRefresh();
		}

	}

	private class VerifiedListener extends BaseNeckRequestListener<ProfileModel> {

		@Override
		public void onRequestError(Error error) {
		}

		@Override
		public void onRequestSuccessfull(ProfileModel profile) {
			if (!profile.isVerified() && !profile.isMobileVerified()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
				builder.setTitle(R.string.warning);
				builder.setMessage(getString(R.string.account_not_verified_message));
				builder.setPositiveButton(R.string.account_not_verified_positive,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								showProgress();
								mSpiceManager.execute(new SendVerificationMailRequest(),
										new SendVerificationMailListener());
							}
						});
				builder.setNegativeButton(R.string.account_not_verified_negative,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				PreferencesUtil.setShownGroupAccountVerificationMessage(getActivity(), true);
			}

		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

	}

	private class UnreadItemsGroupsListener extends BaseNeckRequestListener<UnreadItemsGroupsModel>{

		@Override
		public void onRequestError(Error error) {

		}

		@Override
		public void onRequestSuccessfull(UnreadItemsGroupsModel unreadItemsGroupsModel) {
			mOwnedNewItems = (unreadItemsGroupsModel.getOwnedGroupsItems()>0) ? true : false;
			mSubscribedNewItems = (unreadItemsGroupsModel.getSubscribedGroupItems()>0) ? true : false;
			mTabAdapter.onUpdate();
		}

		@Override
		public void onRequestException(SpiceException exception) {

		}
	}

	private class SendVerificationMailListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
			SwrveSDK.event(SwrveEvents.USER_ACTIVATION_FAIL, payload);
			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean t) {
			if (t) {
				MessageUtil.showSuccess((BaseActivity) getActivity(),
						getResources().getString(R.string.send_verification_email_success),
						getBaseActivity().getCroutonsHolder());
			}
			SwrveSDK.event(SwrveEvents.USER_ACTIVATION_SUCCESS);
			hideProgress();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
			SwrveSDK.event(SwrveEvents.USER_ACTIVATION_FAIL, payload);
			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}
	}

	private class GroupPageChangeListener extends ParkPageChangeListener{

		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			if (mTabAdapter !=null && mViewPager !=null){
				Activity activityAttached = ((FragmentLifeCycle) mTabAdapter.getItem(position)).getActivityAttached();
				if (activityAttached!=null){
					FragmentLifeCycle fragmentToShow = (FragmentLifeCycle) mTabAdapter.getItem(position);
					fragmentToShow.onResumeFragment();
				} else {
					ScreenManager.showCategoryItemsFragmentFromDrawer(getBaseActivity());
				}
			}
		}
	}

}
