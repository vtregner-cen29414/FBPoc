package cz.csas.startup.FBPoc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import cz.csas.startup.FBPoc.utils.Utils;
import org.jivesoftware.smack.SmackAndroid;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 28.5.2014.
 */
public abstract class FbAwareActivity extends Activity {
    protected UiLifecycleHelper uiHelper;
    private static final String TAG = "Friends24";
    protected SmackAndroid smackAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ":onCreate");

        Friends24Application application = (Friends24Application) getApplication();
        if (!application.getFriends24Context().isAppLogged() || application.getFriends24Context().getAccounts() == null || application.getFriends24Context().getAuthHeader() == null) {
            finish();
            Utils.redirectToLogin(this);
            return;
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);
    }

    protected void initSmack() {
        smackAndroid = SmackAndroid.init(this);
        Log.d(TAG, "smack library initilized for activity " + this.getClass().getSimpleName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, getClass().getSimpleName() + ":onResume");
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

    protected boolean ensureOpenFacebookSession() {
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

    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {

    }


    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (uiHelper != null) uiHelper.onDestroy();
        if (smackAndroid != null) smackAndroid.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    protected Friends24Application getFriendsApplication() {
        return (Friends24Application) getApplication();
    }
}
