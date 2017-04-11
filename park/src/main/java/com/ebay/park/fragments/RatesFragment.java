package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseTabFragment;

public class RatesFragment extends BaseTabFragment {
	
	private static final String USERNAME_ARG = "username_arg";
	private static final String PENDING_FEED_ARG = "pending_feed_arg";
	
	private String mUsername;
	private TabAdapter mTabAdapter;
	
	public static RatesFragment forUser(String username, boolean comesFromPendingFeed) {
		RatesFragment fragment = new RatesFragment();
		Bundle aBundle = new Bundle();
		if (comesFromPendingFeed){
			aBundle.putBoolean(PENDING_FEED_ARG, true);
		}
		aBundle.putString(USERNAME_ARG, username);
		fragment.setArguments(aBundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(USERNAME_ARG)){
			mUsername = getArguments().getString(USERNAME_ARG);
		}
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		mTabAdapter = new RateTabAdapter(getChildFragmentManager());
		setHasOptionsMenu(true);
		super.onViewCreated(rootView, savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(PENDING_FEED_ARG)){
			mViewPager.setCurrentItem(RateTabAdapter.PENDING);
		}
		if (mTabAdapter.getCount()==2){
			setUpCenteredTabs();
		}
	}

	@Override
	protected TabAdapter getAdapter() {
		return mTabAdapter;
	}

	@Override
	protected void releaseResources() {
		mTabAdapter = null;
	}


	private boolean checkingMyOwnRatings() {
		return mUsername.equals(ParkApplication.getInstance().getUsername());
	}

	@Override
	protected int getOffsetLimit() {
		return RateTabAdapter.PENDING + 1;
	}

	@Override
	public void onBackPressed() {
	}

	private class RateTabAdapter extends TabAdapter {

		private static final int BUYER = 0;
		private static final int SELLER = 1;	
		private static final int PENDING = 2;

		public RateTabAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			switch (pos) {
			case SELLER:
				return RateListFragment.getSellerRates(mUsername);
			case BUYER:
				return RateListFragment.getBuyerRates(mUsername);
			case PENDING:
				return new PendingRatesFragment();
			default:
				return new Fragment();
			}
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			String title = "";
			switch (position) {
			case SELLER:
				title = getString(R.string.tab_rate_seller);
				break;
			case BUYER:
				title = getString(R.string.tab_rate_buyer);
				break;
			case PENDING:
				title = getString(R.string.tab_rate_pending);
				break;
			}
			return title;
		}

		@Override
		public int getCount() {
			if (checkingMyOwnRatings()){
				return PENDING + 1;	
			}else{
				return SELLER + 1;
			}
			
		}

	}

}
