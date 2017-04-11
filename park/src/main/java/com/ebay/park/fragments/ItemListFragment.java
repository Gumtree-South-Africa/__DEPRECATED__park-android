package com.ebay.park.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseItemListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.model.CategoryModel;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.ItemsListParamsModel;
import com.ebay.park.model.OwnerModel;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * Shows item list.
 *
 * @author federico.perez
 */
public class ItemListFragment extends BaseItemListFragment implements SearchableFragment {

    public static final String TAG = "ItemListFragment";
    private static final String GROUP_ID_PARAM = "group_id";
    private static final String USERNAME_PARAM = "user_id";
    private static final long NO_ID = -123L;
    private long mGroupId = NO_ID;
    public static long sCategoryId;
    private String username;

    private static final String CATEGORY_ARG = "category";
    private static final String CATEGORY_NAME_ARG = "category_name";
    private static final String CATEGORY_COLOR_ARG = "category_color";

    private String mQuery;
    private Filters mFilters;
    private MenuItem mSearchItem;

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilters != null) {
            outState.putString("filters", new Gson().toJson(mFilters));
        }
    }

    public static ItemListFragment forCategory(CategoryModel aCategory) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ARG, aCategory.getId());
        args.putString(CATEGORY_NAME_ARG, aCategory.getName());
        args.putString(CATEGORY_COLOR_ARG, aCategory.getColor());
        fragment.setArguments(args);
        return fragment;
    }

    public static ItemListFragment forGroup(long groupId) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putLong(GROUP_ID_PARAM, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ItemListFragment forUser(String username) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME_PARAM, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null && getArguments().containsKey(GROUP_ID_PARAM)) {
            mGroupId = getArguments().getLong(GROUP_ID_PARAM, NO_ID);
        }
        if (getArguments() != null && getArguments().containsKey(USERNAME_PARAM)) {
            username = getArguments().getString(USERNAME_PARAM);
        }
        mFilters = new Filters();

        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("filters");
            if (json != null) {
                mFilters = new Gson().fromJson(json, Filters.class);
            }
        }

        if (getArguments() != null && getArguments().containsKey(CATEGORY_ARG)) {

            if (getArguments().getLong(CATEGORY_ARG) != 11) {
                mFilters.mCategory = String.valueOf(getArguments().getLong(CATEGORY_ARG));
            }
        }
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        if (getArguments() != null) {
            String categoryColor = null;
            String categoryName = null;
            if (getArguments().containsKey(CATEGORY_NAME_ARG)) {
                categoryName = getArguments().getString(CATEGORY_NAME_ARG);
            }
            if (getArguments().containsKey(CATEGORY_COLOR_ARG)) {
                categoryColor = getArguments().getString(CATEGORY_COLOR_ARG);
            }
            if (getArguments().containsKey(CATEGORY_ARG) || categoryName != null || categoryColor != null) {
                mCategoryName.setVisibility(View.GONE);
            } else {
                mCategoryName.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(categoryName)) {
                mCategoryName.setText(categoryName);
            }
            if (!TextUtils.isEmpty(categoryColor)) {
                mCategoryName.setBackgroundColor(Integer.parseInt(categoryColor, 16) + 0xFF000000);
            }
        } else {
            mCategoryName.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null && getArguments().containsKey(CATEGORY_ARG)) {
            sCategoryId = getArguments().getLong(CATEGORY_ARG);
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
            this.mFilters.mMaxDistance = filters.get(FilterableActivity.MAX_DISTANCE);
        }
    }

    @Override
    public void searchQuery(String query) {
        this.mQuery = query;
        if (mSpiceManager.isStarted()) {
            showProgress();
            clearData();
            getDataFromServer();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSearchItem = menu.findItem(R.id.action_search);
    }

    @Override
    public void onRefresh() {
        if (!GlobalSearchFragment.isSearching(mSearchItem)) {
            mQuery = null;
        }
        super.onRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                ScreenManager.showGroupItemsEditActivity(getBaseActivity(), mGroupId);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgress();
        mSyncProgressBar.setRefreshing(false);
        mSyncProgressBar.destroyDrawingCache();
        mSyncProgressBar.clearAnimation();
    }

    @Override
    public void onBackPressed() {
    }

    @SuppressWarnings("unused")
    private class Filters {
        String mPriceFrom;
        String mPriceTo;
        String mCategory;
        String mUserId;
        String mMaxDistance;
        String mOrderBy = "published";

        private Filters() {
            if (mGroupId == NO_ID && TextUtils.isEmpty(username)) {
                mMaxDistance = "20";
            }
        }
    }

    @Override
    protected void onItemClicked(ItemModel aItem, int pos) {
        if (aItem != null) {
            if (aItem.getId() != null) {
                hideFlagNewItem(aItem);
                ParkApplication.sItemTapped = aItem;

                ItemsListParamsModel itemListParams = new ItemsListParamsModel();
                itemListParams.setCategoryId(mFilters.mCategory);
                itemListParams.setPage(page);
                itemListParams.setCurrentPos(pos);

                ArrayList<Long> itemIds = null;

                if (getArguments() != null && getArguments().containsKey(CATEGORY_ARG)) {
                    itemIds = getItemIdsFromItemList();
                }

                ScreenManager.showItemDetailActivity(getBaseActivity(), aItem.getId(), itemListParams, itemIds);
            }
        }
    }

    private ArrayList<Long> getItemIdsFromItemList() {
        ArrayList<Long> ids = new ArrayList<>();
        for (ItemModel item : mItemAdapter.getItems()){
            ids.add(item.getId());
        }
        return ids;
    }

    @Override
    protected void onUsenameClicked(OwnerModel aUser) {
        if (aUser != null) {
            if (aUser.getUsername() != null) {
                ScreenManager.showProfileActivity(getBaseActivity(), aUser.getUsername());
            }
        }
    }

    protected void getDataFromServer() {
        showProgress();
        Location location = LocationUtil.getLocation(getBaseActivity());

        if (location != null) {
            new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationResolverCallback() {
                @Override
                public void onLocationResolved(Location location) {
                    startItemsRequest(location);
                }
            }).execute();
        } else {
            startItemsRequest(location);
        }
    }

    private void startItemsRequest(Location location) {
        // @formatter:off
        ItemListRequest.Builder builder = new ItemListRequest.Builder().query(mQuery).page(page.longValue())
                .pageSize(((Integer) PAGE_SIZE).longValue()).categoryId(mFilters.mCategory).priceFrom(mFilters.mPriceFrom)
                .priceTo(mFilters.mPriceTo).order(mFilters.mOrderBy).forGroup(mGroupId).forUser(username)
                .maxDistance(mFilters.mMaxDistance);

        if (location != null) {
            builder.withPos(location.getLatitude(), location.getLongitude());
        }
        if (mGroupId > 0) {
            builder.tagNewItem();
        }
        // @formatter:on
        mSpiceManager.execute(builder.build(), new ItemListListener());
    }

    private class ItemListListener extends ListListener {
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