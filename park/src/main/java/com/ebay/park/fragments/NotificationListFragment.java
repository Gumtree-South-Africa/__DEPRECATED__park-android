package com.ebay.park.fragments;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.model.FeedModel;
import com.ebay.park.model.FeedModel.Action;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.OwnerModel;
import com.ebay.park.requests.FeedListRequest;
import com.ebay.park.requests.FeedReadRequest;
import com.ebay.park.requests.FollowUserRequest;
import com.ebay.park.requests.ListConversationsByItem;
import com.ebay.park.requests.UnfollowUserRequest;
import com.ebay.park.responses.ConversationListResponse;
import com.ebay.park.responses.FeedListResponse;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.ButtonDemi;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NotificationListFragment extends BaseListFragment<FeedListResponse> implements OnItemClickListener{

    private static final String ARG = "MYFEEDARG";

    private NotificationAdapter mAdapter;
    private boolean mIsOnlyMyFeeds = false;
    private Boolean mUpdate = true;

    public static NotificationListFragment allNotifications() {
        NotificationListFragment fragment = new NotificationListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG, Boolean.FALSE);
        fragment.setArguments(args);
        return fragment;
    }

    public static NotificationListFragment myNotifications() {
        NotificationListFragment fragment = new NotificationListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG, Boolean.TRUE);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG)) {
            mIsOnlyMyFeeds = getArguments().getBoolean(ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_feed, getContainerLayout(inflater, container, savedInstanceState), true);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        loadEmptyView(getBaseActivity(), (ViewGroup) rootView.findViewById(R.id.list_container));
        super.onViewCreated(rootView, savedInstanceState);
        ListView vNotificationList = (ListView) rootView.findViewById(R.id.notification_list);
        vNotificationList.setEmptyView(mEmptyView);
        vNotificationList.setAdapter(mAdapter);
        vNotificationList.setOnItemClickListener(this);
        vNotificationList.setOnScrollListener(new ListScroll());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        getDataFromServer();
    }

    @Override
    protected ModelAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void initializeAdapter() {
        mAdapter = new NotificationAdapter();
    }

    @Override
    protected void getDataFromServer() {
        mIsLoading = true;
        showProgress();
        mSpiceManager.executeCacheRequest(new FeedListRequest(ParkApplication.getInstance().getUsername()), new NotificationListener());
    }

    @Override
    public void onRefresh() {
        mUpdate = true;
        super.onRefresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FeedModel feed = mAdapter.getItem(position);

        if (feed != null) {
            mSpiceManager.execute(new FeedReadRequest(String.valueOf(feed.getFeedId())),
                    new FeedReadRequestListener());
        }

        switch (feed.getAction()) {
            case CONVERSATION_ACCEPTED:
            case CONVERSATION_REJECTED:
            case CHAT_SENT:
            case SOLD_AN_ITEM_FOR_INTERESTED_FOLLOWERS:
                if (feed.getItemStatus() != "PENDING" || feed.getItemOwnerName().equals(ParkApplication.getInstance().getUsername())) {
                    final String role = (ParkApplication.getInstance().getUsername().equals(feed.getItemOwnerName())) ? Constants.ROLE_SELLER
                            : Constants.ROLE_BUYER;
                    if (feed.getItemStatus() == null) {
                        MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
                                getBaseActivity().getCroutonsHolder());
                    } else {
                        ParkApplication.sItemTapped = createPreloadedItem(feed);
                        if (feed.getConversationId()!=null) {
                            ScreenManager.showChatActivity(getBaseActivity(), null, feed.getConversationId(), feed.getItemId(), role);
                        } else {
                            mSpiceManager.execute(ListConversationsByItem.searchExistingNegotiations(feed.getItemId()),
                                    new ExistingConversationListener(role));
                        }
                    }
                }
                break;
            case DELETE_AN_ITEM:
            case ITEM_REJECTED:
                MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.item_no_longer_availiable),
                        getBaseActivity().getCroutonsHolder());
                break;
            case FOLLOW_ITEM:
            case ITEM_APROVED:
            case NEW_ITEM:
            case UPDATE_AN_ITEM:
            case NEW_COMMENT_ON_ITEM:
            case ITEM_EXPIRE:
            case ITEM_BANNED:
            case SOLD_AN_ITEM:
            case ADD_ITEM_TO_GROUP:
            case NEW_COMMENT_WHEN_SUBSCRIBE:
            case ITEM_ABOUT_TO_EXPIRE:
                if (feed.getItemStatus() == null) {
                    MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
                            getBaseActivity().getCroutonsHolder());
                } else {
                    ParkApplication.sItemTapped = createPreloadedItem(feed);
                    ScreenManager.showItemDetailActivity(getBaseActivity(), feed.getItemId(),
                            null, null);
                }
                break;
            case FB_FRIEND_USING_THE_APP:
            case FOLLOW_USER:
            case UNFOLLOW_USER:
                if (!TextUtils.isEmpty(feed.getUsername()))
                    ScreenManager.showProfileActivity(getBaseActivity(), feed.getUsername());
                break;
            case USER_RATED:
                ScreenManager.showRateListActivity(getBaseActivity(), ParkApplication.getInstance().getUsername());
                break;
            case PENDING_RATE:
                ScreenManager.showRateListActivity(getBaseActivity(), ParkApplication.getInstance().getUsername(), true);
                break;
            case FB_TOKEN_EXPIRED:
            case TW_TOKEN_EXPIRED:
                ScreenManager.showSocialNetworksConfigActivity(getBaseActivity());
                break;
            case ITEM_DELETED_FROM_MODERATION_DUPLICATED:
            case ITEM_DELETED_FROM_MODERATION_PICTURES:
            case ITEM_DELETED_FROM_MODERATION_SERVICES:
            case ITEM_DELETED_FROM_MODERATION_MAKEUP:
            case ITEM_DELETED_FROM_MODERATION_ANIMALS:
            case ITEM_DELETED_FROM_MODERATION_COMMISSION:
            case ITEM_DELETED_FROM_MODERATION_STYLE:
            case ITEM_DELETED_FROM_MODERATION_PRICE:
            case ITEM_DELETED_FROM_MODERATION_FORBIDDEN:
                ScreenManager.showProfileActivity(getBaseActivity());
                break;
            case FEED_FROM_MODERATION:
                view.findViewById(R.id.ly_feed).setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.rounded_rectangular_border));
                view.findViewById(R.id.ly_feed).setPadding(-1, -1, -1, -1);
                ((ImageView) view.findViewById(R.id.iv_rounded_shape))
                        .setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_right));
                view.findViewById(R.id.tv_notification_flag).setVisibility(View.GONE);
                ((ImageView) view.findViewById(R.id.feed_profile_img_mask)).
                        setImageDrawable(getResources().getDrawable(R.drawable.avatar_medium_mask));
                mAdapter.getItem(position).setRead(true);
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private class ExistingConversationListener extends BaseNeckRequestListener<ConversationListResponse> {

        private String mRole;

        public ExistingConversationListener(String role){
            mRole = role;
        }

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            hideProgress();
        }

        @Override
        public void onRequestSuccessfull(ConversationListResponse response) {
            hideProgress();
            List<ConversationModel> conversations = response.getItems();
            if (conversations.size() > 0) {
                ScreenManager.showChatActivity(getBaseActivity(), null,
                        conversations.get(0).getConversationId(),
                        conversations.get(0).getItemId(), mRole);
            } else {
                MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.item_no_longer_availiable),
                        getBaseActivity().getCroutonsHolder());
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Logger.error(exception.getMessage());
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

    }

	@Override
    public void onBackPressed() {
    }

    private class NotificationListener extends ListListener {

        @Override
        public void onRequestSuccessfull(FeedListResponse aResponse) {
            super.onRequestSuccessfull(aResponse);
            if (mUpdate) {
                ActivityFeedFragment.sUnreadCount = 0;
                mUpdate = false;
            }
        }

    }

    @Override
    protected void clearData() {
        mAdapter.clear();
    }

    @Override
    protected void loadData(FeedListResponse aNotificationList) {
        mIsLoading = false;
        if (!aNotificationList.getItems().isEmpty()) {
            long fetchedItems = mAdapter.getCount() + aNotificationList.getItems().size();
            mLoadedAllItems = aNotificationList.getAmountItemsFound() <= fetchedItems;
            mAdapter.merge(aNotificationList.getItems());
        }
        if (mAdapter.isEmpty()) {
            if (!mIsOnlyMyFeeds) {
                mEmptyMessage.setText(aNotificationList.getNoResultsMessage());
                mEmptyHint.setText(aNotificationList.getNoResultsHint());
            } else {
                mEmptyMessage.setText(getString(R.string.feeds_no_result_msg));
                mEmptyHint.setText(getString(R.string.feeds_no_result_hint));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class NotificationAdapter extends ModelAdapter<FeedModel> {

        @Override
        public void merge(List<FeedModel> aList) {
            if (!mIsOnlyMyFeeds) {
                super.merge(aList);
            } else {
                super.merge(getMyItems(aList));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getBaseActivity()).inflate(R.layout.item_activity_feed, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
            aViewHolder.load(getItem(position));
            return convertView;
        }

        private class ViewHolder {

            private View mNotification;
            private FrameLayout mProfileImgLy;
            private ImageView mItemImgDiv;
            private ImageView mItemArrow;
            private ImageView mRoundedShape;
            private ImageView mProfileImg;
            private ImageView mItemImg;
            private ImageView mProfileImgMask;
            private ImageView mNewFeedFlag;
            private TextView mMessage;
            private TextView mTime;
            private ButtonDemi mFollowUser;

            public ViewHolder(View aView) {
                mNotification = aView.findViewById(R.id.ly_feed);
                mProfileImgLy = (FrameLayout) aView.findViewById(R.id.ly_profile_pic_owner);
                mItemArrow = (ImageView) aView.findViewById(R.id.feed_arrow);
                mRoundedShape = (ImageView) aView.findViewById(R.id.iv_rounded_shape);
                mItemImgDiv = (ImageView) aView.findViewById(R.id.iv_divider);
                mProfileImg = (ImageView) aView.findViewById(R.id.feed_profile_img);
                mItemImg = (ImageView) aView.findViewById(R.id.feed_item_img);
                mProfileImgMask = (ImageView) aView.findViewById(R.id.feed_profile_img_mask);
                mMessage = (TextView) aView.findViewById(R.id.feed_message);
                mTime = (TextView) aView.findViewById(R.id.feed_time);
                mNewFeedFlag = (ImageView) aView.findViewById(R.id.tv_notification_flag);
                mFollowUser = (ButtonDemi) aView.findViewById(R.id.feed_follow);
            }

            public void load(final FeedModel feed) {
                mTime.setText(FormatUtils.timeAgo(feed.getCreationDate() / 1000));
                SpannableString aSpannable = new SpannableString(feed.getMessage());
                if (!TextUtils.isEmpty(feed.getUsername()) && feed.getMessage().contains(feed.getUsername())) {
                    aSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.VivaLightBlue)), feed.getMessage().indexOf(feed.getUsername()), feed.getMessage().indexOf(feed.getUsername()) + feed.getUsername().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    getBoldSpan(aSpannable, feed.getMessage(), feed.getUsername(), true);
                }
                getBoldSpan(aSpannable, feed.getMessage(), feed.getItemName());
                getBoldSpan(aSpannable, feed.getMessage(), feed.getGroupName());
                mMessage.setText(aSpannable);
                if (TextUtils.isEmpty(feed.getItemPicture())) {
                    mItemImgDiv.setVisibility(View.GONE);
                    mItemImg.setVisibility(View.GONE);
                    mRoundedShape.setVisibility(View.GONE);
                    if (feed.getAction() == FeedModel.Action.FB_TOKEN_EXPIRED || feed.getAction() == FeedModel.Action.TW_TOKEN_EXPIRED) {
                        mItemArrow.setVisibility(View.VISIBLE);
                    } else if (feed.getAction() == Action.FB_FRIEND_USING_THE_APP) {
                        mFollowUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (feed.getRead()) {
                                    mNotification.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_rectangular_border));
                                    mNotification.setPadding(-1, -1, -1, -1);
                                    mRoundedShape.setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_right));
                                    mNewFeedFlag.setVisibility(View.GONE);
                                    mProfileImgMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_medium_mask));
                                }
                                mFollowUser.setEnabled(false);
                                if (!mIsLoading){
                                    if (mFollowUser.isActivated()) {
                                        showProgress();
                                        mSpiceManager.execute(new UnfollowUserRequest(feed.getUsername()), new FollowUnfollowListener(feed));
                                    } else {
                                        showProgress();
                                        mSpiceManager.execute(new FollowUserRequest(feed.getUsername()), new FollowUnfollowListener(feed));

                                    }
                                } else {
                                    mFollowUser.setEnabled(true);
                                }
                            }
                        });
                        setUpSubscribeBtn(feed.getFollowedByUser());
                        mFollowUser.setVisibility(View.VISIBLE);
                    } else {
                        mFollowUser.setVisibility(View.GONE);
                    }
                } else {
                    mItemImg.setVisibility(View.VISIBLE);
                    mItemImgDiv.setVisibility(View.VISIBLE);
                    mRoundedShape.setVisibility(View.VISIBLE);
                    mItemArrow.setVisibility(View.GONE);
                    mFollowUser.setVisibility(View.GONE);
                    Picasso.with(getBaseActivity()).load(feed.getItemPicture()).fit().centerCrop().into(mItemImg);
                }
                mProfileImgLy.setVisibility(View.VISIBLE);
                mMessage.setPadding(0, 0, 0, 0);
                mTime.setPadding(0,0,0,0);
                if (feed.getAction() == FeedModel.Action.FB_TOKEN_EXPIRED) {
                    Picasso.with(getBaseActivity()).load(R.drawable.avatar_facebook_image_fit).fit().centerCrop().into(mProfileImg);
                } else if (feed.getAction() == FeedModel.Action.TW_TOKEN_EXPIRED) {
                    Picasso.with(getBaseActivity()).load(R.drawable.avatar_twitter_image_fit).fit().centerCrop().into(mProfileImg);
                } else if (feed.getAction() == FeedModel.Action.FEED_FROM_MODERATION) {
                    Picasso.with(getBaseActivity()).load(R.drawable.avatar_viva_image_fit).fit().centerCrop().into(mProfileImg);
                } else {
                    if (!TextUtils.isEmpty(feed.getUsername()) && feed.getMessage().contains(feed.getUsername())) {
                        if (!TextUtils.isEmpty(feed.getProfilePicture())) {
                            mProfileImgLy.setVisibility(View.VISIBLE);
                            Picasso.with(getBaseActivity()).load(feed.getProfilePicture()).placeholder(R.drawable.avatar_medium_ph_image_fit).fit()
                                    .centerCrop().into(mProfileImg);
                        } else {
                            Picasso.with(getBaseActivity()).load(R.drawable.avatar_medium_ph_image_fit).fit().centerCrop().into(mProfileImg);
                        }
                    } else {
                        mProfileImgLy.setVisibility(View.GONE);
                        mMessage.setPadding(17, 0, 0, 0);
                        mTime.setPadding(17,0,0,0);
                    }
                }
                if (!feed.getRead()) {
                    mNotification.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_rectangular_border_unread));
                    mRoundedShape.setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_right_shadow));
                    mNewFeedFlag.setVisibility(View.VISIBLE);
                    mProfileImgMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_mask_unread));
                } else {
                    mNotification.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_rectangular_border));
                    mNotification.setPadding(-1,-1,-1,-1);
                    mRoundedShape.setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_right));
                    mNewFeedFlag.setVisibility(View.GONE);
                    mProfileImgMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_medium_mask));
                }
            }

            private void getBoldSpan(SpannableString aSpannable, String aString, String aSubstring) {
                getBoldSpan(aSpannable, aString, aSubstring, !TextUtils.isEmpty(aSubstring) && aString.contains(aSubstring));
            }

            private void getBoldSpan(SpannableString aSpannable, String aString, String aSubstring, boolean applies) {
                if (applies) {
                    aSpannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), aString.indexOf(aSubstring), aString.indexOf(aSubstring) + aSubstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            private class FollowUnfollowListener extends BaseNeckRequestListener<Boolean> {

                private FeedModel feed;

                public FollowUnfollowListener(FeedModel feed){
                    this.feed = feed;
                }

                @Override
                public void onRequestError(BaseNeckRequestException.Error error) {
                    MessageUtil.showError(getBaseActivity(), error.getMessage(),
                            getBaseActivity().getCroutonsHolder());
                    hideProgress();
                    mFollowUser.setEnabled(true);
                }

                @Override
                public void onRequestSuccessfull(Boolean success) {
                    hideProgress();
                    mFollowUser.setEnabled(true);
                    setUpSubscribeBtn(!mFollowUser.isActivated());
                    feed.setFollowedByUser(mFollowUser.isActivated());
                }

                @Override
                public void onRequestException(SpiceException exception) {
                    MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                            getBaseActivity().getCroutonsHolder());
                    hideProgress();
                    mFollowUser.setEnabled(true);
                }
            }

            private void setUpSubscribeBtn(boolean isSubscriptedActiveUser) {
                if (isSubscriptedActiveUser) {
                    mFollowUser.setActivated(true);
                    mFollowUser.setText(R.string.following);
                } else {
                    mFollowUser.setActivated(false);
                    mFollowUser.setText(R.string.follow);
                }
            }

        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getFeedId();
        }
    }

    private ItemModel createPreloadedItem(FeedModel feed) {
        ItemModel item = new ItemModel();
        OwnerModel owner = new OwnerModel();

        item.setName(feed.getItemName());
        item.setPictureUrl(feed.getItemPicture());
        item.setStatus(feed.getItemStatus());

        owner.setId(feed.getItemOwnerId());
        owner.setPictureUrl(feed.getItemOwnerPicture());
        owner.setUsername(feed.getItemOwnerName());
        owner.setLocationName(feed.getItemOwnerLocationName());
        item.setUser(owner);

        // initialize fields
        item.setDescription("");
        item.setGroups(new ArrayList<GroupModel>());
        item.setPrice(0D);
        item.setUrl("");
        item.setPublishedTime(FormatUtils.timeAgo(feed.getCreationDate() / 1000));

        return item;
    }

    private static ArrayList<FeedModel> getMyItems(List<FeedModel> feedList) {
        ArrayList<FeedModel> newFeeds = new ArrayList<>();
        Iterator<FeedModel> aIterator = feedList.iterator();
        while (aIterator.hasNext()) {
            FeedModel aFeed = aIterator.next();
            if (aFeed.getAction() == Action.ITEM_BANNED || aFeed.getAction() == Action.ITEM_APROVED
                    || aFeed.getAction() == Action.ITEM_REJECTED || aFeed.getAction() == Action.ITEM_EXPIRE
                    || aFeed.getAction() == Action.ITEM_ABOUT_TO_EXPIRE || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_DUPLICATED
                    || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_PICTURES || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_SERVICES
                    || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_MAKEUP || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_ANIMALS
                    || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_COMMISSION || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_STYLE
                    || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_PRICE || aFeed.getAction() == Action.ITEM_DELETED_FROM_MODERATION_FORBIDDEN) {
                newFeeds.add(aFeed);
            }
        }
        return newFeeds;
    }

    private class FeedReadRequestListener extends BaseNeckRequestListener<Boolean> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
        }

        @Override
        public void onRequestSuccessfull(Boolean t) {
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Logger.error(exception.getMessage());
        }

    }
}
