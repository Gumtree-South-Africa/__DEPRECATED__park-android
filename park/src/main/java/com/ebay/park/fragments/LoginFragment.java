package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.LoginModel;
import com.ebay.park.requests.BaseParkHttpRequest;
import com.ebay.park.requests.LoginAKRequest;
import com.ebay.park.requests.LoginFBRequest;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.views.CustomFbLoginButton;
import com.ebay.park.views.TextViewBook;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles app login.
 *
 * @author federico.perez
 * @author Nicolás Matias Fernández
 */
public class LoginFragment extends BaseFragment {

    public static int sAppRequestCode = 99;
	public static final String TEST_TAG = LoginFragment.class.getSimpleName();
	private static final String FB_LOGIN_KEY = "fbLogin";
	private TextViewBook mTermsLink;
	private Button mSmsLoginButton;
    private Button mEmailLoginButton;
	private TextView mStandardLoginButton;
	private ImageView mClose;
	private TextView mLoginFailedView;
	private String mFbToken;
	private String mFbId;
	private CustomFbLoginButton mFbLoginButton;
	private String mDeviceId;
	private CallbackManager mCallbackManager;
	private String mUniqueDeviceId;
	private String mAkToken;
    private String mPhoneNumber;
    private String mEmail;
    private boolean mIsFacebookLogin;
    private boolean mIsAccountKitLogin;
    private boolean mWithSms;
	Tracker mGAnalyticsTracker;
	private boolean mHasEmail = false;

	@Override
	public void onStart() {
		super.onStart();
		mSpiceManager.addListenerIfPending(buildLoginFBRequest(), new LoginRequestListener());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!FacebookSdk.isInitialized()){
			FacebookSdk.sdkInitialize(getActivity());
		}
		setTitle(R.string.login_with_cred);
		getBaseActivity().getSupportActionBar().hide();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (GCMUtils.checkPlayServices(getBaseActivity())) {
			mDeviceId = GCMUtils.getRegistrationId(getBaseActivity());

			if (mDeviceId.isEmpty()) {
				new Thread(new Runnable() {
					public void run() {
						mDeviceId = GCMUtils.registerOnGCM(getBaseActivity());
					}
				}).start();
			}
		} else {
			Logger.info("No valid Google Play Services APK found.");
		}
		mUniqueDeviceId = DeviceUtils.getUniqueDeviceId(getBaseActivity());

		ParkApplication.sMessageSessionExpiredAlreadyShown = false;
		mCallbackManager = CallbackManager.Factory.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		View baseview = inflater.inflate(R.layout.fragment_login, parentView, true);

