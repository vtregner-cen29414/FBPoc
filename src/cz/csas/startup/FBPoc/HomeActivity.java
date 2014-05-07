package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import cz.csas.startup.FBPoc.model.Account;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class HomeActivity extends Activity {

    private static final String TAG = "Friends24";

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    private static final String FRIENDS_KEY = "friends";

    UiLifecycleHelper uiHelper;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    //AccountsAdapter adapter;
    private boolean isFetching=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Find the user's profile picture custom view
        profilePictureView = (ProfilePictureView) findViewById(R.id.currentUser_profile_pic);
        profilePictureView.setCropped(true);


// Find the user's name view
        userNameView = (TextView) findViewById(R.id.currentUser);

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);

        //adapter = new AccountsAdapter(this, R.layout.account_row);
        Friends24Application application = (Friends24Application) getApplication();
        if (application.getAccounts() != null) {
            //adapter.setData(application.getAccounts());
            appendAccountsView(application.getAccounts());
        }
        else {
            new GetAccountsTask(this).execute();
        }

        //setListAdapter(adapter);

        //ensureOpenSession();
        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
            GraphUser fbUser = ((Friends24Application) getApplication()).getFbUser();
            profilePictureView.setProfileId(fbUser.getId());
            userNameView.setText(fbUser.getName());
        }
        else {
            Log.e(TAG, "FB session not opened!");
        }

    }

    private void appendAccountsView(List<Account> accounts) {
        if (accounts.size() > 0) {
            LinearLayout accountListView = (LinearLayout) findViewById(R.id.accountList);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Account account : accounts) {
                View view = inflater.inflate(R.layout.account_row, null);
                TextView aView = (TextView) view.findViewById(R.id.accountNumber);
                TextView aType = (TextView) view.findViewById(R.id.accountType);
                TextView aBalance = (TextView) view.findViewById(R.id.accountBalance);

                StringBuilder a = new StringBuilder();
                if (account.getPrefix() != null) a.append(account.getPrefix()).append("-");
                a.append(account.getNumber()).append("/0800");
                aView.setText(a.toString());
                aType.setText(account.getType());
                aBalance.setText(account.getBalance().toString() + " " + account.getCurrency());
                accountListView.addView(view);
            }
        }

    }


    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        /*if (state.isOpened() && !isFetching) {
            Log.i(TAG, "Logged in...");
            isFetching = true;
            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    isFetching = false;
                    if (user != null) {
                        profilePictureView.setProfileId(user.getId());
                        userNameView.setText(user.getName());
                    }
                }
            }).executeAsync();

        } else if (state.isClosed()) {
            Log.i(T*//*AG, "Logged out...");
        }*/
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

    private class GetAccountsTask extends Friend24AsyncTask<Void, Void, List<Account>> {

        private GetAccountsTask(Context context) {
            super(context);
        }

        Exception ex;

        @Override
        protected List<Account> doInBackground(Void... params) {
            try {
                String url = getBaseUrl() + "accounts";
                HttpGet httpReq = new HttpGet(url);
                HttpClient client = getHttpClient();
                HttpResponse response = client.execute(httpReq);
                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(TAG, "Response status code:" + statusCode + "/" + response.getStatusLine().getReasonPhrase());
                if (statusCode == HttpStatus.SC_OK) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    String responseContent = new String(outputStream.toByteArray(), HTTP.UTF_8);
                    Log.d(TAG, "Response received:");
                    Log.d(TAG, responseContent);
                    JSONObject r = new JSONObject(responseContent);
                    JSONArray jaccounts = r.getJSONArray("accounts");
                    List<Account> accounts = new ArrayList<Account>();
                    for (int i=0; i< jaccounts.length(); i++) {
                        JSONObject jaccount = jaccounts.getJSONObject(i);
                        Account account = new Account();
                        if (jaccount.has("prefix")) account.setPrefix(jaccount.getLong("prefix"));
                        account.setNumber(jaccount.getLong("number"));
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
                else {
                    //showError(String.valueOf(statusCode), null);
                    ex = new RuntimeException("Invalid response: " + statusCode);
                    return null;
                }

            } catch (Exception e) {
                //showError(null, e);
                ex = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Account> accounts) {
            super.onPostExecute(accounts);
            if (ex != null) {
                showError("Chyba", ex);
            }
            else {
                //adapter.setData(accounts);
                ((Friends24Application) HomeActivity.this.getApplication()).setAccounts(accounts);
                appendAccountsView(accounts);
            }
        }





    }
}
