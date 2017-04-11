package com.ebay.park.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.requests.ListConversationsByItem;
import com.ebay.park.requests.ListConversationsRequest;
import com.ebay.park.responses.ConversationListResponse;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Displays a list of conversations. Can display all conversations or
 * conversations of a specific item.
 *
 * @author federico.perez
 */
public class ConversationListFragment extends BaseListFragment<ConversationListResponse> {

    private static final String ITEM_ID = "itemId";
    private static final String ROLE_KEY = "role";
    private static final long NO_ITEM_ID = -123L;

    private Handler mHandler;

    private ListView mChatList;

    private String mRole;
    private long mItemId;

    private ConversationAdapter mAdapter;

    /**
     * Get fragment for conversations as a seller.
     */
    public static ConversationListFragment conversationsAsSeller() {
        ConversationListFragment fragment = new ConversationListFragment();
        Bundle args = new Bundle();
        args.putString(ROLE_KEY, Constants.ROLE_SELLER);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get fragment for conversations as a buyer.
     */
    public static ConversationListFragment conversationsAsBuyer() {
        ConversationListFragment fragment = new ConversationListFragment();
        Bundle args = new Bundle();
        args.putString(ROLE_KEY, Constants.ROLE_BUYER);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get fragment for all conversations as a seller on a particular item.
     *
     * @param itemId The item id.
     */
    public static ConversationListFragment forItemIdSelling(long itemId) {
        ConversationListFragment fragment = new ConversationListFragment();
        Bundle args = new Bundle();
        args.putLong(ITEM_ID, itemId);
        args.putString(ROLE_KEY, ListConversationsRequest.ROLE_SELLER);
        fragment.setArguments(args);
        return fragment;
    }

    private Runnable updater = new Runnable() {

        @Override
        public void run() {
            getDataFromServer();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (!getArguments().containsKey(ROLE_KEY)) {
                throw new IllegalStateException("Conversations list called with no role");
            }
            mRole = getArguments().getString(ROLE_KEY);
            mItemId = getArguments().getLong(ITEM_ID, NO_ITEM_ID);

            mHandler = new Handler();

        } catch (IllegalStateException e) {
            getBaseActivity().finish();
        } catch (Exception e) {
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
            getBaseActivity().finish();
        }

        ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_VISIT_LIST_CHAT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat_list,
                getContainerLayout(inflater, container, savedInstanceState), true);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        mChatList = (ListView) rootView.findViewById(R.id.chat_list);
        loadEmptyView(getBaseActivity(), (ViewGroup) rootView.findViewById(R.id.list_container));

        mChatList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final ConversationAdapter adapter = (ConversationAdapter) mChatList.getAdapter();
                if (adapter.getItem(position).getLastMessageTime() == 0) {
                    MessageUtil.showError(getBaseActivity(), "Negociaci�n incorrecta",
                            getBaseActivity().getCroutonsHolder());
                } else {
                    ScreenManager.showChatActivity(getBaseActivity(), adapter.getItem(position), -1, -1, mRole);
                }
            }
        });
        mChatList.setAdapter(mAdapter);
        mChatList.setOnScrollListener(new ListScroll());
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.negotiations);
        mHandler.postDelayed(updater, Constants.CHAT_UPDATE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(updater);
    }

    @Override
    public void onBackPressed() {
    }

    private class ConversationAdapter extends ModelAdapter<ConversationModel> {

        private Context mContext;

        public ConversationAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getConversationId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_conversation, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.load(getItem(position));
            return convertView;
        }

        private class ViewHolder {

            private View mItemContainer;
            private ImageView mItemThumb;
            private ImageView mProfileThumb;
            private TextViewDemi mUsername;
            private TextViewBook mMessage;
            private TextViewBook mDate;
            private ImageView mStatusSeparator;
            private TextViewBook mStatus;

            public ViewHolder(View vItem) {
                mItemContainer = vItem;
                mItemThumb = (ImageView) vItem.findViewById(R.id.chat_list_item_image);
                mProfileThumb = (ImageView) vItem.findViewById(R.id.chat_list_profile_image);
                mMessage = (TextViewBook) vItem.findViewById(R.id.chat_list_message_text);
                mUsername = (TextViewDemi) vItem.findViewById(R.id.chat_list_username);
                mDate = (TextViewBook) vItem.findViewById(R.id.chat_list_date);
                mStatus = (TextViewBook) vItem.findViewById(R.id.chat_list_status);
                mStatusSeparator = (ImageView) vItem.findViewById(R.id.chat_list_status_separator);
            }

            public void load(final ConversationModel aConversation) {
                if (TextUtils.isEmpty(aConversation.getItemPicture())) {
                    aConversation.setItemPicture(null);
                }
                Picasso.with(getBaseActivity()).load(aConversation.getItemPicture())
                        .placeholder(R.drawable.img_placeholder).fit().centerCrop().into(mItemThumb);

                if (Constants.ROLE_SELLER.equals(mRole) && !TextUtils.isEmpty(aConversation.getBuyerThumbnail())) {
                    loadProfileImage(aConversation.getBuyerThumbnail());
                } else if (Constants.ROLE_BUYER.equals(mRole) && !TextUtils.isEmpty(aConversation.getSellerThumbnail())) {
                    loadProfileImage(aConversation.getSellerThumbnail());
                }

                if (aConversation.getLastChatComment() != null) {
                    String aMessage = aConversation.getLastChatComment().getComment();
                    if (aConversation.getLastChatComment().getAction() == ConversationModel.Message.Action.OFFER){
                        aMessage = aMessage + " " + FormatUtils.formatPrice(aConversation.getLastChatComment().getOfferedPrice());
                    }
                    if (aConversation.getLastChatComment().getSenderUsername()
                            .equals(ParkApplication.getInstance().getUsername())) {
                        aMessage = getString(R.string.conversation_preffix_own_message) + aMessage;
                    }
                    mMessage.setText(aMessage);
                }

                mUsername.setText(aConversation.getUsername());
                mUsername.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ScreenManager.showProfileActivity(getBaseActivity(), aConversation.getUsername());
                    }
                });

                if (aConversation.getLastMessageTime() == 0) {
                    mDate.setText("");
                } else {
                    mDate.setText(FormatUtils.timeAgo(aConversation.getLastMessageTime()));
                }

