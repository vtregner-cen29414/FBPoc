package cz.csas.startup.FBPoc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Constants;
import cz.csas.startup.FBPoc.utils.GothamFont;
import cz.csas.startup.FBPoc.utils.RegistrationUtils;
import cz.csas.startup.FBPoc.utils.Utils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 6.5.2014.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "Friends24";
    public static final String PREFERENCES_NAME = Constants.FRIENDS24_SHARED_PREFERENCES;
    public static final String USERNAME_PREF_KEY = "username";
    public static final String REGISTERED_FACEBOOK_ID = "registeredFacebookId";
    public static final String REGISTERED_FACEBOOK_USERNAME = "registeredFacebookUsername";

    private boolean isFetching=false;
    private UiLifecycleHelper uiHelper;
    int fbRetryCount = 0;
    private boolean googlePlayServicesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        final Typeface mFont = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        //final Typeface boldFont = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        final ViewGroup mContainer = (ViewGroup) findViewById(android.R.id.content).getRootView();
        Utils.setAppFont(mContainer, mFont, false);

        TextView loginLogo1 = (TextView) findViewById(R.id.login_logo1);
        loginLogo1.setTypeface(GothamFont.MEDIUM);

        ((Friends24Application) getApplication()).getFriends24Context().clearSession();
        ((Friends24Application) getApplication()).invalidateSessionInPreferences();

        final EditText username = (EditText) findViewById(R.id.loginUsername);
        final EditText password = (EditText) findViewById(R.id.loginPassword);
        final Button loginButton = (Button) findViewById(R.id.btnLogin);
        loginButton.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginButton.setEnabled(username.length() > 0 && password.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        username.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);

        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        if (preferences.contains(USERNAME_PREF_KEY)) {
            username.setText(preferences.getString(USERNAME_PREF_KEY, null));
            password.requestFocus();
        }

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
    }

    public void onLogin(View view) {
        final TextView username = (TextView) findViewById(R.id.loginUsername);
        final TextView password = (TextView) findViewById(R.id.loginPassword);
        Log.d(TAG, "onLogin");
        Friends24Application application = (Friends24Application) getApplication();
        String authorizationString = "Basic " + Base64.encodeToString((username.getText().toString().trim() + ":" + password.getText().toString().trim()).getBytes(), Base64.NO_WRAP);
        application.getFriends24Context().setAuthHeader(authorizationString);

        new GetAccountsTask(this).execute();
    }

    public void checkGooglePlayServicesAvailability()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        googlePlayServicesAvailable = resultCode == ConnectionResult.SUCCESS;
        if(resultCode != ConnectionResult.SUCCESS)
        {
            //Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 69);
            //dialog.setCancelable(false);
            //dialog.show();
            Toast.makeText(this, GooglePlayServicesUtil.getErrorString(resultCode), Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "Result of checking of google play services is: " + resultCode);
    }


    private boolean ensureOpenSession() {
        if (Session.getActiveSession() == null ||
                !Session.getActiveSession().isOpened()) {
            openActiveSession(this, true, Arrays.asList("xmpp_login"), new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            });
            return false;
        }
        return true;
    }

    private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened() && !isFetching) {
            Log.i(TAG, "FB Logged in...");
            isFetching = true;
            AppEventsLogger.activateApp(this);
            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    Log.i(TAG, "FB Logged in, user fetched...");
                    isFetching = false;
                    if (user != null) {
                        //profilePictureView.setProfileId(user.getId());
                        //userNameView.setText(user.getName());
                        Friends24Application application = (Friends24Application) getApplication();
                        application.getFriends24Context().setFbUser(user);
                        if (application.getFriends24Context().isAppLogged()) {
                            application.saveSessionToPreferences();

                            updateFacebookIdIfNeeded();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            // home screen is always on the top
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LoginActivity.this.startActivity(intent);
                        }
                    }
                }
            }).executeAsync();

        } else if (state.isClosed()) {
            if (exception != null) {
                Log.e(TAG, exception.toString());
                if (exception instanceof FacebookOperationCanceledException) {
                    if (fbRetryCount++ < 1) {
                        Log.d(TAG, "FacebookOperationCanceledException: retry open session again");
                        ensureOpenSession();
                    }
                }
            }
            Log.i(TAG, "Logged out...");
        }
    }

    public void updateFacebookIdIfNeeded() {
        Friends24Context friends24Context = ((Friends24Application) getApplication()).getFriends24Context();
        if (friends24Context.getFbUser() != null && friends24Context.isAppLogged()) {
            SharedPreferences preferences = RegistrationUtils.getGCMPreferences(this);
            String registeredFacebookId = preferences.getString(REGISTERED_FACEBOOK_ID, null);
            String registeredFacebookUsername = preferences.getString(REGISTERED_FACEBOOK_USERNAME, null);
            if (registeredFacebookId == null
                    || registeredFacebookUsername == null
                    || !registeredFacebookId.equals(friends24Context.getFbUser().getId())
                    || !registeredFacebookUsername.equals(friends24Context.getLoggedUser())) {

                Log.d(TAG, String.format("Updating facebook id %s of current user %s", friends24Context.getFbUser().getId(), friends24Context.getLoggedUser()));
                new UpdateFacebookIdTask(this).execute();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.d(TAG, "uiHelper.onActivityResult onComplete");
            }

            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.d(TAG, "uiHelper.onActivityResult onError "+ error.toString());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
        checkGooglePlayServicesAvailability();
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param gcmid GCM id
     * @param username current user
     */
    private void storeRegistrationData(Context context, String gcmid, String username) {
        final SharedPreferences prefs = RegistrationUtils.getGCMPreferences(context);
        int appVersion = RegistrationUtils.getAppVersion(context);
        Log.i(TAG, "Saving gcmRegId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.USERNAME, username);
        editor.putString(Constants.GCM_REGISTRATION_ID, gcmid);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private class GetAccountsTask extends AsyncTask<Void, Void, List<Account>> {
        private static final String uri = "accounts";

        ProgressDialog progressDialog;

        private GetAccountsTask(Context context) {
            super(context, uri, HttpGet.METHOD_NAME, null, true, true);
        }

        @Override
        public List<Account> parseResponseObject(JSONObject object) throws JSONException {
            JSONArray jaccounts = object.getJSONArray("accounts");
            List<Account> accounts = new ArrayList<Account>();
            for (int i=0; i< jaccounts.length(); i++) {
                JSONObject jaccount = jaccounts.getJSONObject(i);
                Account account = new Account();
                account.setId(jaccount.getLong("id"));
                if (!jaccount.isNull("accountPrefix")) account.setPrefix(jaccount.getLong("accountPrefix"));
                account.setNumber(jaccount.getLong("accountNumber"));
                int type = jaccount.getInt("type");
                if (type == 1) {
                    account.setType("Běžný účet");
                }
                else {
                    account.setType("Spořící účet");
                }

                account.setCurrency(jaccount.getString("currency"));
                account.setBalance(new BigDecimal(jaccount.getString("balance")));
                accounts.add(account);
            }
            return accounts;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog =ProgressDialog.show(getContext(), null, getContext().getResources().getString(R.string.waitPlease));
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Account>> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                getApplication().getFriends24Context().setAccounts(result.getResult());
                getApplication().getFriends24Context().setAppLogged(true);
                getApplication().saveSessionToPreferences();
                final EditText username = (EditText) findViewById(R.id.loginUsername);
                SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
                String user = username.getText().toString().trim();
                getApplication().getFriends24Context().setLoggedUser(user);
                preferences.edit().putString(USERNAME_PREF_KEY, user).commit();

                String gcmRegistrationId = RegistrationUtils.getGcmRegistrationId(getContext(), user);
                if (gcmRegistrationId == null) {
                    UpdateGcmIdTask updateGcmIdTask = new UpdateGcmIdTask(getContext(), user);
                    updateGcmIdTask.execute();
                }

                // login to FB
                if (LoginActivity.this.ensureOpenSession() && getApplication().getFriends24Context().getFbUser() != null) {
                    updateFacebookIdIfNeeded();

                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    // home screen is always on the top
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);
                }
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }

        }
    }

    private class UpdateGcmIdTask extends AsyncTask<JSONObject, Void, Void> {
        private static final String uri = "device";
        private String user;
        private String gcmRegistrationId;

        private UpdateGcmIdTask(Context context, String user) {
            super(context, uri, HttpPut.METHOD_NAME, null, false, false);
            this.user = user;
        }

        @Override
        protected AsyncTaskResult<Void> doInBackground(JSONObject... params) {
            if (googlePlayServicesAvailable) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getContext());
                JSONObject o = new JSONObject();
                try {
                    gcmRegistrationId = gcm.register(getContext().getString(R.string.sender_id));
                    Log.d(TAG, "Device registered on GCM: " + gcmRegistrationId);
                    o.put("gcmid", gcmRegistrationId);
                    getHttpClient().setJsonReq(o);
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return new AsyncTaskResult<Void>(AsyncTaskResult.Status.OTHER_ERROR);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    return new AsyncTaskResult<Void>(AsyncTaskResult.Status.OTHER_ERROR);
                }
                return super.doInBackground(params);
            }
            else return new AsyncTaskResult<Void>(AsyncTaskResult.Status.OK);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Void> result) {
            if (gcmRegistrationId != null) {
                if (result.getStatus() == AsyncTaskResult.Status.OK){
                    Log.d(TAG, "Device registred in Friends24 server with id:" + gcmRegistrationId);
                    storeRegistrationData(getContext(), gcmRegistrationId, user);
                }
                else {
                    Log.e(TAG, "Error while registering device on GCM/Friends24");
                    Utils.showToast(getContext(), result);
                }
            }


        }
    }

    public class UpdateFacebookIdTask extends AsyncTask<JSONObject, Void, Void> {
        public static final String URI = "userProfile";

        public UpdateFacebookIdTask(Context context) {
            super(context, URI, HttpPost.METHOD_NAME, createUpdateFacebookIdRequest(context) , false, false);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Void> result) {
            if (result.getStatus() != AsyncTaskResult.Status.OK) {
                Utils.showToast(getContext(), result);
            }
            else {
                SharedPreferences.Editor edit = RegistrationUtils.getGCMPreferences(getContext()).edit();
                edit.putString(REGISTERED_FACEBOOK_ID, getApplication().getFriends24Context().getFbUser().getId());
                edit.putString(REGISTERED_FACEBOOK_USERNAME, getApplication().getFriends24Context().getLoggedUser());
                edit.commit();
                Log.d(TAG, "Facebook id if current user was updated");
            }
        }
    }

    private static JSONObject createUpdateFacebookIdRequest(Context context) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("facebookId", ((Friends24Application) context.getApplicationContext()).getFriends24Context().getFbUser().getId());
            return jsonObject;
        } catch (JSONException e) {
            Log.e(TAG, "Error", e);
            throw new RuntimeException(e);
        }
    }

}
