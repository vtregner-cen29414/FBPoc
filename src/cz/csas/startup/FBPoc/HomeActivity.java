package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import cz.csas.startup.FBPoc.model.Account;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.SmackAndroid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class HomeActivity extends ListActivity {

    private static final String TAG = "Friends24";

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    private static final String FRIENDS_KEY = "friends";

    UiLifecycleHelper uiHelper;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    AccountsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        //LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        //authButton.setReadPermissions(Arrays.asList("xmpp_login"));

        // Find the user's profile picture custom view
        //profilePictureView = (ProfilePictureView) findViewById(R.id.currentUser_profile_pic);
        //profilePictureView.setCropped(true);

        adapter = new AccountsAdapter(this, R.layout.account_row);
        //adapter.setData(createDummyData());
        setListAdapter(adapter);
        //etListShownNoAnimation(false);


// Find the user's name view
        userNameView = (TextView) findViewById(R.id.currentUser);

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);
        new GetAccountsTask().execute();

    }

    private List<Account> createDummyData() {
        List<Account> data = new ArrayList<Account>(2);
        Account a1 = new Account();
        a1.setNumber(123123L);
        a1.setType("Běžný účet");
        a1.setCurrency("Kč");
        a1.setBalance(new BigDecimal(12340));
        data.add(a1);

        Account a2 = new Account();
        a2.setNumber(4232242242L);
        a2.setType("Spořící účet");
        a2.setCurrency("Kč");
        a2.setBalance(new BigDecimal(3340));
        data.add(a2);
        return data;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");

            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        //profilePictureView.setProfileId(user.getId());
                        userNameView.setText(user.getName());
                        //setComponentsVisibility(View.VISIBLE);
                        //SmackAndroid.init(HomeActivityOld.this);
                    }
                }
            }).executeAsync();
            //findViewById(R.id.send).setVisibility(View.VISIBLE);

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            //setComponentsVisibility(View.GONE);
            //findViewById(R.id.send).setVisibility(View.GONE);
        }
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

        Exception ex;

        @Override
        protected List<Account> doInBackground(Void... params) {
            try {
                String url = "http://friends24.apiary-mock.com/accounts";
                HttpGet httpReq = new HttpGet(url);
                HttpClient client = getNewHttpClient();
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
                adapter.setData(accounts);
            }
        }

        public void showError(String message, Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            String m = message != null ? message : "";
            m+=e != null ? e.getMessage() : "";
            builder.setTitle(R.string.error_dialog_title).
                    setMessage("Chyba: " + m);
            builder.show();
        }

        public HttpClient getNewHttpClient() {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 30000);
            params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);

            /*SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            try {
                if (!context.getResources().getString(R.string.deployment).equals("PROD")) {
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);

                    SSLSocketFactory sf = new NoValidatingSSLSocketFactory(trustStore);
                    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    registry.register(new Scheme("https", sf, 443));
                }
                else {
                    registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                }

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
                return new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                return new DefaultHttpClient();
            }*/
        }

    }
}
