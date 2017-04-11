package com.ebay.park.fragments;

import android.app.Activity;
import android.location.Location;
import android.text.TextUtils;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.interfaces.Subscribable;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.GroupModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupListRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.ebay.park.utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.Map;

public class GroupSubscribedFragment extends BoldableGroupFragment implements SearchableFragment, Tabeable,
		Subscribable, FragmentLifeCycle {

	private String mQuery;
	private Filters mFilters = new Filters();

	@Override
	public void onResume() {
		super.onResume();
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

		} else {
			GroupListResponse rta = new GroupListResponse();
			rta.setNoResultsMessage(getString(R.string.unlogged_subscribed_groups));
			rta.setNoResultsHint(getString(R.string.unlogged_subs_or_my_groups_hint));
			rta.setGroups(new ArrayList<GroupModel>());
			loadData(rta);
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

		GroupListRequest.Builder builder = new GroupListRequest.Builder().query(mQuery).page(page.longValue())
				.pageSize(((Integer) PAGE_SIZE).longValue());

		// Filters:
		builder.order(mFilters.mOrderBy);

		if (location != null) {
			builder.withPos(latitude, longitude).maxDistance(mFilters.mMaxDistance);
		}
		aRequest = builder.build(ParkApplication.getInstance().getUsername());

		mSpiceManager.execute(aRequest, new GroupListListener());
	}

	@Override
	public void onBackPressed() {
	}

	private class GroupListListener extends ListListener {
	}

	@Override
	public void searchQuery(String aQuery) {
		if (!TextUtils.isEmpty(aQuery)) {
			clearData();
			mQuery = aQuery;
			getDataFromServer();
		}
	}

	private class Filters {
		String mMaxDistance;
		String mOrderBy = "name";
	}

	@Override
	protected void loadData(GroupListResponse aResponse) {
		super.loadData(aResponse);
		mQuery = null;
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
	}

	@Override
	public void onTabSelected() {
		ParkApplication.sSelectedItemFilters.clear();
		ParkApplication.sSelectedUserFilters.clear();
		ParkApplication.sSelectedGroupFilters.clear();
	}

	@Override
	public void notifyGroupSuscribed() {
		onRefresh();
	}

	@Override
	protected void onGroupSuscribeClicked(GroupModel aGroup) {
		try {
			((ParkActivity) getActivity()).notifyGroupSuscribed();
		} catch (ClassCastException e) {
		} catch (NullPointerException e) {
		}
	}

	@Override
	public void onResumeFragment() {
		onRefresh();
	}
	
	@Override
	public boolean fragmentIsLoading() {
		return mIsLoading;
	}

	@Override
	public Activity getActivityAttached() {
		return getActivity();
	}

}
