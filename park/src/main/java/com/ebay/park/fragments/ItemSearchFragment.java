package com.ebay.park.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseItemListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.OwnerModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.requests.ItemsRecommendedRequest;
import com.ebay.park.responses.ItemListResponse;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.ebay.park.utils.SwrveEvents;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

public class ItemSearchFragment extends BaseItemListFragment implements SearchableFragment, Tabeable, FragmentLifeCycle {

	private View mRecommendedLabel;
	private ViewGroup mRecommendContainer;
	private String mQuery = "";
	private Filters mFilters = new Filters();
	private MenuItem mSearchItem;
	private boolean mIsFilterShowing = false;

	@Override
	protected void onItemClicked(ItemModel aItem, int pos) {
		if (aItem != null) {
			if (aItem.getId() != null){
				ParkApplication.sItemTapped = aItem;
				ScreenManager.showItemDetailActivity(getBaseActivity(), aItem.getId(),
						null, null);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_search_recommended,
				getContainerLayout(inflater, container, savedInstanceState), true);
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView.findViewById(R.id.list_container), savedInstanceState);
	}

	@Override
	protected void loadHeaderView() {
		mRecommendContainer = (ViewGroup) View.inflate(getBaseActivity(), R.layout.recommend_container, null);
		mRecommendedLabel = View.inflate(getBaseActivity(), R.layout.recommended_label, null);
		mRecommendContainer.addView(mRecommendedLabel);
		updateRecommendedLabel(isShowingRecommended());
	}

	private void updateRecommendedLabel(boolean isShowing) {
		if (isShowing) {
			mItemGrid.removeHeaderView(mRecommendContainer);
			mItemGrid.addHeaderView(mRecommendContainer);
		} else {
			mItemGrid.removeHeaderView(mRecommendContainer);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (ParkApplication.sJustLoggedThirdLevel) {
			navigationFlow();
		}
	}

	private void navigationFlow() {
		ParkApplication.sJustLoggedThirdLevel = false;

		if (ParkApplication.sFgmtOrAct_toGo != null) {
			switch (ParkApplication.sFgmtOrAct_toGo) {
				case POST_AD:
					ParkApplication
							.getInstance()
							.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
							.send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
									.setAction("Clicks on Publish").setLabel("The user entered to publish an item").build());
					ScreenManager.showCameraActivity((BaseActivity) getActivity());
					break;
				default:
					break;
			}
			ParkApplication.sFgmtOrAct_toGo = null;
		}
	}

	private boolean isShowingRecommended() {
		return TextUtils.isEmpty(mQuery) && !mIsFilterShowing;
	}

	@Override
	protected void getDataFromServer() {
		showProgress();
		Location location = LocationUtil.getLocation(getBaseActivity());
		if (location != null) {
			new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationResolverCallback() {
				@Override
				public void onLocationResolved(Location location) {
					startItemsSearchRequest(location);
				}
			}).execute();
		} else {
			startItemsSearchRequest(location);
		}
	}

	private void startItemsSearchRequest(Location location) {

		BaseParkSessionRequest<ItemListResponse> aRequest;
		Double mLatitude = null;
		Double mLongitude = null;
		if (location != null) {
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
		}
		if (ParkApplication.sResetPageFromItemFilter && !ParkApplication.sResetPageFromUserFilter
				&& !ParkApplication.sResetPageFromGroupFilter) {
			clearData();
			ParkApplication.sResetPageFromItemFilter = false;
			KeyboardHelper.hide(getBaseActivity(),getView());
		}

		if (isShowingRecommended()) {
			clearData();
			aRequest = new ItemsRecommendedRequest(mLatitude, mLongitude);
		} else {
			ItemListRequest.Builder builder;
			if (!TextUtils.isEmpty(mQuery)) {
				builder = new ItemListRequest.Builder().query(mQuery);
			} else {
				builder = new ItemListRequest.Builder();
			}
			builder = builder.page(page.longValue()).pageSize(((Integer) PAGE_SIZE).longValue());

			// Filters:
			builder.categoryId(mFilters.mCategory).priceFrom(mFilters.mPriceFrom).priceTo(mFilters.mPriceTo)
					.order(mFilters.mOrderBy).maxDistance(mFilters.mMaxDistance);

			if (location != null) {
				builder.withPos(mLatitude, mLongitude);
			}
			aRequest = builder.build();
		}
		mSpiceManager.execute(aRequest, new ItemListListener());
	}

	@Override
	public void onBackPressed() {
	}

	private class ItemListListener extends ListListener {
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
		public void onRequestSuccessfull(ItemListResponse aResponse) {
			super.onRequestSuccessfull(aResponse);
			if (!TextUtils.isEmpty(mQuery)) {
				SwrveSDK.event(SwrveEvents.SAVE_SEARCH_SUCCESS);
			}
			if (aResponse.getItems().isEmpty()){
				updateRecommendedLabel(false);
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
		loadPreviousSearch();
	}

	@Override
	public boolean fragmentIsLoading() {
		return mIsLoading;
	}

	@Override
	public Activity getActivityAttached() {
		return getActivity();
	}

	private void loadPreviousSearch() {
		if (mSearchItem != null) {
			SearchView searchView = ((SearchView) mSearchItem.getActionView());
			if (!TextUtils.isEmpty(mQuery)) {
				searchView.setQuery(mQuery, false);
			}
		}
	}

	@Override
	public void onFilterSelected(Map<String, String> filters, String query) {
		this.mFilters = new Filters();
		if (filters.containsKey(FilterableActivity.CATEGORY)) {
			this.mFilters.mCategory = filters.get(FilterableActivity.CATEGORY);
		}
		if (filters.containsKey(FilterableActivity.PRICE_FROM)) {
			this.mFilters.mPriceFrom = filters.get(FilterableActivity.PRICE_FROM);
		}
		if (filters.containsKey(FilterableActivity.PRICE_TO)) {
			this.mFilters.mPriceTo = filters.get(FilterableActivity.PRICE_TO);
		}
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
		mIsFilterShowing = true;
		if (mRecommendedLabel !=null){
			mRecommendedLabel.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("unused")
	private class Filters {
		String mPriceFrom;
		String mPriceTo;
		String mCategory;
		String mUserId;
		String mMaxDistance = "20";
		String mOrderBy = "published";
	}

	@Override
	public void onTabSelected() {
		clearData();
		updateRecommendedLabel(true);
		mQuery = "";
		ParkApplication.sSelectedItemFilters.clear();
		ParkApplication.sSelectedUserFilters.clear();
		ParkApplication.sSelectedGroupFilters.clear();
		mIsFilterShowing = false;
		mFilters = new Filters();
		getDataFromServer();
	}

	@Override
	protected void onUsenameClicked(OwnerModel aUser) {
		if (aUser!=null){
			if (aUser.getUsername()!=null){
				ScreenManager.showProfileActivity(getBaseActivity(), aUser.getUsername());
			}
		}
	}

	@Override
	public void onResumeFragment() {
		onRefresh();
	}

}
