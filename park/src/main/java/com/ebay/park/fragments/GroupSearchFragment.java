package com.ebay.park.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.GlobalSearchActivity;
import com.ebay.park.base.BaseGroupListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.GroupModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupSearchRequest;
import com.ebay.park.requests.GroupsRecommendedRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.ebay.park.utils.SwrveEvents;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * Groups list screen.
 * 
 * @author Nicol�s Mat�as Fern�ndez
 * 
 */
public class GroupSearchFragment extends BaseGroupListFragment implements SearchableFragment, Tabeable, FragmentLifeCycle {

	private String mQuery = "";
	private Filters mFilters = new Filters();
	private View mRecommendedLabel;
	private MenuItem mSearchItem;
	private boolean mIsFilterSet = false;
	private ViewGroup mRecommendedContainer;

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onViewCreated(rootView, savedInstanceState);
		if (getActivity() instanceof GlobalSearchActivity) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			params.setMargins(0, 0, 0, 0);
			mGroupList.setLayoutParams(params);
		}
	}

	@Override
	protected void loadHeaderView() {
		mRecommendedLabel = View.inflate(getBaseActivity(), R.layout.recommended_label, null);
		mRecommendedContainer = (ViewGroup) View.inflate(getBaseActivity(), R.layout.recommend_container, null);
		mGroupList.addHeaderView(mRecommendedContainer);
		mGroupList.setPadding(0, 0, 0, dpToPx(6));
		updateRecommendedLabel(isShowingRecommended());
	}

	private void updateRecommendedLabel(boolean isShowing) {
		if (mRecommendedContainer != null) {
			if (isShowing) {
				mRecommendedContainer.removeAllViews();
				mRecommendedContainer.addView(mRecommendedLabel);
			} else {
				mRecommendedContainer.removeAllViews();
			}
		}
	}

	public static int dpToPx(int dp) {
		return (int) (dp * (Resources.getSystem().getDisplayMetrics().densityDpi / 160f));
	}

	private boolean isShowingRecommended() {
		return TextUtils.isEmpty(mQuery) && !mIsFilterSet;
	}

	@Override
	protected int getGroupSelectedPosition(int position) {
		return super.getGroupSelectedPosition(position) - 1;
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

	private void startRequest(Location location) {
		Double latitude = null;
		Double longitude = null;
		BaseParkSessionRequest<GroupListResponse> aRequest;
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
		if (!ParkApplication.sResetPageFromItemFilter && !ParkApplication.sResetPageFromUserFilter
				&& ParkApplication.sResetPageFromGroupFilter){
			clearData();
			ParkApplication.sResetPageFromGroupFilter = false;
			KeyboardHelper.hide(getBaseActivity(),getView());
		}
		
		if (isShowingRecommended()){
			clearData();
			aRequest = new GroupsRecommendedRequest();
		} else {
			GroupSearchRequest.Builder builder = new GroupSearchRequest.Builder();
			if (!TextUtils.isEmpty(mQuery)) {
				builder = builder.query(mQuery);
			}
			builder = builder.page(page.longValue()).pageSize(((Integer) PAGE_SIZE).longValue());

			// Filters:
			builder.order(mFilters.mOrderBy);
			builder.maxDistance(mFilters.mMaxDistance);
			if (location != null) {
				builder.withPos(latitude, longitude);
			}
			aRequest = builder.build();
		}
		mSpiceManager.execute(aRequest, new GroupListListener());
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
	public void onBackPressed() {
	}

	private class GroupListListener extends ListListener {
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
		public void onRequestSuccessfull(GroupListResponse aResponse) {
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
	public void searchQuery(String aQuery) {
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
		if (!TextUtils.isEmpty(mQuery)) {
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

	private class Filters {
		String mMaxDistance = "20";
		String mOrderBy = "nearest";
	}

	@Override
	public void onFilterSelected(Map<String, String> filters, String query) {
		this.mFilters = new Filters();
		if (filters.containsKey(FilterableActivity.ORDER_BY)) {
			this.mFilters.mOrderBy = filters.get(FilterableActivity.ORDER_BY);
		}
		if (filters.containsKey(FilterableActivity.MAX_DISTANCE)) {
			// hasLocation = true;
			this.mFilters.mMaxDistance = filters.get(FilterableActivity.MAX_DISTANCE);
		}
		if (!TextUtils.isEmpty(query)){
			mQuery = query;
		}
		mIsFilterSet = true;
		updateRecommendedLabel(false);
		getDataFromServer();
	}

	@Override
	public void onTabSelected() {
		clearData();
		updateRecommendedLabel(true);
		mQuery = "";
		mIsFilterSet = false;
		mFilters = new Filters();
		getDataFromServer();
		ParkApplication.sSelectedItemFilters.clear();
		ParkApplication.sSelectedUserFilters.clear();
		ParkApplication.sSelectedGroupFilters.clear();
	}

	@Override
	public void onResumeFragment() {
		onRefresh();
	}

}
