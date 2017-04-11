package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.model.ConversationModel.Message;
import com.ebay.park.model.ItemModel;
import com.ebay.park.requests.AcceptNegotiationRequest;
import com.ebay.park.requests.BuyItemDirectlyRequest;
import com.ebay.park.requests.CancelNegotiationRequest;
import com.ebay.park.requests.ConversationRequest;
import com.ebay.park.requests.ItemDetailRequest;
import com.ebay.park.requests.ListConversationsByItem;
import com.ebay.park.requests.SendChatRequest;
import com.ebay.park.responses.ConversationListResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
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
 * Chat screen see and send messages.
 *
 * @author federico.perez
 */
public class ChatFragment extends BaseListFragment<ConversationModel> {

    public static final long NEW_CONVERSATION = -123L;

    private static final String ROLE = "role";
    private static final String CONVERSATION_KEY = "conv";
    private static final String CONVERSATION_ID_KEY = "convID";
    private static final String ITEM_KEY = "item";

    private Handler mHandler;

    private ListView mMessageList;
    private ImageView mItemImage;
    private TextViewDemi mItemTitle;
    private EditTextBook mMessageEditText;
    private View mSendButton;
    private View mAcceptNegotiationButton;
    private View mNewNegotiationButton;
    private View mChatCancelled;
    private TextViewDemi mChatCancelledTitle;
    private TextViewBook mChatCancelledMessage;
    private MessageAdapter mAdapter;
    private TextViewBook mPublishedPrice;
    private ConversationModel mConversation;
    private long mItemId;
    private ItemModel mItem;
    private long mConversationId;
    private String mRole;
    private Double mItemPrice = new Double(0D);
    private Boolean mItemSold = false;
    private MenuItem mCancelChat;
    private Boolean mItemDeleted = false;
    private Boolean mAutomaticUpdate = false;
    protected ProgressBar mProgressbar;

    /**
     * Get a instance of {@link ChatFragment} fo
     * r a specific conversation.
     *
     * @param conversation the conversation.
     */
    public static ChatFragment forConversation(ConversationModel conversation, String role) {
        ChatFragment fragment = new ChatFragment();
        if (conversation != null) {
            Bundle args = new Bundle();
            args.putSerializable(CONVERSATION_KEY, conversation);
            args.putLong(ITEM_KEY, conversation.getItemId());
            args.putString(ROLE, role);
            fragment.setArguments(args);
        }
        return fragment;
    }

    /**
     * Get a instance of {@link ChatFragment} for a new conversation.
     *
     * @param itemId the item associated to the conversation.
     */
    public static ChatFragment forNewConversation(long itemId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(ITEM_KEY, itemId);
        args.putString(ROLE, Constants.ROLE_BUYER);
        fragment.setArguments(args);
        return fragment;
    }

