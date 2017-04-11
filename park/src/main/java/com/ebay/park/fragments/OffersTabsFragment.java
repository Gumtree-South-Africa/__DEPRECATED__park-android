package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.ebay.park.R;
import com.ebay.park.base.BaseTabFragment;

public class OffersTabsFragment extends BaseTabFragment {
	
	public static final String TEST_TAG = OffersTabsFragment.class.getSimpleName();
	private static final int OFFERS_SALES = 0;
	private static final int PURCHASE_OFFERS = 1;
	private TabAdapter mTabAdapter;

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setTitle(R.string.negotiations);
		mTabAdapter = new ListsTabAdapter(getChildFragmentManager());
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
	}

	@Override
	public void onBackPressed() {
	}

	private class ListsTabAdapter extends TabAdapter {

		public ListsTabAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int pos) {
			Fragment aFragment = null;
			switch (pos) {
			case PURCHASE_OFFERS:
				aFragment = ConversationListFragment.conversationsAsBuyer();
				break;
			case OFFERS_SALES:
				aFragment = ConversationListFragment.conversationsAsSeller();
				break;
			default:
				getActivity().onBackPressed();
				break;
			}
			return aFragment;
		}

		@Override
		public int getCount() {
			return PURCHASE_OFFERS + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = "";
			switch (position) {
			case PURCHASE_OFFERS:
				title = getString(R.string.purchase_offers_title);
				break;
			case OFFERS_SALES:
				title = getString(R.string.offers_sales_title);
				break;
			}
			return title;
		}

	}

	@Override
	public void onRefresh() {
	}

}
