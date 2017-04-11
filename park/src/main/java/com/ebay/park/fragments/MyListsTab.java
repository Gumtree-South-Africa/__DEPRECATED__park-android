package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;

import com.ebay.park.R;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.fragments.MyLists.Whishlist;

public class MyListsTab extends BaseTabFragment implements Whishlist {
	
	public static final String TEST_TAG = MyListsTab.class.getSimpleName();
	public static final String TAG = "TagMyLists";
	private TabAdapter mTabAdapter;
	
	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		setTitle(R.string.my_lists_title);
		mTabAdapter = new ListsTabAdapter(getChildFragmentManager());
		super.onViewCreated(rootView, savedInstanceState);
		showPostAdButton();
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
	
	public void notifyLiked() {
		switch (mViewPager.getCurrentItem()) {
		case ListsTabAdapter.ITEMS_ON_MY_WISHLIST:		
			((Whishlist) mTabAdapter.getItem(ListsTabAdapter.ITEMS_PEOPLE_I_FOLLOW)).notifyLiked();
			break;
		case ListsTabAdapter.ITEMS_PEOPLE_I_FOLLOW:
			((Whishlist) mTabAdapter.getItem(ListsTabAdapter.ITEMS_ON_MY_WISHLIST)).notifyLiked();
			break;
		default:
			break;
		}
	}

	@Override
	protected int getOffsetLimit() {
		return 2;
	}

	@Override
	public void onBackPressed() {
	}

	private class ListsTabAdapter extends TabAdapter{
		
		public static final int ITEMS_ON_MY_WISHLIST = 0;
		public static final int ITEMS_PEOPLE_I_FOLLOW = 1;
		public static final int ITEMS_SUBSCRIBED_GROUPS = 2;
		
		private SparseArray<Fragment> mFragments;

		public ListsTabAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
			mFragments = new SparseArray<Fragment>();
			mFragments.put(ITEMS_ON_MY_WISHLIST, new MyLists.MyItemWhishlist());
			mFragments.put(ITEMS_PEOPLE_I_FOLLOW, new MyLists.MyFavoritePeopleItemList());
			mFragments.put(ITEMS_SUBSCRIBED_GROUPS, new MyLists.MyGroupsItemList());
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int titleRes;
			switch (position) {
			case ITEMS_ON_MY_WISHLIST:
				titleRes = R.string.my_lists_spinner_op1;
				break;
			case ITEMS_PEOPLE_I_FOLLOW:
				titleRes = R.string.my_lists_spinner_op2;
				break;				
			case ITEMS_SUBSCRIBED_GROUPS:				
			default:
				titleRes = R.string.my_lists_spinner_op3;
				break;
			}
			return getActivity().getString(titleRes);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}			
		
	}

	@Override
	public void onRefresh() {
	}

}