    public static ChatFragment forConversationId(long conversationId, long itemId, String role) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(CONVERSATION_ID_KEY, conversationId);
        args.putLong(ITEM_KEY, itemId);
        args.putString(ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    private Runnable updater = new Runnable() {

        @Override
        public void run() {
            if (mConversation != null) {
                SwrveSDK.event(SwrveEvents.R2S_CHAT_ATTEMPT);
                mAutomaticUpdate = true;
                mSpiceManager.execute(new ConversationRequest(mConversation.getConversationId()), new MessageListener());
                mHandler.postDelayed(this, Constants.CHAT_UPDATE_INTERVAL);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (!getArguments().containsKey(ITEM_KEY)) {
                throw new IllegalStateException("Chat fragment called with no item id");
            }
            mConversation = (ConversationModel) getArguments().getSerializable(CONVERSATION_KEY);
            mConversationId = getArguments().getLong(CONVERSATION_ID_KEY, -1);
            mItemId = getArguments().getLong(ITEM_KEY);
            mRole = getArguments().getString(ROLE);

            mHandler = new Handler();

        } catch (IllegalStateException e) {
            getBaseActivity().finish();
        } catch (Exception e) {
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
            getBaseActivity().finish();
        }

        setHasOptionsMenu(true);
        ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_VISIT_CHAT);
        SwrveSDK.event(SwrveEvents.R2S_CHAT_BEGIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat, getContainerLayout(inflater, container, savedInstanceState),
                true);
    }

    @Override
    public void onViewCreated(View baseview, Bundle savedInstanceState) {
        super.onViewCreated(baseview, savedInstanceState);
        mMessageList = (ListView) baseview.findViewById(R.id.chat_list);
        mMessageList.setAdapter(mAdapter);

        mChatCancelled = baseview.findViewById(R.id.ly_cancelled);
        mChatCancelledTitle = (TextViewDemi) baseview.findViewById(R.id.tv_canceller_message_title);
        mChatCancelledMessage = (TextViewBook) baseview.findViewById(R.id.tv_canceller_message_hint);

        mItemImage = (ImageView) baseview.findViewById(R.id.chat_item_img);
        mItemTitle = (TextViewDemi) baseview.findViewById(R.id.chat_item_title);
        mMessageEditText = (EditTextBook) baseview.findViewById(R.id.chat_message_et);
        mPublishedPrice = (TextViewBook) baseview.findViewById(R.id.chat_published_price);

        mProgressbar = (ProgressBar) baseview.findViewById(R.id.progress_bar);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSendButton.setEnabled(!TextUtils.isEmpty(s.toString()));
            }
        });
        mMessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mSendButton.setEnabled(!TextUtils.isEmpty(mMessageEditText.getText().toString()));
            }
        });

        baseview.findViewById(R.id.chat_item_detail).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mItemId >= 0 && !mItemDeleted) {
                    ParkApplication.sItemTapped = mItem;
                    if(mItem != null)
                    {
                        if (mItem.getPictures() != null) {
                            // Set picture as the principal one
                            ParkApplication.sItemTapped.setPictureUrl(mItem.getPictures().get(0));
                        }
                    }
                    ScreenManager.showItemDetailActivity(getBaseActivity(), mItemId, null, null);
                }
            }
        });

        mSendButton = baseview.findViewById(R.id.chat_send);
        mSendButton.setEnabled(false);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mMessageEditText.getText().toString().trim())){
                    final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(),
                            R.string.warning, R.string.message_chat_rule)
                            .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                } else {
                    SwrveSDK.event(SwrveEvents.MESSAGE_SEND_ATTEMPT);
                    sendMessage();
                }
            }
        });

        mAcceptNegotiationButton = baseview.findViewById(R.id.btn_chat_accept_negotiation);
        mAcceptNegotiationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversation != null || mItem != null) {
                    String price;
                    if (mConversation == null) {
                        price = FormatUtils.formatPrice(mItem.getPrice());
                    } else {
                        if (mRole.equals(Constants.ROLE_SELLER)) {
                            price = FormatUtils.formatPrice(mConversation.getCurrentPriceProposedByBuyer());
                        } else {
                            price = FormatUtils.formatPrice(mConversation.getCurrentPriceProposedBySeller());
                        }
                    }

                    final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(),
                            String.format(getString(R.string.accept_negotiation_confirm), price))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    acceptNegotiation();
                                }
                            })
                            .setNegativeButton(R.string.no, null).create();

                    if (DeviceUtils.isDeviceLollipopOrHigher()){
                        dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
                    } else {
                        dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
                    }

                    dialog.show();
                }
            }
        });

        mAcceptNegotiationButton = baseview.findViewById(R.id.btn_chat_accept_negotiation);

        mNewNegotiationButton = baseview.findViewById(R.id.btn_chat_new_negotiation);
        mNewNegotiationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversation ==null){
                    ConversationModel conversationAux = new ConversationModel();
                    // New conversation
                    conversationAux.setConversationId(-1);
                    conversationAux.setHasOffer(false);
                    conversationAux.setUsername(mItem.getUser().getUsername());
                    conversationAux.setCurrentPriceProposedBySeller(mItemPrice);
                    conversationAux.setItemId(mItemId);
                    ScreenManager.showNewOfferFragment(getBaseActivity(), conversationAux, mRole);
                } else {
                    ScreenManager.showNewOfferFragment(getBaseActivity(), mConversation, mRole);
                }
            }
        });

    }

    protected void showProgressBar(){
        mProgressbar.setVisibility(View.VISIBLE);
        mIsLoading = true;
    }

    protected void hideProgressBar(){
        mProgressbar.setVisibility(View.GONE);
        mIsLoading = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mCancelChat = menu.findItem(R.id.action_cancel_chat);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            SwrveSDK.event(SwrveEvents.R2S_CHAT_CANCEL);
        }
        if (item.getItemId() == R.id.action_cancel_chat) {
            showCancelConfirmation();
            return true;
        }
        if (item.getItemId() == R.id.action_show_rates) {
            ScreenManager.showRateListActivity(getBaseActivity(), (mConversation !=null) ? mConversation.getUsername() : mItem.getUser().getUsername());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCancelConfirmation() {
        final AlertDialog dialog = DialogUtils.getDialogWithField(getBaseActivity(),
                R.string.cancel_negotiation_confirm, R.string.chat_cancellation_hint)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.no, null).create();

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
        } else {
            dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
        }

        dialog.show();

        final EditText editCancelReason = DialogUtils.getDialogField(dialog);
        InputFilter[] editFilters = editCancelReason.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.LengthFilter(160);
        editCancelReason.setFilters(newFilters);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNegotiation(editCancelReason.getText().toString());
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle(R.string.negotiation);
        disableRefreshSwipe();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromServer();
        mHandler.postDelayed(updater, Constants.CHAT_UPDATE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(updater);
    }

    @Override
    protected ModelAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void initializeAdapter() {
        mAdapter = new MessageAdapter(getBaseActivity());
    }

    private void logAcceptOfferEventAndProperty(){
        ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_OFFER_ACCEPTED);

        Map<String, String> attributes = new HashMap<>();
        attributes.put(SwrveEvents.PURCHASED_ITEM, mItem.getCategory().getName());
        attributes.put(SwrveEvents.PURCHASED_ITEM + "." + mItem.getCategory().getName()
                .replace(" ", "-"), mItem.getName());
        SwrveSDK.userUpdate(attributes);
    }

    private void acceptNegotiation() {
        toggleNegotiation(false);
        showProgressBar();
        if (mConversation != null) {
            mSpiceManager.execute(new AcceptNegotiationRequest(mConversation.getConversationId()),
                    new AcceptNegotiationListener());
        } else {
            mSpiceManager.execute(new BuyItemDirectlyRequest(mItemId), new BuyDirectlyListener());
        }
    }

    private void cancelNegotiation(String aCancelReason) {
        toggleNegotiation(false);
        showProgressBar();
        if (mConversation != null) {
            mSpiceManager.execute(new CancelNegotiationRequest(mConversation.getConversationId(), aCancelReason),
                    new CancelNegotiationListener(aCancelReason));
        } else {
            getBaseActivity().onBackPressed();
        }
    }

    private void toggleNegotiation(boolean state) {
        mNewNegotiationButton.setEnabled(state);
    }

    private void sendMessage() {
        if (!Validations.validateChatMessage(mMessageEditText)) {
            MessageUtil.showError(getBaseActivity(), String.format(getString(R.string.chat_invalid_message), Constants.CHAT_MIN_LENGHT,
                    Constants.CHAT_MAX_LENGHT), getBaseActivity().getCroutonsHolder());
            return;
        }
        final String messageText = mMessageEditText.getText().toString();
        mMessageEditText.setEnabled(false);
        showProgressBar();
        mSpiceManager.execute(new SendChatRequest(mItemId, (mConversation != null) ? mConversation.getConversationId()
                : NEW_CONVERSATION, messageText), new SendMessageListener());
    }

    private void updateUI(ConversationModel conversation) {
        if (getActivity() != null) {
            if (TextUtils.isEmpty(conversation.getItemPicture())) {
                conversation.setItemPicture(null);
            }
            Picasso.with(getBaseActivity()).load(conversation.getItemPicture()).fit().centerCrop().into(mItemImage);

            switch (conversation.getStatus()) {
                case CANCELLED:
                    mMessageEditText.setEnabled(false);
                    mSendButton.setEnabled(false);
                    toggleAcceptButton(false);
                    toggleCancelButton(false);
                    toggleNegotiation(false);
                    mAcceptNegotiationButton.setVisibility(View.VISIBLE);
                    mAcceptNegotiationButton.setEnabled(false);
                    break;
                case ACCEPTED:
                    toggleAcceptButton(false);
                    toggleNegotiation(false);
                    mAcceptNegotiationButton.setVisibility(View.GONE);
                    mNewNegotiationButton.setVisibility(View.GONE);
                    break;
                case OPEN:
                    toggleCancelButton(true);
                    if (isItemSold(conversation.getChats())){
                        if (mItem != null && mItem.getStatus().equals("ACTIVE")){
                            toggleNegotiation(true);
                            if (mRole.equals(Constants.ROLE_SELLER)) {
                                if (conversation.hasOffer()) {
                                    toggleAcceptButton(offerFromBuyer(conversation.getChats()));
                                    mAcceptNegotiationButton.setEnabled(true);
                                } else {
                                    mAcceptNegotiationButton.setVisibility(View.GONE);
                                }
                            } else {
                                toggleAcceptButton(true);
                                mAcceptNegotiationButton.setEnabled(true);
                            }
                        } else {
                            mAcceptNegotiationButton.setVisibility(View.VISIBLE);
                            mAcceptNegotiationButton.setEnabled(false);
                            toggleNegotiation(false);
                        }
                    } else {
                        toggleNegotiation(true);
                        if (mRole.equals(Constants.ROLE_SELLER)) {
                            if (conversation.hasOffer()) {
                                toggleAcceptButton(offerFromBuyer(conversation.getChats()));
                            }
                        } else {
                            toggleAcceptButton(true);
                        }
                    }
                    break;
                default:
                    toggleNegotiation(false);
                    break;
            }
        }
    }

    private boolean isItemSold(List<Message> chats) {
        for (int i = 0; i < chats.size(); i++) {
            Message message = chats.get(i);
            if (message.getAction().equals(Message.Action.MARKED_AS_SOLD)) {
                return true;
            }
        }
        return false;
    }

    private boolean offerFromBuyer(List<Message> chats) {
        for (int i = 0; i < chats.size(); i++) {
            Message message = chats.get(i);
            if (message.getAction().equals(Message.Action.OFFER)) {
                if (!message.getSenderUsername().equals(ParkApplication.getInstance().getUsername())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void toggleAcceptButton(Boolean canAccept) {
        mAcceptNegotiationButton.setVisibility(canAccept ? View.VISIBLE : View.GONE);
    }

    private void toggleCancelButton(Boolean canCancel){
        mCancelChat.setEnabled(canCancel);
    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.R2S_CHAT_CANCEL);
    }

    private class MessageAdapter extends ModelAdapter<Message> {

        private static final int TYPE_RETROCOMPATIBILITY = 0;
        private static final int TYPE_MESSAGE_SENT = 1;
        private static final int TYPE_MESSAGE_RECEIVED = 2;
        private static final int TYPE_ACCEPTED = 3;
        private static final int TYPE_CANCELLED = 4;
        private static final int TYPE_AUTOMATIC_MESSAGE = 5;

        private LayoutInflater mInflater;

        public MessageAdapter(Context context) {
            mInflater = (LayoutInflater) getBaseActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void merge(List<Message> aList) {
            reverseMerge(aList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                switch (getItemViewType(position)) {
                    case TYPE_MESSAGE_SENT:
                        convertView = mInflater.inflate(R.layout.item_chat_send, parent, false);
                        viewHolder.mMessageTime = (TextView) convertView.findViewById(R.id.chat_messagge_time);
                        viewHolder.mMessage = (TextView) convertView.findViewById(R.id.chat_messagge);
                        viewHolder.mPrice = (TextView) convertView.findViewById(R.id.chat_price_offered);
                        break;
                    case TYPE_MESSAGE_RECEIVED:
                        convertView = mInflater.inflate(R.layout.item_chat_received, parent, false);
                        viewHolder.mMessageTime = (TextView) convertView.findViewById(R.id.chat_messagge_time);
                        viewHolder.mProfileImg = (ImageView) convertView.findViewById(R.id.chat_profile_img);
                        viewHolder.mMessageSender = (TextView) convertView.findViewById(R.id.chat_messagge_buyer);
                        viewHolder.mMessage = (TextView) convertView.findViewById(R.id.chat_messagge);
                        viewHolder.mPrice = (TextView) convertView.findViewById(R.id.chat_price_offered);
                        break;
                    case TYPE_ACCEPTED:
                        convertView = mInflater.inflate(R.layout.item_chat_accepted, parent, false);
                        viewHolder.mMessage = (TextView) convertView.findViewById(R.id.chat_messagge);
                        viewHolder.mHint = (TextView) convertView.findViewById(R.id.chat_messagge_hint);
                        break;
                    case TYPE_CANCELLED:
                        convertView = mInflater.inflate(R.layout.item_chat_cancelled, parent, false);
                        break;
                    case TYPE_AUTOMATIC_MESSAGE:
                    case TYPE_RETROCOMPATIBILITY:
                        if (ParkApplication.getInstance().getUsername()
                                .equals(getItem(position).getSenderUsername())){
                            convertView = mInflater.inflate(R.layout.item_chat_send, parent, false);
                        } else {
                            convertView = mInflater.inflate(R.layout.item_chat_received, parent, false);
                            viewHolder.mProfileImg = (ImageView) convertView.findViewById(R.id.chat_profile_img);
                            viewHolder.mMessageSender = (TextView) convertView.findViewById(R.id.chat_messagge_buyer);
                        }
                        viewHolder.mMessage = (TextView) convertView.findViewById(R.id.chat_messagge);
                        viewHolder.mPrice = (TextView) convertView.findViewById(R.id.chat_price_offered);
                        viewHolder.mMessageTime = (TextView) convertView.findViewById(R.id.chat_messagge_time);
                        break;
                    default:
                        break;
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Message message = getItem(position);

            switch (getItemViewType(position)) {
                case TYPE_ACCEPTED:
                    viewHolder.mMessage.setText(message.getComment());
                    viewHolder.mHint.setText(message.getHint());
                    mPublishedPrice.setText(String.format(getString(R.string.accepted_price),FormatUtils.formatPrice(message.getOfferedPrice())));
                    mPublishedPrice.setTextColor(getResources().getColor(R.color.Green));
                    mItemSold = true;
                    return convertView;
                case TYPE_CANCELLED:
                    showChatCancelledAlert(message.getComment(), message.getHint());
                    if (!mItemPrice.equals(0D) && !mItemSold) {
                        mPublishedPrice.setText(String.format(getString(R.string.published_price),
                                FormatUtils.formatPrice(mItemPrice)));
                    }
                    return convertView;
                case TYPE_MESSAGE_RECEIVED:
                    String picture;
                    if (mRole.equals(Constants.ROLE_BUYER)) {
                        picture = mConversation.getSellerThumbnail();
                    } else {
                        picture = mConversation.getBuyerThumbnail();
                    }
                    if (TextUtils.isEmpty(picture)) {
                        picture = null;
                    }
                    Picasso.with(getBaseActivity()).load(picture).placeholder(R.drawable.avatar_chat_ph_image_fit).fit().centerCrop()
                            .into(viewHolder.mProfileImg);
                    if (message.getOfferedPrice() == 0) {
                        viewHolder.mMessage.setText(message.getComment());
                        viewHolder.mPrice.setVisibility(View.GONE);
                    } else {
                        viewHolder.mMessage.setText(message.getComment().toUpperCase());
                        viewHolder.mPrice.setText(FormatUtils.formatPrice(message.getOfferedPrice()));
                        viewHolder.mPrice.setVisibility(View.VISIBLE);
                    }
                    viewHolder.mMessageTime.setText(FormatUtils.timeAgo(message.getPostTime()));
                    viewHolder.mMessageSender.setText(message.getSenderUsername());
                    if (!mItemPrice.equals(0D) && !mItemSold) {
                        mPublishedPrice.setText(String.format(getString(R.string.published_price),
                                FormatUtils.formatPrice(mItemPrice)));
                    }
                    return convertView;
                case TYPE_MESSAGE_SENT:
                    if (message.getOfferedPrice() == 0) {
                        viewHolder.mMessage.setText(message.getComment());
                        viewHolder.mPrice.setVisibility(View.GONE);
                    } else {
                        viewHolder.mMessage.setText(message.getComment().toUpperCase());
                        viewHolder.mPrice.setText(FormatUtils.formatPrice(message.getOfferedPrice()));
                        viewHolder.mPrice.setVisibility(View.VISIBLE);
                    }
                    viewHolder.mMessageTime.setText(FormatUtils.timeAgo(message.getPostTime()));
                    if (!mItemPrice.equals(0D) && !mItemSold) {
                        mPublishedPrice.setText(String.format(getString(R.string.published_price),
                                FormatUtils.formatPrice(mItemPrice)));
                    }
                    return convertView;
                case TYPE_AUTOMATIC_MESSAGE:
                    //Item name it is found manually otherwise we would have to wait until BE response
                    int charactersBeforeItemName = 11;
                    String charactersAfterItemName = ". Muchas gracias";
                    String itemName = message.getComment().substring(charactersBeforeItemName,
                            message.getComment().indexOf(charactersAfterItemName));

                    SpannableString spannableString = new SpannableString(message.getComment());
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                            message.getComment().indexOf(itemName),
                            message.getComment().indexOf(itemName) + itemName.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.mMessage.setText(spannableString);
                    handleMessage(position, viewHolder, message);
                    return convertView;
                case TYPE_RETROCOMPATIBILITY:
                    viewHolder.mMessage.setText(message.getComment());
                    handleMessage(position, viewHolder, message);
                    return convertView;
                default:
                    return convertView;
            }
        }

        private void handleMessage(int position, ViewHolder viewHolder, Message message) {
            viewHolder.mPrice.setVisibility(View.GONE);
            viewHolder.mMessageTime.setText(FormatUtils.timeAgo(message.getPostTime()));

            if (!ParkApplication.getInstance().getUsername()
                    .equals(getItem(position).getSenderUsername())) {
                viewHolder.mMessageSender.setText(message.getSenderUsername());

                String photo;
                if (mRole.equals(Constants.ROLE_BUYER)) {
                    photo = mConversation.getSellerThumbnail();
                } else {
                    photo = mConversation.getBuyerThumbnail();
                }
                if (TextUtils.isEmpty(photo)) {
                    photo = null;
                }
                Picasso.with(getBaseActivity()).load(photo).placeholder(R.drawable.avatar_chat_ph_image_fit).fit().centerCrop()
                        .into(viewHolder.mProfileImg);
            }
        }

        @Override
        public int getViewTypeCount() {
            return 7;
        }

        @Override
        public int getItemViewType(int position) {
            Message.Action action = getItem(position).getAction();
            if (action != null) {
                switch (action) {
                    case ACCEPTED:
                        return TYPE_ACCEPTED;
                    case CANCELLED:
                        return TYPE_CANCELLED;
                    case CHAT:
                        boolean sent = ParkApplication.getInstance().getUsername()
                                .equals(getItem(position).getSenderUsername());
                        return sent ? TYPE_MESSAGE_SENT : TYPE_MESSAGE_RECEIVED;
                    case OFFER:
                        sent = ParkApplication.getInstance().getUsername().equals(getItem(position).getSenderUsername());
                        return sent ? TYPE_MESSAGE_SENT : TYPE_MESSAGE_RECEIVED;
                    case MARKED_AS_SOLD:
                        return TYPE_AUTOMATIC_MESSAGE;
                    default:
                        return TYPE_MESSAGE_SENT;
                }
            } else {
                return TYPE_RETROCOMPATIBILITY;
            }
        }

        class ViewHolder {
            TextView mHint;
            ImageView mProfileImg;
            TextView mMessage;
            TextView mPrice;
            TextView mMessageSender;
            TextView mMessageTime;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getChatId();
        }
    }

    private void showChatCancelledAlert(String cancellerTitle, String cancellerMessage) {
        mMessageList.setPadding(0, 0, 0, 250);
        mChatCancelled.setVisibility(View.VISIBLE);
        mChatCancelledTitle.setText(cancellerTitle);
        mChatCancelledMessage.setText(cancellerMessage);
    }

    private class MessageListener extends ListListener {

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.R2S_CHAT_FAIL, payload);
            hideProgressBar();
            if (error.getErrorCode() == ResponseCodes.DeleteItem.ITEM_ALREADY_DELETED) {
                if (ParkApplication.sItemTapped != null) {
                    ChatFragment.this.mItem = ParkApplication.sItemTapped;
                    ParkApplication.sItemTapped = null;
                    preparePreloadedItem();
                    updateItemView();
                    mItemDeleted = true;
                }
            }
            mAutomaticUpdate = false;
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(ConversationModel conversation) {
            super.onRequestSuccessfull(conversation);
            SwrveSDK.event(SwrveEvents.R2S_CHAT_SUCCESS);
            ChatFragment.this.mConversation = conversation;
            if (mAutomaticUpdate){
                mSpiceManager.execute(new ItemDetailRequest(mItemId), new ItemListener());
            } else {
                mNewNegotiationButton.setVisibility(View.VISIBLE);
                updateUI(conversation);
            }
        }
    }

    private void updateItemView(){
        if (!mItem.getPictures().isEmpty()) {
            Picasso.with(getBaseActivity()).load(mItem.getPictures().get(0))
                    .placeholder(R.drawable.img_placeholder).fit().centerCrop().into(mItemImage);
        }
        mItemTitle.setText(mItem.getName());
        toggleCancelButton(false);
        mAcceptNegotiationButton.setVisibility(View.VISIBLE);
        mAcceptNegotiationButton.setEnabled(false);
        mNewNegotiationButton.setVisibility(View.VISIBLE);
        mNewNegotiationButton.setEnabled(false);
        mMessageEditText.setEnabled(false);
        mSendButton.setEnabled(false);
    }

    private void preparePreloadedItem(){
        // pictures list is null in the list
        if (mItem.getPictureUrl() == null){ // Item without picture
            mItem.setPictures(new ArrayList<String>());
        } else { // Add the principal picture to the preloaded pictures array
            List<String> pictures = new ArrayList<>();
            pictures.add(mItem.getPictureUrl());
            mItem.setPictures(pictures);
        }

        // Location name is null in the list
        // Set owner's location to the preloaded item
        mItem.setLocationName(mItem.getUser().getLocationName());
    }

    private class SendMessageListener extends BaseNeckRequestListener<Long> {

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.MESSAGE_SEND_FAIL, payload);
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
            mMessageEditText.setEnabled(true);
        }

        @Override
        public void onRequestSuccessfull(Long chatId) {
            SwrveSDK.event(SwrveEvents.MESSAGE_SEND_SUCCESS);
            mMessageEditText.setText("");
            mMessageEditText.setEnabled(true);
            if (mConversation == null) {
                mSpiceManager.execute(ListConversationsByItem.searchExistingNegotiations(mItemId),
                        new ExistingConversationListener());
            } else {
                getDataFromServer();
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.MESSAGE_SEND_FAIL, payload);
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            mMessageEditText.setEnabled(true);
        }
    }

    private class ExistingConversationListener extends BaseNeckRequestListener<ConversationListResponse> {

        @Override
        public void onRequestError(Error error) {
            hideProgressBar();
        }

        @Override
        public void onRequestSuccessfull(ConversationListResponse response) {
            List<ConversationModel> conversations = response.getItems();
            if (conversations.size() > 0) {
                mConversation = conversations.get(0);
                getDataFromServer();
            }
            hideProgressBar();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Logger.error(exception.getMessage());
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

    }

    private class AcceptNegotiationListener extends BaseNeckRequestListener<Boolean> {

        @Override
        public void onRequestError(Error error) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Boolean t) {
            hideProgressBar();
            toggleNegotiation(false);
            logAcceptOfferEventAndProperty();
            MessageUtil.showSuccess(getBaseActivity(), getString(R.string.chat_accepted_successfully),
                    getBaseActivity().getCroutonsHolder());
            getDataFromServer();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

    }

    private class CancelNegotiationListener extends BaseNeckRequestListener<Boolean> {

        private String cancelReason;

        public CancelNegotiationListener(String reason){
            cancelReason = reason;
        }

        @Override
        public void onRequestError(Error error) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Boolean t) {
            hideProgressBar();
            toggleNegotiation(false);
            mMessageEditText.setEnabled(false);
            mSendButton.setEnabled(false);
            showChatCancelledAlert(getString(R.string.cancel_negotiation_title),
                    (TextUtils.isEmpty(cancelReason) ? getString(R.string.cancel_negotiation_no_reason) : cancelReason));
            getDataFromServer();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

    }

    private class ItemListener extends BaseNeckRequestListener<ItemModel> {

        @Override
        public void onRequestError(Error error) {
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(ItemModel item) {
            mItem = item;
            if (mAutomaticUpdate) {
                mAutomaticUpdate = false;
            } else {
                mNewNegotiationButton.setVisibility(View.VISIBLE);
            }
            if (mConversation != null) {
                updateUI(mConversation);
            } else {
                toggleAcceptButton(mRole == Constants.ROLE_BUYER);
                toggleCancelButton(false);
            }
            if (!item.getPictures().isEmpty()) {
                Picasso.with(getBaseActivity()).load(item.getPictures().get(0))
                        .placeholder(R.drawable.img_placeholder).fit().centerCrop().into(mItemImage);
            }
            mItemTitle.setText(item.getName());
            mItemPrice = item.getPrice();
            if (mConversation == null || (mConversation !=null && mConversation.getStatus() != ConversationModel.Status.ACCEPTED)) {
                mPublishedPrice.setText(String.format(getString(R.string.published_price),
                        FormatUtils.formatPrice(mItemPrice)));
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }

    }

    private class BuyDirectlyListener extends BaseNeckRequestListener<Long> {

        @Override
        public void onRequestError(Error error) {
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(Long conversationId) {
            logAcceptOfferEventAndProperty();
            SwrveSDK.event(SwrveEvents.R2S_CHAT_ATTEMPT);
            mSpiceManager.execute(new ConversationRequest(conversationId), new MessageListener());
        }

        @Override
        public void onRequestException(SpiceException exception) {
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.warn(exception.getMessage());
        }

    }

    @Override
    protected void getDataFromServer() {
        showProgressBar();
        if (mConversation != null || mConversationId != -1) {
            SwrveSDK.event(SwrveEvents.R2S_CHAT_ATTEMPT);
            long id = (mConversation != null) ? mConversation.getConversationId() : mConversationId;
            mSpiceManager.execute(new ConversationRequest(id), new MessageListener());
        } else {
            mSpiceManager.execute(ListConversationsByItem.searchExistingNegotiations(mItemId),
                    new ExistingConversationListener());
        }
        mSpiceManager.execute(new ItemDetailRequest(mItemId), new ItemListener());
    }

    @Override
    protected void clearData() {
        mAdapter.clear();
    }

    @Override
    protected void loadData(ConversationModel aResponse) {
        mAdapter.merge(aResponse.getChats());
        mMessageList.setSelection(mAdapter.getCount() - 1);
        hideProgressBar();
    }

}