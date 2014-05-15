package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Utils;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 6.5.2014.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "Friends24";

    private boolean isFetching=false;
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        final TextView username = (TextView) findViewById(R.id.loginUsername);
        final TextView password = (TextView) findViewById(R.id.loginPassword);
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

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);
    }

    public void onLogin(View view) {
        final TextView username = (TextView) findViewById(R.id.loginUsername);
        final TextView password = (TextView) findViewById(R.id.loginPassword);
        Log.d(TAG, "onLogin");
        Friends24Application application = (Friends24Application) getApplication();
        String authorizationString = "Basic " + Base64.encodeToString((username.getText().toString().trim() + ":" + password.getText().toString().trim()).getBytes(), Base64.NO_WRAP);
        application.setAuthHeader(authorizationString);

        new GetAccountsTask(this).execute();
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
                        application.setFbUser(user);
                        if (application.isAppLogged()) {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            // home screen is always on the top
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LoginActivity.this.startActivity(intent);
                        }
                    }
                }
            }).executeAsync();

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
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
                if (jaccount.has("accountPrefix")) account.setPrefix(jaccount.getLong("accountPrefix"));
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
            progressDialog =ProgressDialog.show(getContext(), getContext().getResources().getString(R.string.logging), null);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Account>> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                getApplication().setAccounts(result.getResult());
                getApplication().setAppLogged(true);
                // login to FB
                if (LoginActivity.this.ensureOpenSession() && getApplication().getFbUser() != null) {
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

}
