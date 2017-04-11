package com.ebay.park.base;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStripV22;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.analytics.HitBuilders;

public class BaseTabPagerFragment extends BaseFragment {

	protected TabAdapter mTabAdapter;
	protected ViewPager mTabPager;
	protected PagerTabStripV22 mPagerTabStrip;
	protected FloatingActionButton mFabPublish;
	protected ParkPageChangeListener mPagerChangeListener = new ParkPageChangeListener();
	protected ProgressBar mProgressBar;

	@Override
	public void onBackPressed() {
	}

	public interface EditManager {
		public void setCanEdit(boolean canEdit);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_item_category,
				(ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		disableRefreshSwipe();
		mPagerTabStrip = (PagerTabStripV22) rootView.findViewById(R.id.tab_title);
		mTabPager = (ViewPager) rootView.findViewById(R.id.tabs_pager);
		mTabPager.setAdapter(mTabAdapter);
		mTabPager.setOffscreenPageLimit(getOffsetLimit());
		mTabPager.addOnPageChangeListener(mPagerChangeListener);

		mFabPublish = (FloatingActionButton) rootView.findViewById(R.id.fab_publish);
		if (mFabPublish != null) {
			mFabPublish.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (PreferencesUtil.getParkToken(getActivity()) != null) {
						ParkApplication
								.getInstance()
								.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
								.send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
										.setAction("Clicks on Publish").setLabel("The user entered to publish an item")
										.build());
						ScreenManager.showCameraActivity((BaseActivity) getActivity());
					} else {
						MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.POST_AD);
					}

				}
			});
		}

		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

		for (int i = 0; i < mPagerTabStrip.getChildCount(); ++i) {
			View nextChild = mPagerTabStrip.getChildAt(i);
			if (nextChild instanceof TextView) {
				((TextView) nextChild).setTypeface(FontsUtil.getFSDemi(getContext()));
			}
		}
	}

	protected void showProgressBar(){
		mProgressBar.setVisibility(View.VISIBLE);
	}

	protected void hideProgressBar(){
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
        mTabAdapter = null;
        super.onDestroy();
	}

	protected void showPostAdButton() {
		mFabPublish.setVisibility(View.VISIBLE);
	}

	protected void updateTabColor(int position) {
		if (areTabsVisible()) {
			int color = getColorForItem(position);
			for (int i = 0; i < mPagerTabStrip.getChildCount(); ++i) {
				View nextChild = mPagerTabStrip.getChildAt(i);
				if (nextChild instanceof TextView) {
					final TextView textViewToConvert = (TextView) nextChild;

                    if (!TextUtils.isEmpty(textViewToConvert.getText()) && mTabAdapter.isItemInBold(getIndexFor(textViewToConvert.getText().toString()))){
                        textViewToConvert.setTypeface(Typeface.DEFAULT_BOLD);
						textViewToConvert.postInvalidateDelayed(50);
					}else{
						textViewToConvert.setTypeface(Typeface.DEFAULT);
					}
					textViewToConvert.setTextColor(getResources().getColor(R.color.White));
					textViewToConvert.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					textViewToConvert.setPadding(11, 12, 12, 11);
                }
			}
			mPagerTabStrip.setTabIndicatorColor(color);
		}
	}

    private int getIndexFor(String aText) {
        int pos = 0;
        while (pos < mTabAdapter.getCount() && !mTabAdapter.getPageTitle(pos).equals(aText)){
            pos++;
        }
        return pos;
    }

    protected int getColorForItem(int position) {
		return getResources().getColor(R.color.White);
	}

	protected boolean areTabsVisible() {
		return true;
	}

	@Override
	public void onRefresh() {
	}

	protected abstract class TabAdapter extends FragmentStatePagerAdapter {

		public TabAdapter(FragmentManager fm) {
			super(fm);
		}

        protected boolean isItemInBold(int position) {
            return false;
        }

	}

	protected class ParkPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			if (mFabPublish.getVisibility() != View.GONE){
				if (mFabPublish.isHidden()){
					mFabPublish.show(true);
				}
			}
		}

		@Override
		public void onPageScrolled(int position, float arg1, int arg2) {
			updateTabColor(position);
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	}

	protected int getOffsetLimit() {
		return 2;
	}

}
