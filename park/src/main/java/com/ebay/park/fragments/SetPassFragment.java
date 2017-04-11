package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.requests.CheckValueRequest;
import com.ebay.park.requests.ForgotPassRequest;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

public class SetPassFragment extends BaseFragment {

    private EditTextBook mUsernameEditText;
    private TextViewBook mLoginFailedView;
    private TextView mRegister;
    private TextView mReadyTextView;
    private TextView mEmailSentTextView;
    private TextView mEnterEmailTextView;
    private TextViewBook mTermsLink;
    private LinearLayout mRegisterLy;
    private ButtonDemi mSendButton;
    Tracker mGAnalyticsTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        View baseview = inflater.inflate(R.layout.fragment_set_password, parentView, true);
        setTitle(R.string.set_password_title);

        mUsernameEditText = (EditTextBook) baseview.findViewById(R.id.et_username);

        mLoginFailedView = (TextViewBook) baseview.findViewById(R.id.tv_login_failed);
        mRegister = (TextViewBook) baseview.findViewById(R.id.tv_register);
        mTermsLink = (TextViewBook) baseview.findViewById(R.id.tvTermsLink);

        mRegisterLy = (LinearLayout) baseview.findViewById(R.id.ly_register);
        mReadyTextView = (TextView) baseview.findViewById(R.id.tv_ready);
        mEmailSentTextView = (TextView) baseview.findViewById(R.id.tv_email_sent);
        mEnterEmailTextView = (TextView) baseview.findViewById(R.id.tv_enter_email);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationScreen();
            }
        });
        mTermsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory.getTermsConditions());
            }
        });

        mSendButton = (ButtonDemi) baseview.findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    showProgress();
                    hideKeyboard();
                    mSpiceManager.execute(new ForgotPassRequest(mUsernameEditText.getText().toString()), new ForgotPassListener());
                }
            }
        });

        mUsernameEditText.setFilters(new InputFilter[]{Constants.EMAIL_FILTER});
        mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mLoginFailedView.setVisibility(View.GONE);
                    if (!hasFocus && !Validations.validateEmail(mUsernameEditText)) {
                        mLoginFailedView.setVisibility(View.VISIBLE);
                    } else {
                        mLoginFailedView.setVisibility(View.GONE);
                    }
                }
            }
        });

        return baseview;
    }

    private void showRegistrationScreen() {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        LoginFragment fragment = (LoginFragment) getBaseActivity().getSupportFragmentManager().findFragmentByTag(LoginFragment.TEST_TAG);
        fragment.onLoginPhone(false, mUsernameEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
        mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
    }

    private void hideKeyboard() {
        try {
            mSendButton.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        } catch (NullPointerException e) {
        } catch (ClassCastException e) {
        } catch (IllegalStateException e) {
        }
    }

    private class ForgotPassListener extends BaseNeckRequestListener<Boolean> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            hideProgress();

            if (error != null) {
                if(error.getErrorCode() == ResponseCodes.Login.NON_EXISTENT_EMAIL){
                    String[] messageArray = error.getMessage().split("\\.");
                    showResultsDialog(messageArray[0].substring(7) + "<br><br>" + messageArray[1]);
                } else {
                    mLoginFailedView.setText(error.getMessage());
                    mLoginFailedView.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        public void onRequestSuccessfull(Boolean t) {
            mReadyTextView.setVisibility(View.VISIBLE);
            mEmailSentTextView.setVisibility(View.VISIBLE);
            mEnterEmailTextView.setVisibility(View.GONE);
            mUsernameEditText.setVisibility(View.GONE);
            mRegisterLy.setVisibility(View.GONE);
            // Build and send "User recovered password" Event.
            mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Server action")
                    .setAction("User recovered password").setLabel("The recover password call succeed").build());
            mSendButton.setText(getString(R.string.text_ok));
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInput()) {
                         getFragmentManager().popBackStack();
                    }
                }
            });
            hideProgress();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            mLoginFailedView.setText(getString(R.string.recovery_email_fail));
			mLoginFailedView.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateInput() {
        boolean isInputValid = true;

        if (TextUtils.isEmpty(mUsernameEditText.getText())) {
            isInputValid = false;
            mLoginFailedView.setVisibility(View.VISIBLE);
        } else {
            if (!Validations.validateEmail(mUsernameEditText)) {
                mLoginFailedView.setVisibility(View.VISIBLE);
                isInputValid = false;
            } else {
                mLoginFailedView.setVisibility(View.GONE);
            }
        }

        return isInputValid;
    }


    private void showResultsDialog(String message) {
        final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.ooops,
                message)
                .setPositiveButton(R.string.register_user, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRegistrationScreen();
                    }
                })
                .setNegativeButton(R.string.no_thanks, null).create();

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
        } else {
            dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
        }

        dialog.show();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRefresh() {

    }
}
