package com.ebay.park.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseUserListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.FollowerModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.DiscoverUsersRequest;
import com.ebay.park.requests.SearchUserRequest;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.ebay.park.utils.SwrveEvents;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

public class UserSearchFragment extends BaseUserListFragment implements SearchableFragment, Tabeable, FragmentLifeCycle {

	private static final String ID_PARAM = "id";
	private static final long NO_ID = -123L;
	private Long mGroupId;
	private Filters mFilters;
	private String mQuery = "";
	private View mRecommendedLabel;
	private MenuItem mSearchItem;
	private boolean mIsFilterShowing = false;
	private ViewGroup mRecommendedContainer;
	private static Boolean sShowRecommended = false;

	public static UserSearchFragment withRecommendedLayout() {
		UserSearchFragment fragment = new UserSearchFragment();
		sShowRecommended = true;
		return fragment;
	}

	public static UserSearchFragment forGroup(long id) {
		UserSearchFragment fragment = new UserSearchFragment();
		Bundle args = new Bundle();
		args.putLong(ID_PARAM, id);
		fragment.setArguments(args);
		sShowRecommended = false;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(ID_PARAM)) {
			mGroupId = getArguments().getLong(ID_PARAM, NO_ID);
		}
		mFilters = new Filters();
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onViewCreated(rootView.findViewById(R.id.list_container), savedInstanceState);				
	}
		
	@Override
	protected void loadHeaderView() {
		if (sShowRecommended){
			mRecommendedLabel = View.inflate(getBaseActivity(), R.layout.recommended_label, null);
			mRecommendedContainer = (ViewGroup) View.inflate(getBaseActivity(), R.layout.recommend_container, null);
			mUserList.addHeaderView(mRecommendedContainer);
			updateRecommendedLabel(isShowingRecommended());
		}
	}

	private void updateRecommendedLabel(boolean isShowing) {
		if (isShowing){
			mRecommendedContainer.removeAllViews();
			mRecommendedContainer.addView(mRecommendedLabel);
		}else{
			mRecommendedContainer.removeAllViews();
		}
	}

	private boolean isShowingRecommended() {
		return TextUtils.isEmpty(mQuery) && !mIsFilterShowing;
	}

	public void searchQuery(String aQuery) {
		Map<String, String> swrveAttributes = new HashMap<String, String>();
		swrveAttributes.put(SwrveEvents.FILTERED_SELLER, aQuery);
		SwrveSDK.userUpdate(swrveAttributes);
		clearData();
		mQuery = aQuery;
		getDataFromServer();
		updateRecommendedLabel(false);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		mSearchItem = menu.findItem(R.id.action_search);
		SearchView searchView = ((SearchView) mSearchItem.getActionView());
		if (!TextUtils.isEmpty(mQuery)){			
        	searchView.setQuery(mQuery, false);
		}
	}
	
	@Override
	public boolean fragmentIsLoading() {
		return mIsLoading;
	}

	@Override
	public Activity getActivityAttached() {
		return getActivity();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.edit:
				ScreenManager.showGroupFollowersEditActivity(getBaseActivity(), mGroupId);
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mIsLoading && getArguments() != null) {
			clearData();
			getDataFromServer();
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
					startFollowersRequest(location);
				}
			}).execute();
		} else {
			startFollowersRequest(location);
		}
	}

	private void startFollowersRequest(Location location) {
		if (!ParkApplication.sResetPageFromItemFilter && ParkApplication.sResetPageFromUserFilter
				&& !ParkApplication.sResetPageFromGroupFilter){
			clearData();
			ParkApplication.sResetPageFromUserFilter = false;
			KeyboardHelper.hide(getBaseActivity(),getView());
		}
		BaseParkSessionRequest<UserListResponse> request;
		if (isShowingRecommended() && mGroupId == null){
			if (location == null) {
				request = new DiscoverUsersRequest(ParkApplication.getInstance().getUsername());
			} else {
				request = new DiscoverUsersRequest(ParkApplication.getInstance().getUsername(),
						location.getLatitude(), location.getLongitude());
			}
		}else{
			SearchUserRequest.Builder builder;
			if (isShowingRecommended() && mGroupId != null){
				builder = new SearchUserRequest.Builder(ParkApplication.getInstance().getUsername(), mGroupId);
			}else{
				if (!TextUtils.isEmpty(mQuery)){
					builder = new SearchUserRequest.Builder(ParkApplication.getInstance().getUsername(), mQuery);	
				}else{
					builder = new SearchUserRequest.Builder(ParkApplication.getInstance().getUsername());					
				}				
				if (mGroupId != null) {
					builder.forGroup(mGroupId);
				}
				if (!TextUtils.isEmpty(mFilters.mOrderBy)) {
					builder.orderBy(mFilters.mOrderBy);
				}
				if (!TextUtils.isEmpty(mFilters.mRadius)) {
					builder.withRadius(mFilters.mRadius);
				}
			}
			builder.page(page);
			builder.pageSize(PAGE_SIZE);
			if (location != null) {
				builder.withLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
			}
			request = builder.build();
		}
		mSpiceManager.execute(request, new UserListener());
	}

	@Override
	public void onBackPressed() {
	}

	private class UserListener extends ListListener {
		@Override
		public void onRequestError(BaseNeckRequestException.Error error) {
			super.onRequestError(error);
			if (!TextUtils.isEmpty(mQuery)) {
				Map<String, String> payload = new HashMap<>();
				payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
				SwrveSDK.event(SwrveEvents.SAVE_SEARCH_FAIL, payload);
			}
		}

		@Override
		public void onRequestSuccessfull(UserListResponse aResponse) {
			super.onRequestSuccessfull(aResponse);
			if (!TextUtils.isEmpty(mQuery)) {
				SwrveSDK.event(SwrveEvents.SAVE_SEARCH_SUCCESS);
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			super.onRequestException(exception);
			if (!TextUtils.isEmpty(mQuery)) {
				Map<String, String> payload = new HashMap<>();
				payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
				SwrveSDK.event(SwrveEvents.SAVE_SEARCH_FAIL, payload);
			}
		}
	}

	@Override
	public void onFilterSelected(Map<String, String> selectedFilters, String query) {
		mFilters = new Filters();
		if (selectedFilters.containsKey(FilterableActivity.MAX_DISTANCE)) {
			mFilters.mRadius = selectedFilters.get(FilterableActivity.MAX_DISTANCE);
		}
		if (selectedFilters.containsKey(FilterableActivity.ORDER_BY)) {
			mFilters.mOrderBy = selectedFilters.get(FilterableActivity.ORDER_BY);
		}
		if (!TextUtils.isEmpty(query)){
			mQuery = query;
		}
		mIsFilterShowing = true;
		updateRecommendedLabel(false);
		getDataFromServer();
	}

	@Override
	protected void onUserClicked(FollowerModel aUser) {
		if (!mIsLoading && aUser!=null) {
			if (aUser.getUsername()!=null){
				ScreenManager.showProfileActivity(getBaseActivity(), aUser.getUsername());
			}
		}
	}

	@Override
	protected void onUsernameClick(String username) {
		if (!mIsLoading && username!=null) {
			ScreenManager.showProfileActivity(getBaseActivity(), username);
		}
	}

	private class Filters {
		String mRadius;
		String mOrderBy = "name";

		private Filters() {
			if (mGroupId == null) {
				mRadius = "50";
			}
		}
	}

	@Override
	public void onTabSelected() {
		if (sShowRecommended) {
			clearData();
			updateRecommendedLabel(true);
			mQuery = "";
			mIsFilterShowing = false;
			mFilters = new Filters();
			ParkApplication.sSelectedUserFilters.clear();
			getDataFromServer();
			
		}
	}

	@Override
	public void onResumeFragment() {
		onRefresh();
	}

}
