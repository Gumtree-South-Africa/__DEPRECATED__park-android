package com.ebay.park.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.ItemModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupSubscribeRequest;
import com.ebay.park.requests.GroupUnsubscribeRequest;
import com.ebay.park.requests.ItemDetailRequest;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;

public class ItemGroupsListFragment extends BaseListFragment<ItemModel> {

	private static final String ITEM_ID = "ITEM_ID";

	private ListView mGroupList;
	private GroupAdapter mGroupAdapter;

	private long mItemId;

	public static ItemGroupsListFragment forItem(Long aItemId) {
		ItemGroupsListFragment fragment = new ItemGroupsListFragment();
		Bundle args = new Bundle();
		args.putLong(ITEM_ID, aItemId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(ITEM_ID)) {
			mItemId = getArguments().getLong(ITEM_ID);
		} else {
			getBaseActivity().onBackPressed();
		}
		setTitle(R.string.item_groups_list_title);
	}

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
		mGroupList.setAdapter(mGroupAdapter);
		mGroupList.setOnScrollListener(new ListScroll());
		mGroupList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onItemClicked(mGroupAdapter.getItem(position));
			}
		});
	}

	protected void onItemClicked(GroupModel aGroup) {
		if (aGroup != null){
			if (aGroup.getId() >= 0){
				ScreenManager.showGroupDetailActivity(getBaseActivity(), aGroup.getId());
			}
		}
	}

	@Override
	protected ModelAdapter getAdapter() {
		return mGroupAdapter;
	}

	@Override
	protected void initializeAdapter() {
		mGroupAdapter = new GroupAdapter();
	}

	@Override
	protected void getDataFromServer() {
		showProgress();
		mSpiceManager.execute(new ItemDetailRequest(mItemId), new GroupListListener());
	}

	@Override
	public void onBackPressed() {
	}

	private class GroupListListener extends ListListener {
	}

	@Override
	protected void loadData(ItemModel aResponse) {
		mIsLoading = false;
		if (!aResponse.getGroups().isEmpty()) {
			mGroupAdapter.merge(aResponse.getGroups());
			mGroupAdapter.orderListAlphabetically();
			mLoadedAllItems = true;
		}
	}

	@Override
	protected void clearData() {
		mGroupAdapter.clear();
	}

	private class GroupAdapter extends ModelAdapter<GroupModel> {

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getBaseActivity(), R.layout.item_group, null);
				convertView.setTag(new ViewHolder(convertView));
			}
			ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
			aViewHolder.load(getItem(position));
			return convertView;
		}

		protected void orderListAlphabetically(){
			if (mList.size() > 0) {
				Collections.sort(mList, new GroupComparator());
			}
		}

		private class ViewHolder {

			private ImageView mGroupImage;
			private TextView mGroupTitle;
			private TextView mGroupLocation;
			private TextView mGroupSuscribers;
			private TextView mGroupItems;
			private Button mGroupSuscribe;

			public ViewHolder(View vItem) {
				mGroupImage = (ImageView) vItem.findViewById(R.id.iv_group_picture);
				mGroupTitle = (TextView) vItem.findViewById(R.id.tv_group_name);
				mGroupLocation = (TextView) vItem.findViewById(R.id.tv_group_location);
				mGroupSuscribers = (TextView) vItem.findViewById(R.id.group_subscrs);
				mGroupItems = (TextView) vItem.findViewById(R.id.group_items);
				mGroupSuscribe = (Button) vItem.findViewById(R.id.btn_group_subscribe);
			}

			public void load(final GroupModel aGroup) {
				if (TextUtils.isEmpty(aGroup.getPictureUrl())) {
					aGroup.setPictureUrl(null);
				}
				Picasso.with(getBaseActivity()).load(aGroup.getPictureUrl())
						.placeholder(R.drawable.group_placeholder).fit().centerCrop().into(mGroupImage);

				mGroupTitle.setText(aGroup.getName());
				if (!TextUtils.isEmpty(aGroup.getLocationName())) {
					mGroupLocation.setText(aGroup.getLocationName());
				} else {
					mGroupLocation.setText(getString(R.string.none));
				}
				mGroupSuscribers.setText(String.valueOf(aGroup.getTotalSubscribers()));
				mGroupItems.setText(getString(R.string.items_label) + String.valueOf(aGroup.getTotalItems()));
				setUpSubscribeBtn(mGroupSuscribe, aGroup.getSubscribed());
				mGroupSuscribe.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (PreferencesUtil.getParkToken(getActivity()) != null) {
							BaseParkSessionRequest<Boolean> aRequest;
							if (aGroup.getSubscribed()) {
								aRequest = new GroupUnsubscribeRequest(aGroup.getId());
							} else {
								aRequest = new GroupSubscribeRequest(aGroup.getId());
							}
							mSpiceManager.execute(aRequest, new SubscribeRequestListener(mGroupSuscribe, aGroup));
						} else {
							MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.GROUPS);
						}
					}
				});
			}

		}

		private class GroupComparator implements Comparator<GroupModel>{

			@Override
			public int compare(GroupModel group1, GroupModel group2) {
				return group1.getName().toLowerCase().compareTo(group2.getName().toLowerCase());
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
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean isSuccesful) {
			hideProgress();
			mSubscribeButton.setEnabled(true);
			mGroup.setSubscribed(!mGroup.getSubscribed());
			setUpSubscribeBtn(mSubscribeButton, mGroup.getSubscribed());
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
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