//                if (aConversation.isNewChats()) {
//                    mItemContainer.setBackgroundColor(getResources().getColor(R.color.unread_feed));
//                } else {
//                    mItemContainer.setBackgroundColor(getResources().getColor(R.color.White));
//                }

                if (ConversationModel.Status.ACCEPTED.equals(aConversation.getStatus())
                        || ConversationModel.Status.CANCELLED.equals(aConversation.getStatus())) {
                    mStatusSeparator.setVisibility(View.VISIBLE);
                    mStatus.setVisibility(View.VISIBLE);
                    if (ConversationModel.Status.ACCEPTED.equals(aConversation.getStatus())) {
                        mStatusSeparator.setColorFilter(getResources().getColor(R.color.Green));
                        mStatus.setText(getString(R.string.offer_accepted));
                        mStatus.setTextColor(getResources().getColor(R.color.Green));
                    } else {
                        mStatusSeparator.setColorFilter(getResources().getColor(R.color.IndicatorOrange));
                        mStatus.setTextColor(getResources().getColor(R.color.IndicatorOrange));
                        mStatus.setText(getString(R.string.cancelled));
                    }
                } else {
                    mStatusSeparator.setVisibility(View.GONE);
                    mStatus.setVisibility(View.GONE);
                }
            }

            private void loadProfileImage(String aImageUrl) {
                if (TextUtils.isEmpty(aImageUrl)) {
                    aImageUrl = null;
                }
                Picasso.with(getBaseActivity()).load(aImageUrl).placeholder(R.drawable.avatar_medium_ph_image_fit).fit().centerCrop()
                        .into(mProfileThumb);

            }
        }
    }

    @Override
    protected ModelAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void initializeAdapter() {
        mAdapter = new ConversationAdapter(getBaseActivity());
    }

    @Override
    protected void getDataFromServer() {
        showProgress();
        mIsLoading = true;
        if (mItemId == NO_ITEM_ID) {
            long lastRequest = PreferencesUtil.getLastChatRequest(getBaseActivity(), mRole);
            mSpiceManager.execute(new ListConversationsRequest(page, PAGE_SIZE, lastRequest, mRole),
                    new ConversationListener());
            PreferencesUtil.setLastChatRequest(getBaseActivity(), mRole);
        } else {
            mSpiceManager.execute(
                    new ListConversationsByItem(page, PAGE_SIZE, mItemId, new Timestamp(new Date().getTime()).getTime(),
                            mRole), new ConversationListener());
        }
    }

    private class ConversationListener extends ListListener {
    }

    @Override
    protected void clearData() {
    }

    @Override
    protected void loadData(ConversationListResponse aResponse) {
        mIsLoading = false;
        if (aResponse.getItems() != null) {
            long fetchedItems = mAdapter.getCount() + aResponse.getItems().size();
            mLoadedAllItems = aResponse.getAmountTotalItemsFound() <= fetchedItems;
            mAdapter.merge(aResponse.getItems());
        }
        if (mAdapter.getCount() == 0) {
            mChatList.setEmptyView(mEmptyView);
            mEmptyMessage.setText(aResponse.getNoResultsMessage());
            mEmptyHint.setText(aResponse.getNoResultsHint());
        }
    }
}
