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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.photos.views.HeaderGridView;
import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.CarouselCategoryFragment;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.OwnerModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.ItemFollowRequest;
import com.ebay.park.requests.ItemUnFollowRequest;
import com.ebay.park.responses.ItemListResponse;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.github.clans.fab.FloatingActionButton;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;
import com.swrve.sdk.SwrveSDK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for items listing, subclasses must provide the items list by
 * querying the server with different filters or endpoints.
 *
 * @author federico.perez
 */
public abstract class BaseItemListFragment extends BaseListFragment<ItemListResponse> {

    protected HeaderGridView mItemGrid;
    protected ItemAdapter mItemAdapter;
    protected TextView mCategoryName;
    private List<ItemModel> mToBeRemoved = new ArrayList<ItemModel>();
    private int mPreviousVisibleItem;
    private static final String STATE_SOLD = "SOLD";
    private static final String STATE_EXPIRED = "EXPIRED";
    private static final String STATE_PENDING = "PENDING";
    private static final String STATE_ACTIVE = "ACTIVE";
    private static final int NO_LIKES = 0;

    private void updateRemoved(View aView, ItemModel aItem, boolean isSelected) {
        aView.setSelected(!isSelected);
        if (!isSelected) {
            mToBeRemoved.add(aItem);
        } else {
            if (mToBeRemoved.contains(aItem)) {
                mToBeRemoved.remove(aItem);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_list,
                getContainerLayout(inflater, container, savedInstanceState), true);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        loadEmptyView(getBaseActivity(), (ViewGroup) rootView.findViewById(R.id.list_container));
        mItemGrid = (HeaderGridView) rootView.findViewById(R.id.item_grid);
        loadHeaderView();
        mItemGrid.setEmptyView(mEmptyView);
        mItemGrid.setAdapter(mItemAdapter);
        mItemGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mIsLoading && id != -1) {
                    onItemClicked((ItemModel) mItemGrid.getItemAtPosition(position),position);
                }
            }
        });

        mCategoryName = (TextView) rootView.findViewById(R.id.tv_category_name);

        mItemGrid.setOnScrollListener(new BaseListFragment.ListScroll());

        final FloatingActionButton fab = (FloatingActionButton) getBaseActivity().findViewById(R.id.fab_publish);
        if (fab != null){
            final BaseListFragment.ListScroll listScroll = new BaseListFragment.ListScroll();
            mItemGrid.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    listScroll.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                    if (fab.getVisibility() != View.GONE) {
                        if (firstVisibleItem > mPreviousVisibleItem) {
                            fab.hide(true);
                        } else if (firstVisibleItem < mPreviousVisibleItem) {
                            fab.show(true);
                        }
                        mPreviousVisibleItem = firstVisibleItem;
                    }
                }
            });
        }
    }

    protected void loadHeaderView() {
    }

    protected void onItemClicked(ItemModel aItem, int pos) {
    }

    @Override
    protected ModelAdapter getAdapter() {
        return mItemAdapter;
    }

    @Override
    protected void initializeAdapter() {
        mItemAdapter = new ItemAdapter();
    }

    protected List<ItemModel> getRemovedItems() {
        return this.mToBeRemoved;
    }

    @Override
    protected void clearData() {
        if (mItemAdapter != null) {
            mItemAdapter.clear();
        } else {
            mItemAdapter = new ItemAdapter();
        }
        page = START_PAGE;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (ParkApplication.sComesFromFilter && mItemAdapter == null) {
            ParkApplication.sComesFromFilter = false;
            getActivity().finish();
        }
    }

    protected boolean forEdition() {
        return false;
    }

    protected class ItemAdapter extends ModelAdapter<ItemModel> {

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getBaseActivity(), R.layout.item_product, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
            aViewHolder.load(getItem(position));
            return convertView;
        }

        private class ViewHolder {

            private View mItem;
            private ImageView mItemImage;
            private ImageView mItemFlagNew;
            private ImageView mItemFlagStatus;
            private TextViewDemi mItemPrice;
            private TextView mItemUserName;
            private TextViewBook mItemLikes;
            private View mItemControls;
            private ButtonDemi mItemRemove;
            private ImageView mFlagLike;

            public ViewHolder(View vItem) {
                this.mItem = vItem;
                mItemImage = (ImageView) vItem.findViewById(R.id.iv_item_picture);
                mItemFlagNew = (ImageView) vItem.findViewById(R.id.iv_new_item);
                mItemFlagStatus = (ImageView) vItem.findViewById(R.id.iv_status_item);
                mItemPrice = (TextViewDemi) vItem.findViewById(R.id.tv_item_price);
                mItemUserName = (TextView) vItem.findViewById(R.id.tv_item_username);
                mItemLikes = (TextViewBook) vItem.findViewById(R.id.tv_item_likes);
                mItemControls = vItem.findViewById(R.id.ly_controls);
                mItemRemove = (ButtonDemi) vItem.findViewById(R.id.btn_item_remove);
                mFlagLike = (ImageView) vItem.findViewById(R.id.iv_like);
            }

            public void load(final ItemModel aItem) {

                if (TextUtils.isEmpty(aItem.getPictureUrl())) {
                    aItem.setPictureUrl(null);
                }
                Picasso.with(getBaseActivity()).load(aItem.getPictureUrl()).placeholder(R.drawable.img_placeholder)
                        .fit().centerCrop().into(mItemImage);

                mItemPrice.setText(FormatUtils.formatPrice(aItem.getPrice()));

                switch (aItem.getStatus().toUpperCase()) {
                    case STATE_ACTIVE:
                        mItemFlagStatus.setVisibility(View.GONE);
                        break;
                    case STATE_PENDING:
                        Picasso.with(getBaseActivity()).load(R.drawable.flag_pending).into(mItemFlagStatus);
                        mItemFlagStatus.setVisibility(View.VISIBLE);
                        break;
                    case STATE_EXPIRED:
                        Picasso.with(getBaseActivity()).load(R.drawable.flag_expired).into(mItemFlagStatus);
                        mItemFlagStatus.setVisibility(View.VISIBLE);
                        break;
                    case STATE_SOLD:
                        Picasso.with(getBaseActivity()).load(R.drawable.flag_sold).into(mItemFlagStatus);
                        mItemFlagStatus.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mItemFlagStatus.setVisibility(View.GONE);
                }

                mItemUserName.setText(aItem.getUser().getUsername());
                mItemUserName.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onUsenameClicked(aItem.getUser());
                    }
                });

                if (aItem.isNewItem()) {
                    mItemFlagNew.setVisibility(View.VISIBLE);
                } else {
                    mItemFlagNew.setVisibility(View.GONE);
                }

                if (forEdition()) {
                    mItemControls.setVisibility(View.INVISIBLE);
                    mItemRemove.setVisibility(View.VISIBLE);
                    mItemRemove.setTag(aItem);
                    mItem.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View btnRemove = v.findViewById(R.id.btn_item_remove);
                            updateRemoved(btnRemove, (ItemModel) btnRemove.getTag(), btnRemove.isSelected());
                        }
                    });
                    mItemRemove.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            updateRemoved(v, (ItemModel) v.getTag(), v.isSelected());
                        }
                    });
                    mItemRemove.setSelected(mToBeRemoved.contains(aItem));
                } else {
                    mItemRemove.setOnClickListener(null);
                    mItemControls.setVisibility(View.VISIBLE);
                    mItemRemove.setVisibility(View.INVISIBLE);
                    mFlagLike.setEnabled("ACTIVE".equals(aItem.getStatus()));
                    mItemLikes.setVisibility((aItem.getLikes() > NO_LIKES) ? View.VISIBLE : View.INVISIBLE);
                    mItemLikes.setText(Integer.toString(aItem.getLikes()));
                    mFlagLike.setSelected(aItem.isFollowedByUser());
                    mFlagLike.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (PreferencesUtil.getParkToken(getActivity()) != null) {
                                if (aItem.isFollowedByUser()){
                                    SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_ATTEMPT);
                                } else {
                                    SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_ATTEMPT);

                                    Map<String, String> attributes = new HashMap<>();
                                    attributes.put(SwrveEvents.FAVORITE_ITEM, aItem.getCategory().getName());
                                    attributes.put(SwrveEvents.FAVORITE_ITEM + "." + aItem.getCategory().getName()
                                            .replace(" ", "-"), aItem.getName());
                                    SwrveSDK.userUpdate(attributes);
                                }
                                mFlagLike.setEnabled(false);
                                doFollow(aItem, mItemLikes);
                            } else {
                                if (CarouselCategoryFragment.sTabPosition != -1) {
                                    ParkApplication.sNavegarTab_toGo = CarouselCategoryFragment.sTabPosition;
                                    ParkApplication.sJustLoggedFromNavegar = true;
                                }
                                MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.CATEGORIES);
                            }

                        }
                    });
                }
            }

        }
    }

    protected void onUsenameClicked(OwnerModel aUser) {
        ScreenManager.showUserProfileFragment(getBaseActivity(), aUser.getUsername(), true);
    }

    protected void doFollow(ItemModel aItem) {
        doFollow(aItem, new FollowListener(aItem));
    }

    protected void doFollow(ItemModel aItem, View aDisablableView){
        doFollow(aItem, new FollowListener(aItem, aDisablableView));
    }

    protected void doFollow(ItemModel aItem, FollowListener aListener){
        showProgress();
        BaseParkSessionRequest<Boolean> aRequest;
        if (aItem.isFollowedByUser()) {
            aRequest = new ItemUnFollowRequest(aItem.getId());
            aItem.unlike();
        } else {
            aItem.like();
            aRequest = new ItemFollowRequest(aItem.getId());
        }
        aItem.setFollowedByUser(!aItem.isFollowedByUser());
        mSpiceManager.execute(aRequest, aListener);
        mItemAdapter.notifyDataSetChanged();
    }

    protected void hideFlagNewItem(ItemModel aItem) {
        aItem.setNewItem(false);
        mItemAdapter.notifyDataSetChanged();
    }

    private class FollowListener extends BaseNeckRequestListener<Boolean> {

        private ItemModel mItem;
        private boolean mIsFollowRequest;
        private View mView;

        public FollowListener(ItemModel aItem) {
            this.mItem = aItem;
            mIsFollowRequest = !aItem.isFollowedByUser();
        }

        public FollowListener(ItemModel aItem, View aView) {
            this(aItem);
            this.mView = aView;
        }

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(mIsFollowRequest ? SwrveEvents.WATCHLIST_ADD_FAIL :
                    SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
            hideProgress();
            undo();
            if (mView != null){
                mView.setEnabled(true);
            }
            MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Boolean success) {
            if (success){
                SwrveSDK.event(mIsFollowRequest ? SwrveEvents.WATCHLIST_ADD_SUCCESS :
                        SwrveEvents.WATCHLIST_REMOVE_SUCCESS);
                mItem.setFollowedByUser(mIsFollowRequest);
                mItemAdapter.notifyDataSetChanged();
            }
            if (mView != null){
                mView.setEnabled(true);
            }
            hideProgress();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(mIsFollowRequest ? SwrveEvents.WATCHLIST_ADD_FAIL :
                    SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
            hideProgress();
            undo();
            if (mView != null){
                mView.setEnabled(true);
            }
            Logger.error(exception.getMessage());
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
        }

        private void undo() {
            if (mIsFollowRequest){
                mItem.unlike();
            }else{
                mItem.like();
            }
            mItemAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void loadData(ItemListResponse aResponse) {
        mIsLoading = false;
        if (!aResponse.getItems().isEmpty()) {
            List<ItemModel> itemsList = new ArrayList<ItemModel>();
            itemsList.addAll(aResponse.getItems());
            long fetchedItems = mItemAdapter.getCount() + aResponse.getItems().size();
            mLoadedAllItems = aResponse.getAmountItemsFound() <= fetchedItems;
            mItemAdapter.merge(itemsList);
        }
        if (mItemAdapter.isEmpty()) {
            mEmptyMessage.setText(aResponse.getNoResultsMessage());
            mEmptyHint.setText(aResponse.getNoResultsHint());
            mItemAdapter.notifyDataSetChanged();
        }
    }

}