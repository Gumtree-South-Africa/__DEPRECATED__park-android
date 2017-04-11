package com.ebay.park.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.requests.SocialConnectRequest;
import com.ebay.park.requests.SocialDisconnectRequest;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.TwitterUtil;
import com.ebay.park.views.FbButtonLinkedAccount;
import com.ebay.park.views.TextViewBook;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Social Networks Fragment
 *
 * @author Nicol�s Mat�as Fern�ndez
 *
 */

public class SocialNetworksFragment extends BaseFragment {

	// TWITTER:
	private static Twitter sTwitter;
	private static String sVerifier;
	private static RequestToken sRequestToken;
	private static AccessToken sAccessToken;
	// FACEBOOK:
	private static String sFbAccessToken;
	private static String sFbUserId;

	private static final String TWITTER = "twitter";
	private static final String FACEBOOK = "facebook";
	private static String sCurrentSocialNet;

	private FbButtonLinkedAccount mFbLoginButton;
	private TextViewBook mTwLoginTextView;
//	private TextViewBook mTwitterUsername;
//	private TextViewBook mFacebookUsername;
	private CallbackManager mCallbackManager;
	private boolean mIsExecutingLogin = false;
	private String mUsernameFacebook;
	private Boolean mHasEmail = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setTitle(R.string.linked_accounts);

		final ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		final View baseview = inflater.inflate(R.layout.fragment_social_networks, parentView, true);

		baseview.findViewById(R.id.btn_fb_login).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doFacebookLogin();
			}
		});

		mFbLoginButton = (FbButtonLinkedAccount) baseview.findViewById(R.id.btn_fb_login);

		mTwLoginTextView = (TextViewBook) baseview.findViewById(R.id.tv_tw_login);
		mTwLoginTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				newTwitterLogin();
			}
		});

//		mTwitterUsername = (TextViewBook) baseview.findViewById(R.id.tv_twitter_username);
//		mFacebookUsername = (TextViewBook) baseview.findViewById(R.id.tv_facebook_username);

		updateView();

		return baseview;
	}

	private void updateView() {
		if (hasFacebookLogin()){
			mFbLoginButton.setTextColor(getResources().getColor(R.color.system_notification));
			mFbLoginButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo_accounts_fb_blue, 0, 0, 0);
//			mFacebookUsername.setText(FacebookUtil.getFacebookUsername(getBaseActivity()));
//			mFacebookUsername.setVisibility(View.VISIBLE);
		} else {
			mFbLoginButton.setTextColor(getResources().getColor(R.color.GraySocialNetwork));
			mFbLoginButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo_accounts_fb_gray, 0, 0, 0);
		}

		if (hasTwitterLogin()){
			mTwLoginTextView.setTextColor(getResources().getColor(R.color.system_notification));
			mTwLoginTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo_accounts_twitter_blue, 0, 0, 0);
