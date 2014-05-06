package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.GraphUser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 6.5.2014.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "Friends24";

    private boolean isFetching=false;
    UiLifecycleHelper uiHelper;

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
        new LoginTask(this).execute(username.getText().toString().trim(), password.getText().toString().trim());
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


    public class LoginTask extends Friend24AsyncTask<String, Void, Boolean> {
        Exception ex;

        protected LoginTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                HttpPost httpPost = new HttpPost(getBaseUrl() + "login");
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
                parameters.add(new BasicNameValuePair("username", params[0]));
                parameters.add(new BasicNameValuePair("password", params[1]));
                httpPost.setEntity(new UrlEncodedFormEntity(parameters));
                HttpResponse response = getHttpClient().execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(TAG, "Response status code:" + statusCode + "/" + response.getStatusLine().getReasonPhrase());
                if (statusCode == HttpStatus.SC_OK) {
                    return true;
                }
                else {
                    ex = new RuntimeException("Invalid response: " + statusCode);
                    return null;
                }

            } catch (Exception e) {
                ex = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (ex != null) {
                showError("Chyba", ex);
            }
            else {
                Log.i(TAG, "User log in into Friend24 successfully!");
                Friends24Application application = (Friends24Application) LoginActivity.this.getApplication();
                application.setAppLogged(true);
                // login to FB
                if (LoginActivity.this.ensureOpenSession() && application.getFbUser() != null) {
                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    // home screen is always on the top
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);
                }
            }
        }
    }
}
