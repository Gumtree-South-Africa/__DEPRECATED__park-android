package com.ebay.park.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ebay.park.R;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public abstract class BaseListFragment<T> extends BaseFragment {

	protected static final int START_PAGE = 0;
	protected static final int PAGE_SIZE = 24;

	protected View mEmptyView;
	protected TextView mEmptyMessage;
	protected TextView mEmptyHint;
	protected Button mBtnAction;

	protected Integer page = START_PAGE;
	protected boolean mIsLoading;
	protected boolean mLoadedAllItems;
	protected boolean mHasPendingRequest;

	protected class ListScroll implements OnScrollListener {

		public ListScroll() {
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState != 0) {
				KeyboardHelper.hide(getBaseActivity(),getBaseActivity().getCurrentFocus());
			}
		}

		@Override
		public void onScroll(AbsListView aListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			int topRowVerticalPos = (aListView == null || aListView.getChildCount() == 0) ? 0 : aListView.getChildAt(0)
					.getTop();
			mSyncProgressBar.setEnabled(topRowVerticalPos >= 0 && firstVisibleItem == 0);

			if (visibleItemCount + firstVisibleItem >= totalItemCount && !(aListView.getAdapter().isEmpty())
					&& !mIsLoading && !mLoadedAllItems) {
				page++;
				mHasPendingRequest = true;
				getDataFromServer();
			}
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initializeAdapter();
	}

	protected abstract ModelAdapter getAdapter();
	protected abstract void initializeAdapter();

	@Override
	public void onRefresh() {
		clearData();
		page = START_PAGE;
		mHasPendingRequest = true;
		getDataFromServer();
	}

	@Override
	protected void showProgress() {
		super.showProgress();
		clearEmptyViews();
		if (mEmptyView != null) {
			mEmptyView.setVisibility(View.GONE);
		}
		mIsLoading = true;
	}

	@Override
	protected void hideProgress() {
		super.hideProgress();
		mIsLoading = false;
	}

	protected abstract void getDataFromServer();

	protected abstract void clearData();

	protected class ListListener extends BaseNeckRequestListener<T> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(T aResponse) {
			hideProgress();
			mHasPendingRequest = false;
			loadData(aResponse);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.error(exception.getMessage());
		}

	}

	protected abstract void loadData(T aResponse);

	protected ViewGroup getContainerLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
	}

	protected void loadEmptyView(Context aContext, ViewGroup container) {
		mEmptyView = container.findViewById(R.id.empty_layout);
		mEmptyMessage = (TextView) mEmptyView.findViewById(R.id.empty_message);
		mEmptyHint = (TextView) mEmptyView.findViewById(R.id.empty_hint);
		mBtnAction = (Button) mEmptyView.findViewById(R.id.btn_action);
	}

	protected void clearEmptyViews() {
		if (mEmptyMessage != null && mEmptyHint != null) {
			mEmptyMessage.setText(null);
			mEmptyHint.setText(null);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHasPendingRequest = true;
	}

	@Override
	public void onStart() {
		super.onStart();
		if(mHasPendingRequest) {
			getDataFromServer();
		}
	}

	protected abstract class ModelAdapter<V> extends BaseAdapter {

		protected List<V> mList;

		public ModelAdapter() {
			mList = new ArrayList<V>();
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public V getItem(int position) {
			if (position <= mList.size() && position >= 0) {
				return mList.get(position);
			} else {
				onRefresh();
				return mList.get(new Random().nextInt(mList.size() - 1));
			}

		}

		public List<V> getItems (){
			return mList;
		}

		public void merge(List<V> aList) {
			Iterator<V> aIterator = aList.iterator();
			while (aIterator.hasNext()) {
				V aData = aIterator.next();
				if (mList.contains(aData)) {
					mList.set(mList.indexOf(aData), aData);
				} else {
					mList.add(aData);
				}
			}
			notifyDataSetChanged();
		}

		public void reverseMerge(List<V> aList) {
			ListIterator<V> aIterator = aList.listIterator(aList.size());
			while (aIterator.hasPrevious()) {
				V aData = aIterator.previous();
				if (mList.contains(aData)) {
					mList.set(mList.indexOf(aData), aData);
				} else {
					mList.add(aData);
				}
			}
			notifyDataSetChanged();
		}

		public void clear() {
			mList.clear();
			notifyDataSetChanged();
		}

		public void remove(V aData) {
			mList.remove(aData);
			notifyDataSetChanged();
		}

	}

}
