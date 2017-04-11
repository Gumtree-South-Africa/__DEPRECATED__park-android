package com.ebay.park.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.interfaces.FragmentLifeCycle;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.SwrveEvents;
import com.swrve.sdk.SwrveSDK;

import java.util.Map;

public class GlobalSearchFragment extends BaseTabFragment implements SearchableFragment {

	public static final int ITEMS_POS = 0;
	public static final int USERS_POS = 1;
	public static final int GROUP_POS = 2;
	public int mCurmCurrentTabentTab;
	private SearchView mSearchView;
	public static final String TAG = "SEARCHABLE";
	private String mSearchQuery = "";
	private TabAdapter mTabAdapter;

	@Override
	public void onStart() {
		super.onStart();
		setTitle(R.string.global_search);
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onViewCreated(rootView, savedInstanceState);
		setUpCenteredTabs();
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
		getBaseActivity().setTheme(R.style.LeftTabsTheme);
		ParkApplication.sSelectedItemFilters.clear();
		ParkApplication.sSelectedUserFilters.clear();
		ParkApplication.sSelectedGroupFilters.clear();

		SearchTabAdapter tabsAdapter = new SearchTabAdapter(getChildFragmentManager());
		tabsAdapter.addTab(SearchTabAdapter.ITEMS_POS, new ItemSearchFragment());
		tabsAdapter.addTab(SearchTabAdapter.USERS_POS, UserSearchFragment.withRecommendedLayout());
		tabsAdapter.addTab(SearchTabAdapter.GROUP_POS, new GroupSearchFragment());
		mTabAdapter = tabsAdapter;
		SwrveSDK.event(SwrveEvents.SAVE_SEARCH_BEGIN);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_global_search, menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (ParkApplication.sComesFromFilter) {
			switch (mCurmCurrentTabentTab) {
			case SearchTabAdapter.ITEMS_POS:
				((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem()))
						.onFilterSelected(ParkApplication.sSelectedItemFilters, mSearchQuery);
				break;
			case SearchTabAdapter.USERS_POS:
				((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem()))
						.onFilterSelected(ParkApplication.sSelectedUserFilters, mSearchQuery);
				break;
			case SearchTabAdapter.GROUP_POS:
				((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem()))
						.onFilterSelected(ParkApplication.sSelectedGroupFilters, mSearchQuery);
				break;
			default:
				break;
			}
			((FragmentLifeCycle) mTabAdapter.getItem(mViewPager.getCurrentItem())).onResumeFragment();
			ParkApplication.sComesFromFilter = false;
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		if (searchItem != null) {
			mSearchView = ((SearchView) searchItem.getActionView());
			mSearchView.setSearchableInfo(((SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE))
                    .getSearchableInfo(getActivity().getComponentName()));
			mSearchView.setIconifiedByDefault(false);
			mSearchView.setQueryHint(getString(R.string.search_hint));

			// Change background of the searchview
			LinearLayout searchLayout = (LinearLayout) mSearchView.findViewById(R.id.search_edit_frame);
			searchLayout.setBackgroundResource(R.drawable.rounded_background_white);

			searchItem.expandActionView();

			// Get the searchview left drawable
			ImageView leftDrawable = (ImageView) mSearchView.findViewById(R.id.search_mag_icon);
			leftDrawable.setImageDrawable(getResources().getDrawable(R.drawable.icon_search_loupe));

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.MATCH_PARENT
			);
			params.setMargins(30, 8, 0, 8);
			leftDrawable.setLayoutParams(params);

			// Get the searchview close button
			ImageView closeButton = (ImageView) mSearchView.findViewById(R.id.search_close_btn);
			closeButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_search_clear));
            closeButton.setPadding(0, 0, 35, 0);

			// Get the searchview edittext
			final EditText et = (EditText) mSearchView.findViewById(R.id.search_src_text);
			et.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
			et.setHintTextColor(getResources().getColor(R.color.feed_filter_grey));
			et.setTextColor(getResources().getColor(R.color.title_gray));
			et.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
			et.setFilters(new InputFilter[]{KeyboardHelper.EMOJI_FILTER});

			closeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					et.setText("");
					mSearchQuery = "";
					mSearchView.setQuery(mSearchQuery, false);
                    searchQuery(mSearchQuery);
					mSearchView.clearFocus();
					mSearchView.setFocusable(false);
				}
			});

			et.setOnEditorActionListener(new EditText.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
						SwrveSDK.event(SwrveEvents.SAVE_SEARCH_ATTEMPT);
                        String searchText = et.getText().toString();
                        mSearchView.setQuery(searchText, false);
                        mSearchView.clearFocus();
                        mSearchView.setFocusable(false);
                        searchQuery(searchText);
                        return true;
                    }
					return false;
				}
			});

			if (mViewPager != null) {
				updateQueryHint(mViewPager.getCurrentItem());
			}
			MenuItemCompat.setOnActionExpandListener(searchItem, new OnActionExpandListener() {

				@Override
				public boolean onMenuItemActionExpand(MenuItem arg0) {
					return true;
				}

				@Override
				public boolean onMenuItemActionCollapse(MenuItem arg0) {
					getActivity().onBackPressed();
					return false;
				}
			});
			mSearchView.clearFocus();
			mSearchView.setFocusable(false);
		}
		if (menu.findItem(R.id.action_filter) != null) {
			menu.findItem(R.id.action_filter).setVisible(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem aItem) {
		switch (aItem.getItemId()) {
		case android.R.id.home:
			getFragmentManager().popBackStack();
			break;
		case R.id.action_filter:
			if (!fragmentIsLoading()) {
				mSearchQuery = mSearchView.getQuery().toString();
				startActivity(IntentFactory.getFilterIntent(getBaseActivity(), mCurmCurrentTabentTab));
			}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(aItem);
	}

	@Override
	public void onBackPressed() {
	}

	private class SearchTabAdapter extends TabAdapter {

		private static final int ITEMS_POS = 0;
		private static final int USERS_POS = 1;
		private static final int GROUP_POS = 2;

		private final SparseArray<Fragment> mFragments;

		public SearchTabAdapter(FragmentManager fm) {
			super(fm);
			mFragments = new SparseArray<Fragment>();
		}

		public void addTab(int pos, Fragment aFragment) {
			mFragments.put(pos, aFragment);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = "";
			switch (position) {
			case ITEMS_POS:
				title = getString(R.string.tab_title_items);
				break;
			case USERS_POS:
				title = getString(R.string.tab_title_users);
				break;
			case GROUP_POS:
				title = getString(R.string.tab_title_groups);
				break;
			}
			return title;
		}

	}

	protected void updateQueryHint(int position) {
		if (mSearchView != null) {
			switch (position) {
			case SearchTabAdapter.ITEMS_POS:
				mCurmCurrentTabentTab = SearchTabAdapter.ITEMS_POS;
				break;
			case SearchTabAdapter.USERS_POS:
				mCurmCurrentTabentTab = SearchTabAdapter.USERS_POS;
				break;
			case SearchTabAdapter.GROUP_POS:
				mCurmCurrentTabentTab = SearchTabAdapter.GROUP_POS;
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected int getOffsetLimit() {
		return mTabAdapter.getCount();
	}

	@Override
	public void searchQuery(String query) {
		if (mTabAdapter != null && mViewPager != null) {
			if (getActivityAttached() != null){
				((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem())).searchQuery(query);
			} else {
				getActivity().finish();
			}
		}
	}

	@Override
	public void onFilterSelected(Map<String, String> filters, String query) {
		if (mTabAdapter != null && mViewPager != null) {
			((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem())).onFilterSelected(filters, mSearchQuery);
		}

	}

	public static boolean isSearching(MenuItem searchItem) {
		return searchItem != null && searchItem.getActionView() != null
				&& !TextUtils.isEmpty(((SearchView) searchItem.getActionView()).getQuery().toString());
	}

	@Override
	public boolean fragmentIsLoading() {
		if (mTabAdapter != null && mViewPager != null) {
			return ((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem())).fragmentIsLoading();
		}
		return false;
	}

	@Override
	public Activity getActivityAttached() {
		if (mTabAdapter != null && mViewPager != null) {
			return ((SearchableFragment) mTabAdapter.getItem(mViewPager.getCurrentItem())).getActivityAttached();
		}
		return null;
	}

}
