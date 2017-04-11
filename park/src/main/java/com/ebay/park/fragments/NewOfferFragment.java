package com.ebay.park.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.requests.SendOfferRequest;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewOfferFragment extends BaseFragment {

    public static final String CHAT = "chat";
    public static final String ROLE = "role";
    public static final long NEW_CONVERSATION = -123L;

    private TextViewBook mHint;
    private TextViewDemi mQuestion;
    private EditTextBook mPrice;
    private TextViewBook mError;
    private ConversationModel mChat;
    private String mRole;

    public static NewOfferFragment forConversation(ConversationModel conversation, String role){
        NewOfferFragment fragment = new NewOfferFragment();
        Bundle args = new Bundle();
        args.putSerializable(CHAT, conversation);
        args.putString(ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    public NewOfferFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChat = (ConversationModel) getArguments().getSerializable(CHAT);
        mRole = getArguments().getString(ROLE);
        setHasOptionsMenu(true);
        setTitle(R.string.your_offer);
        SwrveSDK.event(SwrveEvents.R2S_BID_BEGIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseView = inflater.inflate(R.layout.fragment_new_offer,
                (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
        return baseView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disableRefreshSwipe();

        mHint = (TextViewBook) view.findViewById(R.id.new_offer_hint);
        mQuestion = (TextViewDemi) view.findViewById(R.id.new_offer_question);
        mPrice = (EditTextBook) view.findViewById(R.id.new_offer_price);
        mPrice.addTextChangedListener(new PriceTextWatcher(mPrice));
        mPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mPrice.post(new Runnable() {
                        @Override
                        public void run() {
                            KeyboardHelper.show(getBaseActivity(), mPrice, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            }
        });

        mError = (TextViewBook) view.findViewById(R.id.new_offer_error);

        if (mChat.hasOffer()){
            if (offerFromOther(mChat.getChats())){
                if (mRole.equals(Constants.ROLE_BUYER)){
                    mHint.setText(String.format(getString(R.string.re_offer_hint), mChat.getUsername(),
                            FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller())));
                    mPrice.setText(FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller()).replace("$", "").replace(",",""));
                } else {
                    mHint.setText(String.format(getString(R.string.re_offer_hint), mChat.getUsername(),
                            FormatUtils.formatPrice(mChat.getCurrentPriceProposedByBuyer())));
                    mPrice.setText(FormatUtils.formatPrice(mChat.getCurrentPriceProposedByBuyer()).replace("$", "").replace(",",""));
                }
                mQuestion.setText(getString(R.string.re_offer_question));
            } else {
                if (mRole.equals(Constants.ROLE_BUYER)){
                    mHint.setText(String.format(getString(R.string.new_offer_hint), mChat.getUsername(),
                            FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller())));
                    mQuestion.setText(getString(R.string.new_offer_question));
                } else {
                    mHint.setText(String.format(getString(R.string.my_re_offer_hint),
                            FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller())));
                    mQuestion.setText(getString(R.string.my_re_offer_question));
                }
                mPrice.setText(FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller()).replace("$", "").replace(",",""));
            }
        } else {
            if (mRole.equals(Constants.ROLE_BUYER)){
                mHint.setText(String.format(getString(R.string.new_offer_hint), mChat.getUsername(),
                        FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller())));
                mQuestion.setText(getString(R.string.new_offer_question));
            } else {
                mHint.setText(String.format(getString(R.string.my_offer_hint),
                        FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller())));
                mQuestion.setText(getString(R.string.my_offer_question));
            }
            mPrice.setText(FormatUtils.formatPrice(mChat.getCurrentPriceProposedBySeller()).replace("$", "").replace(",",""));
        }

        mPrice.requestFocus();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_update_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                SwrveSDK.event(SwrveEvents.R2S_BID_CANCEL);
                getBaseActivity().onSupportNavigateUp();
                return true;
            case R.id.action_ok:
                if (!Validations.validatePrice(mPrice)) {
                    mError.setVisibility(View.VISIBLE);
                    mPrice.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
                } else {
                    showConfirmationDialog();
                }
                return true;
            case R.id.action_cancel:
                SwrveSDK.event(SwrveEvents.R2S_BID_CANCEL);
                getBaseActivity().onSupportNavigateUp();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        String dialogQuestion;
        if (mChat.hasOffer() && offerFromOther(mChat.getChats())){
            dialogQuestion = getString(R.string.re_offer_confirmation);
        } else {
            dialogQuestion = getString(R.string.new_offer_confirmation);
        }

        final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(),
                String.format(dialogQuestion, FormatUtils.formatPrice(Double.parseDouble(mPrice.getText().toString().replace("$","")))))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendNewOffer(mPrice);
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

    @Override
    public void onRefresh() {
    }

    private boolean offerFromOther(List<ConversationModel.Message> chats) {
        for (int i = 0; i < chats.size(); i++) {
            ConversationModel.Message message = chats.get(i);
            if (message.getAction().equals(ConversationModel.Message.Action.OFFER)) {
                if (!message.getSenderUsername().equals(ParkApplication.getInstance().getUsername())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.R2S_BID_CANCEL);
    }

    private class PriceTextWatcher implements TextWatcher {

        private EditText mPriceEditText;

        private PriceTextWatcher(EditTextBook priceEdit) {
            this.mPriceEditText = priceEdit;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mError.setVisibility(View.GONE);
            mPrice.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            if (s.toString().contains("$")) {
                mPriceEditText.removeTextChangedListener(this);
                s.delete(0, 1);
            }

            String text = s.toString();
            int length = text.length();

            if (!Pattern.matches("^(?!0[0-9])[0-9]{1,6}([.][0-9]{0,2})?$", text)) {
                if (length > 0) {
                    s.delete(length - 1, length);
                }
            }

            String userInput = "" + s.toString().replaceAll("[^\\d.]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);
            mPriceEditText.removeTextChangedListener(this);
            mPriceEditText.setText(cashAmountBuilder.toString());
            mPriceEditText.setTextKeepState("$" + cashAmountBuilder.toString());
            Selection.setSelection(mPriceEditText.getText(), cashAmountBuilder.toString().length() + 1);
            mPriceEditText.addTextChangedListener(this);
        }
    }

    private void sendNewOffer(EditTextBook priceEdit) {
        SwrveSDK.event(SwrveEvents.R2S_BID_ATTEMPT);
        double price = NumberUtils.toDouble(priceEdit.getText().toString().replace("$", ""));

        mSpiceManager.execute(new SendOfferRequest(mChat.getItemId(), (mChat.getConversationId() != -1) ? mChat.getConversationId()
                : NEW_CONVERSATION, price), new SendOfferListener());
    }

    private class SendOfferListener extends BaseNeckRequestListener<Long> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.R2S_BID_FAIL, payload);
            hideProgress();
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Long chatId) {
            SwrveSDK.event(SwrveEvents.R2S_BID_SUCCESS);
            ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_NEW_OFFER);
            getBaseActivity().onSupportNavigateUp();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.R2S_BID_FAIL, payload);
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }
    }
}