		mFbLoginButton = (CustomFbLoginButton) baseview.findViewById(R.id.btn_fb_login);
		mFbLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SwrveSDK.event(SwrveEvents.LOGIN_FACEBOOK_ATTEMPT);
				doFacebookLogin();
			}
		});

		mStandardLoginButton = (TextView) baseview.findViewById(R.id.tv_cred_login);
		mStandardLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Build and send "The user taps on login with credentials" Event.
				mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User action")
						.setAction("User goes to sign up from login form")
						.setLabel("The user taps on login with credentials").build());
				ScreenManager.showStandardLoginFragment(getBaseActivity());
			}
		});

		mClose = (ImageView) baseview.findViewById(R.id.iv_close);
		mClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getBaseActivity().finish();
			}
		});

		mLoginFailedView = (TextView) baseview.findViewById(R.id.tv_login_failed);
		mTermsLink = (TextViewBook) baseview.findViewById(R.id.tvTermsLink);

        mSmsLoginButton = (Button) baseview.findViewById(R.id.btn_register_accountkit_sms);
        mSmsLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				SwrveSDK.event(SwrveEvents.LOGIN_SMS_ATTEMPT);
                onLoginPhone(true, "");
            }
        });

        mEmailLoginButton = (Button) baseview.findViewById(R.id.btn_register_accountkit_mail);
        mEmailLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				SwrveSDK.event(SwrveEvents.LOGIN_MAIL_ATTEMPT);
                onLoginPhone(false, "");
            }
        });

		mTermsLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(IntentFactory.getTermsConditions());
			}
		});

		if (AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null) {
			LoginManager.getInstance().logOut();
		}

		if (DeviceUtils.isDeviceLollipopOrHigher()) {
			LinearLayout loginControlsLayout = (LinearLayout) baseview.findViewById(R.id.login_controls);
			loginControlsLayout.setPadding(0, DeviceUtils.getStatusBarHeight(getBaseActivity()), 0, 0);
		}

		SpannableString spannableString = new SpannableString(getResources().getString(R.string.terms_of_service_nocapitals));
		spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
		mTermsLink.setText(spannableString);

		return baseview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(FB_LOGIN_KEY)) {
			mIsFacebookLogin = savedInstanceState.getBoolean(FB_LOGIN_KEY);
		}
		disableRefreshSwipe();
		mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(FB_LOGIN_KEY, mIsFacebookLogin);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        if (getBaseActivity() != null && data != null) {

            if (requestCode == sAppRequestCode) {
                // confirm that this response matches your request
                AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                Toast toast = Toast.makeText(getBaseActivity(), "", Toast.LENGTH_LONG);
                if (loginResult.getError() != null) {
					Map<String,String> payload = new HashMap<>();
					payload.put(SwrveEvents.EVENT_FAIL_KEY, loginResult.getError().getUserFacingMessage());
					SwrveSDK.event(mWithSms ? SwrveEvents.LOGIN_SMS_FAIL :
							SwrveEvents.LOGIN_MAIL_FAIL, payload);
                    toast.setText(getString(R.string.account_kit_error));
                    toast.show();
                } else if (loginResult.wasCancelled()) {
					SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
                    toast.setText(getString(R.string.account_kit_canceled));
                    toast.show();
                } else {
                    if (loginResult.getAccessToken() != null) {
                        try {
                            SwrveSDK.event(mWithSms ? SwrveEvents.LOGIN_SMS_SUCCESS :
									SwrveEvents.LOGIN_MAIL_SUCCESS);
							mIsAccountKitLogin = true;
                            mAkToken = loginResult.getAccessToken().getToken();
                            getAccountKitInfo();
                        } catch (Exception e) {
							Map<String,String> payload = new HashMap<>();
							payload.put(SwrveEvents.EVENT_FAIL_KEY, e.getMessage());
							SwrveSDK.event(mWithSms ? SwrveEvents.LOGIN_SMS_FAIL :
									SwrveEvents.LOGIN_MAIL_FAIL, payload);
                            if (isAdded()) {
                                MessageUtil.showError((BaseActivity) getActivity(),
                                        getResources().getString(R.string.account_kit_error),
                                        getBaseActivity().getCroutonsHolder());
                            }
                        }
                    } else {
						Map<String,String> payload = new HashMap<>();
						payload.put(SwrveEvents.EVENT_FAIL_KEY, "AccessToken es null");
						SwrveSDK.event(mWithSms ? SwrveEvents.LOGIN_SMS_FAIL :
								SwrveEvents.LOGIN_MAIL_FAIL, payload);
                        toast.setText(getString(R.string.account_kit_error));
                        toast.show();
                    }
                }

            } else {
                if (resultCode == Activity.RESULT_OK){
                    mCallbackManager.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else {
            if (getActivity() != null) {
                MessageUtil.showError((BaseActivity) getActivity(),
                        getResources().getString(R.string.error_facebook_result),
                        getBaseActivity().getCroutonsHolder());
            }
        }
	}

	private BaseParkHttpRequest<LoginModel> buildLoginFBRequest() {
		return new LoginFBRequest(mFbToken, mFbId, mDeviceId, mUniqueDeviceId);
	}

	private BaseParkHttpRequest<LoginModel> buildLoginAKRequest() {
        return new LoginAKRequest(mAkToken, mPhoneNumber, mEmail, mDeviceId, mUniqueDeviceId);
    }

    private void getAccountKitInfo(){
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                mPhoneNumber = account.getPhoneNumber() == null ? null : account.getPhoneNumber().toString();
                mEmail = account.getEmail();

                mSpiceManager.execute(buildLoginAKRequest(), new LoginRequestListener());
            }
            @Override
            public void onError(final AccountKitError error) {
            }
        });
    }

	private void showError(String message) {
		mLoginFailedView.setText(message);
		mLoginFailedView.setVisibility(View.VISIBLE);
	}

	private void clearError() {
		mLoginFailedView.setText("");
		mLoginFailedView.setVisibility(View.GONE);
	}

	private void doFacebookLogin() {
		clearError();
		if (!NeckSpiceManager.isDeviceOnline(getBaseActivity())) {
			MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.no_internet),
					getBaseActivity().getCroutonsHolder());
			return;
		}
		// Set permissions
		mFbLoginButton.setReadPermissions(FacebookUtil.READ_PERMISSONS);

		// Callback registration
		mFbLoginButton.registerCallback(mCallbackManager,
				new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						if (loginResult != null) {
							try {
								SwrveSDK.event(SwrveEvents.LOGIN_FACEBOOK_SUCCESS);
								SwrveSDK.event(SwrveEvents.LOGIN_ATTEMPT);
								mIsFacebookLogin = true;
								mFbToken = loginResult.getAccessToken().getToken();
								mFbId = loginResult.getAccessToken().getUserId();
								FacebookUtil.saveIsPublishPermissionGranted(getContext(),
										loginResult.getAccessToken().getPermissions()
												.contains("publish_actions"));
								FacebookUtil.saveIsFriendsPermissionGranted(getContext(),
										loginResult.getAccessToken().getPermissions()
												.contains("user_friends"));
								mHasEmail = loginResult.getAccessToken().getPermissions()
										.contains("email");
								mSpiceManager.execute(buildLoginFBRequest(), new LoginRequestListener());
							} catch (Exception e) {
								if (isAdded()) {
									Map<String,String> payload = new HashMap<>();
									payload.put(SwrveEvents.EVENT_FAIL_KEY, e.getMessage());
									SwrveSDK.event(SwrveEvents.LOGIN_FACEBOOK_FAIL, payload);
									MessageUtil.showError((BaseActivity) getActivity(),
											getResources().getString(R.string.error_facebook_result),
											getBaseActivity().getCroutonsHolder());
								}
							}
						}
					}

					@Override
					public void onCancel() {
						SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
					}

					@Override
					public void onError(FacebookException error) {
						if (isAdded()) {
							Map<String,String> payload = new HashMap<>();
							payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
							SwrveSDK.event(SwrveEvents.LOGIN_FACEBOOK_FAIL, payload);
							MessageUtil.showError((BaseActivity) getActivity(),
									getResources().getString(R.string.error_facebook_result),
									getBaseActivity().getCroutonsHolder());
						}
					}
				});

	}

	@Override
	protected void showProgress() {
		try {
			super.showProgress();
			mFbLoginButton.setEnabled(false);
		} catch (NullPointerException e) {
		}
	}

	@Override
	protected void hideProgress() {
		try {
			super.hideProgress();
			mFbLoginButton.setEnabled(true);
		} catch (NullPointerException e) {
		}
	}

	@Override
	public void onBackPressed() {
	}

	/**
	 * Listener for login responses from Park server.
	 */
	private class LoginRequestListener extends BaseNeckRequestListener<LoginModel> {

		@Override
		public void onRequestSuccessfull(LoginModel login) {
			if (login != null) {
				SwrveSDK.event(SwrveEvents.LOGIN_SUCCESS);
				ParkApplication.getInstance().setSessionToken(login.getSessionToken());
				ParkApplication.getInstance().setUsername(login.getUsername());
				ParkApplication.getInstance().setUserProfilePicture(login.getProfilePicture());

				if (mIsFacebookLogin) {
					PreferencesUtil.saveHasEmail(getBaseActivity(),mHasEmail);
					FacebookUtil.saveIsFacebookLoggedInAlready(getBaseActivity());
				}
				if (mWithSms){
					PreferencesUtil.saveIsSmsUser(getBaseActivity(), true);
				} else {
					if (mIsAccountKitLogin){
						PreferencesUtil.saveHasEmail(getBaseActivity(), true);
					}
				}

				ParkApplication.sJustLogged = true;
				ParkApplication.sJustLoggedSecondLevel = true;
				ParkApplication.sJustLoggedThirdLevel = true;
				getBaseActivity().finish();

			}
			hideProgress();
		}

		@Override
		public void onRequestError(Error error) {
			Logger.verb("onRequestError");
			if (error != null) {
				Map<String,String> payload = new HashMap<>();
				payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
				SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
				switch (error.getErrorCode()) {
					case ResponseCodes.Login.INVALID_PASSWORD:
						showError(getString(R.string.invalid_user_pass));
					case ResponseCodes.Login.USER_NOT_FOUND:
					case ResponseCodes.Login.NO_FACEBOOK:
						if (mIsFacebookLogin) {
							ScreenManager.showUserFBRegisterFragment(getBaseActivity());
               			} else if (mIsAccountKitLogin) {
                            ScreenManager.showUserAccKitRegisterFragment(getBaseActivity(), mWithSms);
                        } else {
							showError(error.getMessage());
						}
						break;
					case ResponseCodes.Login.IO_ERROR:
						showError(getString(R.string.error_generic));
						break;
					case ResponseCodes.Signout.APP_DEPRECATED:
						break;
					default:
						showError(error.getMessage());
						break;
				}
			}
			hideProgress();
		}

		@Override
		public void onRequestException(SpiceException ex) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, ex.getMessage());
			SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
			hideProgress();
			showError(ex.getMessage());
			Logger.verb(ex.getLocalizedMessage());
		}

	}

	@Override
	public void onRefresh() {
		;// No refresh
	}

    public void onLoginPhone(boolean isSMSLogin, String email) {
		clearError();
        LoginType loginType = isSMSLogin ? LoginType.PHONE : LoginType.EMAIL;
        mWithSms =  isSMSLogin;

        final Intent intent = new Intent(getActivity(), AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.TOKEN);
        String whiteListProd [] = {"US"};
		String whiteList [] = {"US", "AR", "UY", "CN", "MX"};
		if (ParkApplication.sParkConfiguration.logRequests()) {
			configurationBuilder.setSMSWhitelist(whiteList);
		} else {
			configurationBuilder.setSMSWhitelist(whiteListProd);
		}
		if(!isSMSLogin){
			if(!email.isEmpty()){
				configurationBuilder.setInitialEmail(email);
			}
		}
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        getBaseActivity().startActivityForResult(intent, sAppRequestCode);
    }
}
