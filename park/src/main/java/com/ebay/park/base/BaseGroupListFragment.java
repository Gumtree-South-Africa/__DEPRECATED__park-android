package com.ebay.park.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.model.GroupModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupSubscribeRequest;
import com.ebay.park.requests.GroupUnsubscribeRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.github.clans.fab.FloatingActionButton;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

public abstract class BaseGroupListFragment extends BaseListFragment<GroupListResponse> {

	protected ListView mGroupList;
	protected GroupAdapter mGroupAdapter;
	private int mPreviousVisibleGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_groups_list,
				getContainerLayout(inflater, container, savedInstanceState), true);
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		mGroupList = (ListView) rootView.findViewById(R.id.groups_list);
		loadEmptyView(getBaseActivity(), (ViewGroup) rootView.findViewById(R.id.list_container));
		loadHeaderView();
		mGroupList.setAdapter(getAdapter());
		mGroupList.setOnScrollListener(new ListScroll());
		final FloatingActionButton fab = (FloatingActionButton) getBaseActivity().findViewById(R.id.fab_publish);
		if (fab != null){
			final BaseListFragment.ListScroll listScroll = new BaseListFragment.ListScroll();
			mGroupList.setOnScrollListener(new AbsListView.OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleGroup, int visibleItemCount, int totalItemCount) {
					listScroll.onScroll(view, firstVisibleGroup, visibleItemCount, totalItemCount);

					if (fab.getVisibility() != View.GONE) {
						if (firstVisibleGroup > mPreviousVisibleGroup) {
							fab.hide(true);
						} else if (firstVisibleGroup < mPreviousVisibleGroup) {
							fab.show(true);
						}
						mPreviousVisibleGroup = firstVisibleGroup;
					}
				}
			});
		}
		mGroupList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mIsLoading && id != -1){
					onItemClicked((GroupModel) getAdapter().getItem(getGroupSelectedPosition(position)));
				}
			}
		});
	}

	protected ModelAdapter getAdapter() {
		return mGroupAdapter;
	}

	protected int getGroupSelectedPosition(int position) {
		return position;
	}

	protected void loadHeaderView() {
	}

	protected void onItemClicked(GroupModel item) {
	}

	protected void onGroupSuscribeClicked(GroupModel aGroup) {
	}

	@Override
	protected void loadData(GroupListResponse aResponse) {
		mIsLoading = false;
		if (!aResponse.getGroups().isEmpty()) {
			long fetchedItems = getAdapter().getCount() + aResponse.getGroups().size();
			mLoadedAllItems = aResponse.getTotalGroupsFound() <= fetchedItems;
			getAdapter().merge(aResponse.getGroups());
		}
		if (getAdapter().isEmpty()) {
			mGroupList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(aResponse.getNoResultsMessage());
			mEmptyHint.setText(aResponse.getNoResultsHint());
			getAdapter().notifyDataSetChanged();
		}
	}

	@Override
	protected void clearData() {
		page = START_PAGE;
		if (getAdapter() != null){
			getAdapter().clear();
		} else {
			initializeAdapter();
		}
	}

	@Override
	protected void initializeAdapter() {
		mGroupAdapter = new GroupAdapter();
	}

	protected boolean isActionEnabled() {
		return true;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (ParkApplication.sComesFromFilter && getAdapter() == null){
			ParkApplication.sComesFromFilter = false;
			getActivity().finish();
		}
	}

	protected class GroupAdapter extends ModelAdapter<GroupModel> {

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getBaseActivity(), R.layout.item_group, null);
				convertView.setTag(getViewHolder(convertView));
			}
			ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
			aViewHolder.load(getItem(position));
			return convertView;
		}

		protected ViewHolder getViewHolder(View aView) {
			return new ViewHolder(aView);
		}

		protected class ViewHolder {

			protected LinearLayout mGroupLayout;
			protected ImageView mGroupImage;
			protected ImageView mRoundedShape;
			protected TextView mGroupTitle;
			private TextView mGroupLocation;
			private TextView mGroupSuscribers;
			private TextView mGroupItems;
			private Button mGroupSuscribe;

			public ViewHolder(View vItem) {
				mGroupLayout = (LinearLayout) vItem.findViewById(R.id.ly_item_group);
				mGroupImage = (ImageView) vItem.findViewById(R.id.iv_group_picture);
				mRoundedShape = (ImageView) vItem.findViewById(R.id.iv_rounded_shape);
				mGroupTitle = (TextView) vItem.findViewById(R.id.tv_group_name);
				mGroupLocation = (TextView) vItem.findViewById(R.id.tv_group_location);
				mGroupSuscribers = (TextView) vItem.findViewById(R.id.group_subscrs);
				mGroupItems = (TextView) vItem.findViewById(R.id.group_items);
				mGroupSuscribe = (Button) vItem.findViewById(R.id.btn_group_subscribe);
			}

			public void load(final GroupModel aGroup) {
				if (!TextUtils.isEmpty(aGroup.getPictureUrl())) {
					Picasso.with(getBaseActivity()).load(aGroup.getPictureUrl())
							.placeholder(R.drawable.img_placeholder).fit().centerCrop().into(mGroupImage);
				} else {
					Picasso.with(getBaseActivity()).load(R.drawable.group_placeholder)
							.placeholder(R.drawable.img_placeholder).fit().centerCrop().into(mGroupImage);
				}
				mGroupTitle.setText(aGroup.getName());
				if (!TextUtils.isEmpty(aGroup.getLocationName())) {
					mGroupLocation.setText(aGroup.getLocationName());
				} else {
					mGroupLocation.setText(getString(R.string.none));
				}
				mGroupSuscribers.setText(String.valueOf(aGroup.getTotalSubscribers()));
				mGroupItems.setText(String.valueOf(aGroup.getTotalItems()));
				setUpSubscribeBtn(mGroupSuscribe, aGroup.getSubscribed());

				boolean isGroupOwner = aGroup.getOwner() != null
						&& TextUtils.equals(aGroup.getOwner().getUsername(), ParkApplication.getInstance()
						.getUsername());
				if (!isActionEnabled() || isGroupOwner) {
					mGroupSuscribe.setVisibility(View.GONE);
				} else {
					mGroupSuscribe.setVisibility(View.VISIBLE);
					mGroupSuscribe.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!mIsLoading) {
								if (PreferencesUtil.getParkToken(getActivity()) != null) {
									onGroupSuscribeClicked(aGroup);
									BaseParkSessionRequest<Boolean> aRequest;
									if (aGroup.getSubscribed()) {
										aRequest = new GroupUnsubscribeRequest(aGroup.getId());
									} else {
										aRequest = new GroupSubscribeRequest(aGroup.getId());
									}
									showProgress();
									mSpiceManager.execute(aRequest, new SubscribeRequestListener(mGroupSuscribe, aGroup));
								} else {
									MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.GROUPS);
								}
							}
						}
					});
				}
			}

		}

	}

	private class SubscribeRequestListener extends BaseNeckRequestListener<Boolean> {
		private GroupModel mGroup;
		private Button mSubscribeButton;

		public SubscribeRequestListener(Button subscribeBtn, GroupModel group) {
			this.mGroup = group;
			this.mSubscribeButton = subscribeBtn;
		}

		@Override
		public void onRequestError(Error error) {
			mSubscribeButton.setEnabled(true);
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean isSuccesful) {
			hideProgress();
			mSubscribeButton.setEnabled(true);
			mGroup.setSubscribed(!mGroup.getSubscribed());
			if (mGroup.getSubscribed()) {
				mGroup.setTotalSubscribers(mGroup.getTotalSubscribers() + 1);
			} else {
				mGroup.setTotalSubscribers(mGroup.getTotalSubscribers() - 1);
			}
			getAdapter().notifyDataSetChanged();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
			mSubscribeButton.setEnabled(true);
			Logger.error(exception.getMessage());
		}

	}

	private void setUpSubscribeBtn(Button btnSubscribe, boolean isSubscriptedActiveUser) {
		if (isSubscriptedActiveUser) {
			btnSubscribe.setActivated(true);
			btnSubscribe.setText(R.string.unsubscribe);
		} else {
			btnSubscribe.setActivated(false);
			btnSubscribe.setText(R.string.subscribe);
		}
	}

}