//			mTwitterUsername.setText("@"+TwitterUtil.getTwitterUsername(getBaseActivity()));
//			mTwitterUsername.setVisibility(View.VISIBLE);
		} else {
			mTwLoginTextView.setTextColor(getResources().getColor(R.color.GraySocialNetwork));
			mTwLoginTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo_accounts_twitter_gray, 0, 0, 0);
		}
	}

	private boolean hasFacebookLogin() {
		return FacebookUtil.getIsFacebookLoggedInAlready(getBaseActivity());
	}

	private boolean hasTwitterLogin() {
		return TwitterUtil.getIsTwitterLoggedInAlready(getBaseActivity());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mCallbackManager = CallbackManager.Factory.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		onRefresh();
	}

	@Override
	public void onStart() {
		super.onStart();
		afterTwitterLogin();
	}

	@Override
	public void onRefresh() {
	}

	private void doFacebookLogin() {

		if (!NeckSpiceManager.isDeviceOnline(getBaseActivity())) {
			MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.no_internet),
					getBaseActivity().getCroutonsHolder());
			return;
		}

		if (!FacebookUtil.getIsFacebookLoggedInAlready(getBaseActivity())) {

			mFbLoginButton.setReadPermissions(FacebookUtil.READ_PERMISSONS);
			mFbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(final LoginResult loginResult) {
					if (loginResult != null) {
						Bundle fbParameters = new Bundle();
						fbParameters.putString("fields", "id,name,email");

						GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
							@Override
							public void onCompleted(JSONObject user, GraphResponse response) {
								if (response.getError() != null) {
									System.out.println("ERROR");
									if (isAdded()) {
										MessageUtil.showError((BaseActivity) getActivity(),
												getResources().getString(R.string.error_facebook_result),
												getBaseActivity().getCroutonsHolder());
									}
								} else {
									try {
										sFbAccessToken = loginResult.getAccessToken().getToken();
										sFbUserId = loginResult.getAccessToken().getUserId();
										FacebookUtil.saveIsPublishPermissionGranted(getContext(),
												loginResult.getAccessToken().getPermissions()
														.contains("publish_actions"));
										mHasEmail = loginResult.getAccessToken().getPermissions()
												.contains("email");
										mUsernameFacebook = user.getString("name");
										sCurrentSocialNet = FACEBOOK;
										if (!mIsExecutingLogin) {
											mIsExecutingLogin = true;
											mSpiceManager.execute(new SocialConnectRequest(sCurrentSocialNet, sFbAccessToken, null, sFbUserId),
													new SocialNetworksConnectListener());
										}
									} catch (Exception e) {
										if (isAdded()) {
											MessageUtil.showError((BaseActivity) getActivity(),
													getResources().getString(R.string.error_facebook_result),
													getBaseActivity().getCroutonsHolder());
										}
									}
								}
							}
						});

						graphRequest.setParameters(fbParameters);
						graphRequest.executeAsync();
					}
				}

				@Override
				public void onCancel() {
				}

				@Override
				public void onError(FacebookException e) {
				}
			});
		} else {
			logoutFromFacebook();
		}
		mFbLoginButton.setEnabled(false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (getBaseActivity() != null && resultCode == Activity.RESULT_OK && data != null) {
			mCallbackManager.onActivityResult(requestCode, resultCode, data);
		} else {
			if (getActivity() != null) {
				MessageUtil.showError((BaseActivity) getActivity(),
						getResources().getString(R.string.error_facebook_result),
						getBaseActivity().getCroutonsHolder());
			}
		}
	}

	private void newTwitterLogin() {
		if (!NeckSpiceManager.isDeviceOnline(getBaseActivity())) {
			MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.no_internet),
					getBaseActivity().getCroutonsHolder());
			return;
		}

		if (!TwitterUtil.getIsTwitterLoggedInAlready(getBaseActivity())) {
			showProgress();
			new GetTwitterTokenTask(getActivity()).execute("");
		} else {
			// Logout from Twitter:
			logoutFromTwitter();
		}
	}

	private void afterTwitterLogin() {
		if (!TwitterUtil.getIsTwitterLoggedInAlready(getBaseActivity())) {
			Uri uri = getActivity().getIntent().getData();
			if (uri != null) {
				// oAuth verifier
				sVerifier = uri.getQueryParameter(TwitterUtil.URL_TWITTER_OAUTH_VERIFIER);
				new GetAccessToken().execute();
			}
		}
	}

	@Override
	public void onBackPressed() {
	}

	private class GetAccessToken extends AsyncTask<Void, Void, AccessToken> {

		protected AccessToken doInBackground(Void... arg0) {
			try {
				return sTwitter.getOAuthAccessToken(sRequestToken, sVerifier);
			} catch (TwitterException e) {
				// Check log for login errors
				Log.e("Twitter Login Error", "> " + e.getMessage());
			}
			return null;
		}

		protected void onPostExecute(AccessToken accessToken) {

			if (accessToken != null) {
				SocialNetworksFragment.sAccessToken = accessToken;

				long userID = accessToken.getUserId();

				sCurrentSocialNet = TWITTER;

				mSpiceManager.execute(
						new SocialConnectRequest(sCurrentSocialNet, accessToken.getToken(),
								accessToken.getTokenSecret(), String.valueOf(userID)),
						new SocialNetworksConnectListener());
			} else {
				MessageUtil.showError((BaseActivity) getActivity(), "Error al conectarse con Twitter",
						getBaseActivity().getCroutonsHolder());
			}

		}
	}

	private void logoutFromTwitter() {
		sCurrentSocialNet = TWITTER;
		showProgress();
		mSpiceManager.execute(new SocialDisconnectRequest(sCurrentSocialNet), new SocialNetworksDisconnectListener());
	}

	private void logoutFromFacebook() {
		sCurrentSocialNet = FACEBOOK;
		showProgress();
		mSpiceManager.execute(new SocialDisconnectRequest(sCurrentSocialNet), new SocialNetworksDisconnectListener());
	}

	private class SocialNetworksConnectListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			if (sCurrentSocialNet.equals(FACEBOOK)) {
				mIsExecutingLogin = false;
				mFbLoginButton.setEnabled(true);
				LoginManager.getInstance().logOut();
			}
			MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean t) {
			if (t) {

				if (sCurrentSocialNet.equals(TWITTER)) {

					// After getting access token, access token secret
					// store them in application preferences
					String accTokenParts[] = sAccessToken.getToken().split("-");
					String accToken = accTokenParts[1];
					TwitterUtil.saveTwitterAccessToken(getBaseActivity(), accToken);
					TwitterUtil.saveTwitterSecretAccessToken(getBaseActivity(), sAccessToken.getTokenSecret());
					TwitterUtil.saveIsTwitterLoggedInAlready(getBaseActivity());
//					TwitterUtil.saveTwitterUsername(getBaseActivity(), sAccessToken.getScreenName());

//					mTwitterUsername.setText("@" + sAccessToken.getScreenName());
//					mTwitterUsername.setVisibility(View.VISIBLE);

					Log.e("Twitter OAuth Token", "> " + sAccessToken.getToken());
					long userID = sAccessToken.getUserId();
					TwitterUtil.saveTwitterUserId(getBaseActivity(), userID);
					// Change twitter login button to logout twitter button:
					MessageUtil.showSuccess(getBaseActivity(), getResources()
							.getString(R.string.linked_with_tw_success), getBaseActivity().getCroutonsHolder());
				} else if (sCurrentSocialNet.equals(FACEBOOK)) {
					mIsExecutingLogin = false;
					mFbLoginButton.setEnabled(true);
					PreferencesUtil.saveHasEmail(getBaseActivity(),mHasEmail);
					FacebookUtil.saveIsFacebookLoggedInAlready(getBaseActivity());
//					FacebookUtil.saveFacebookUsername(getContext(),mUsernameFacebook);
//					mFacebookUsername.setText(mUsernameFacebook);
//					mFacebookUsername.setVisibility(View.VISIBLE);

					// Change facebook login button to logout
					// facebook button:
					MessageUtil.showSuccess(getBaseActivity(), getResources()
							.getString(R.string.linked_with_fb_success), getBaseActivity().getCroutonsHolder());
				}

				updateView();
				hideProgress();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			if (sCurrentSocialNet.equals(FACEBOOK)) {
				mIsExecutingLogin = false;
				mFbLoginButton.setEnabled(true);
				LoginManager.getInstance().logOut();
			}
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());

		}
	}

	private class SocialNetworksDisconnectListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			if (sCurrentSocialNet.equals(FACEBOOK)) {
				mIsExecutingLogin = false;
				mFbLoginButton.setEnabled(true);
			}
			MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean t) {
			if (t) {

				if (sCurrentSocialNet.equals(TWITTER)) {
					TwitterUtil.logoutFromTwitter(getBaseActivity());
//					mTwitterUsername.setVisibility(View.GONE);
//					mTwitterUsername.setText("");
					MessageUtil.showSuccess(getBaseActivity(),
							getResources().getString(R.string.unlinked_from_tw_success), getBaseActivity().getCroutonsHolder());
					// Change twitter logout button to login twitter button:
				} else if (sCurrentSocialNet.equals(FACEBOOK)) {
					mIsExecutingLogin = false;
					mFbLoginButton.setEnabled(true);
					if (PreferencesUtil.getIsSmsUser(getBaseActivity())){
						mHasEmail = false;
						PreferencesUtil.saveHasEmail(getBaseActivity(),mHasEmail);
					}
					FacebookUtil.logoutFromFacebook(getBaseActivity());
//					mFacebookUsername.setVisibility(View.GONE);
//					mFacebookUsername.setText("");
					MessageUtil.showSuccess(getBaseActivity(),
							getResources().getString(R.string.unlinked_from_fb_success), getBaseActivity().getCroutonsHolder());
					// Change facebook logout button to login facebook button:
				}
				hideProgress();
				updateView();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			if (sCurrentSocialNet.equals(FACEBOOK)) {
				mIsExecutingLogin = false;
				mFbLoginButton.setEnabled(true);
			}
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}
	}

	private class GetTwitterTokenTask extends AsyncTask<String, Void, String> {

		private Activity mActivity;
		private String mOauthUrl, mVerifier;
		private Dialog mDialog;
		ProgressBar mProgressBar;
		private WebView webView;

		// Twitter variables
		private RequestToken mRequestToken;

		public GetTwitterTokenTask(Activity activity) {
			this.mActivity = activity;
			sTwitter = new TwitterFactory().getInstance();
			sTwitter.setOAuthConsumer(TwitterUtil.TWITTER_CONSUMER_KEY, TwitterUtil.TWITTER_CONSUMER_SECRET);
		}

		@Override
		protected String doInBackground(String... args) {
			try {
				mRequestToken = sTwitter.getOAuthRequestToken(TwitterUtil.TWITTER_CALLBACK_URL);
				if (mRequestToken != null) {
					mOauthUrl = mRequestToken.getAuthorizationURL();
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return mOauthUrl;
		}

		@Override
		protected void onPostExecute(String oauthUrl) {
			if (oauthUrl != null) {
				mDialog = new Dialog(mActivity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				mDialog.setContentView(R.layout.auth_dialog);
				mProgressBar = (ProgressBar) mDialog.findViewById(R.id.pbWebv);
				webView = (WebView) mDialog.findViewById(R.id.webv);
				webView.getSettings().setJavaScriptEnabled(true);
				webView.loadUrl(oauthUrl);

				webView.setWebViewClient(new WebViewClient() {
					boolean authComplete = false;

					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
						hideProgress();
						mProgressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						mProgressBar.setVisibility(View.GONE);
						if (url.contains("oauth_verifier") && authComplete == false) {
							authComplete = true;
							Uri uri = Uri.parse(url);
							mVerifier = uri.getQueryParameter("oauth_verifier");

							mDialog.dismiss();
							showProgress();

							// revoke access token asynctask
							new AccessTokenGetTask(mRequestToken, mVerifier).execute();
						} else if (url.contains("denied")) {
							mDialog.dismiss();
						}
					}
				});

				webView.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onProgressChanged(WebView view, int newProgress) {
						mProgressBar.setProgress(newProgress);
					}
				});

				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(mDialog.getWindow().getAttributes());
				lp.width = WindowManager.LayoutParams.MATCH_PARENT;
				lp.height = WindowManager.LayoutParams.MATCH_PARENT;
				mDialog.getWindow().setAttributes(lp);

				mDialog.show();
				mDialog.setCancelable(true);
			}
		}
	}

	private class AccessTokenGetTask extends AsyncTask<String, String, AccessToken> {

		private RequestToken mRequestToken;
		private String mVerifier;

		public AccessTokenGetTask(RequestToken requestToken, String verifier) {
			mRequestToken = requestToken;
			mVerifier = verifier;
		}

		@Override
		protected AccessToken doInBackground(String... args) {
			AccessToken accessToken = null;
			try {
				accessToken = sTwitter.getOAuthAccessToken(mRequestToken, mVerifier);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return accessToken;
		}

		@Override
		protected void onPostExecute(AccessToken accessToken) {
			if (accessToken != null) {
				SocialNetworksFragment.sAccessToken = accessToken;

				long userID = accessToken.getUserId();

				sCurrentSocialNet = TWITTER;

				mSpiceManager.execute(
						new SocialConnectRequest(sCurrentSocialNet, accessToken.getToken(),
								accessToken.getTokenSecret(), String.valueOf(userID)),
						new SocialNetworksConnectListener());
			} else {
				MessageUtil.showError((BaseActivity) getActivity(), "Error al conectarse con Twitter",
						getBaseActivity().getCroutonsHolder());
			}
		}
	}

}
