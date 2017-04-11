package com.ebay.park.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.interfaces.BackPressable;
import com.ebay.park.views.CustomSwipeRefresh;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.common.NeckSpiceManager.ProgressListener;
import com.squareup.leakcanary.RefWatcher;

public abstract class BaseFragment extends Fragment implements OnRefreshListener, ProgressListener, BackPressable {

	protected CustomSwipeRefresh mSyncProgressBar;
	protected NeckSpiceManager mSpiceManager = new NeckSpiceManager();

	protected boolean isShowingProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSpiceManager = new NeckSpiceManager(this);
		if (savedInstanceState != null) {
			isShowingProgress = savedInstanceState.getBoolean("isShowingProgress");
		}
	}

	@Override
	public void onStart() {
		mSpiceManager.start(this.getActivity());
		super.onStart();
	}

	@Override
	public void onStop() {
		mSpiceManager.shouldStop();
		if(isShowingProgress) {
			hideProgress();
		}
		super.onStop();
	}

	@Override
	public void onPause() {
		if(isShowingProgress) {
			hideProgress();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isShowingProgress) {
			showProgress();
		}
	}

	@Override public void onDestroy() {
		super.onDestroy();
		RefWatcher refWatcher = ParkApplication.getRefWatcher(getActivity());
		refWatcher.watch(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isShowingProgress", isShowingProgress);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Shows {@link SwipeRefreshLayout} on top of the screen. Override this
	 * method to provide additional actions but remember to call super.
	 */
	protected void showProgress() {
		isShowingProgress = true;
		if (mSyncProgressBar !=null) {
			mSyncProgressBar.post(new Runnable() {
				@Override
				public void run() {
					mSyncProgressBar.setRefreshing(isShowingProgress);
				}
			});
		}
	}

	/**
	 * Hides {@link SwipeRefreshLayout}.Override this method to provide
	 * additional actions but remember to call super.
	 */
	protected void hideProgress() {
		isShowingProgress = false;
		mSyncProgressBar.setRefreshing(isShowingProgress);
	}

	/**
	 * Call this on your fragment's {@link #onActivityCreated(Bundle)} if you
	 * want to disable swipe refresh gesture.
	 */
	protected void disableRefreshSwipe() {
		mSyncProgressBar.setEnabled(false);
	}

	/**
	 * Enables swipe refresh gesture.
	 */
	protected void enableSwipeGesture() {
		mSyncProgressBar.setEnabled(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseview = inflater.inflate(R.layout.fragment_base, container, false);
		mSyncProgressBar = (CustomSwipeRefresh) baseview.findViewById(R.id.swipe_container);
		mSyncProgressBar.setOnRefreshListener(this);
		return baseview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSyncProgressBar.setColorSchemeResources(R.color.VivaBlue, R.color.VivaYellow, R.color.VivaGreen, R.color.VivaRed);
	}

	protected BaseActivity getBaseActivity() {
		try {
			return (BaseActivity) getActivity();
		} catch (ClassCastException ex) {
			throw new ClassCastException("Base fragments must be attached to BaseActivities");
		}
	}

	@Override
	public void onShowProgress() {
		showProgress();
	}

	protected void setTitle(int aTitleRes){
		getBaseActivity().setTitle(aTitleRes);
	}

	protected void setTitle(String aTitleRes){
		getBaseActivity().setTitle(aTitleRes);
	}
}
