package cz.csas.startup.FBPoc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.utils.GothamFont;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cen29414 on 28.5.2014.
 */
public abstract class FbAwareActivity extends Activity {
    protected UiLifecycleHelper uiHelper;
    private static final String TAG = "Friends24";
    //protected SmackAndroid smackAndroid;
    protected ActionBarDrawerToggle mDrawerToggle;

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
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);

    }



    protected void setupDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        FrameLayout drawerContainer = (FrameLayout) findViewById(R.id.left_drawer);
        View drawerView = getLayoutInflater().inflate(R.layout.drawer, null);
        if (drawerContainer != null) {
            drawerContainer.addView(drawerView);
            // enable ActionBar app icon to behave as action to toggle nav drawer


            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                   // getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                  //  getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected void initSmack() {
       /* if (smackAndroid == null) {
            smackAndroid = SmackAndroid.init(this);
            Log.d(TAG, "smack library initilized for activity " + this.getClass().getSimpleName());
        }*/
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
        if (session.isOpened()) {
            GraphUser fbUser = ((Friends24Application) getApplication()).getFriends24Context().getFbUser();
            FrameLayout drawerContainer = (FrameLayout) findViewById(R.id.left_drawer);
            if (fbUser != null && drawerContainer != null) {
                RoundedProfilePictureView drawerUserPic = (RoundedProfilePictureView) drawerContainer.findViewById(R.id.dr_currentUser_profile_pic);
                drawerUserPic.setProfileId(fbUser.getId());
                TextView drUsername = (TextView) drawerContainer.findViewById(R.id.dr_currentUser);
                drUsername.setTypeface(GothamFont.BOLD);
                drUsername.setText(fbUser.getName().toUpperCase());
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, getClass().getSimpleName() + ":onPause");
        uiHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, getClass().getSimpleName() + ":onStop");
        uiHelper.onStop();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, getClass().getSimpleName() + ":onDestroy");
        if (uiHelper != null) uiHelper.onDestroy();
        /*if (smackAndroid != null) {
            smackAndroid.onDestroy();
            smackAndroid = null;
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    protected Friends24Application getFriendsApplication() {
        return (Friends24Application) getApplication();
    }

    public void onDrawerItemClick(View view) {
        if (view.getId() == R.id.dr_quickcheck) {
            launchPlayApplication("at.spardat.quickcheck");
        }
        else if (view.getId() == R.id.dr_netbanking) {
            launchPlayApplication("at.spardat.netbanking");
        }
        else if (view.getId() == R.id.dr_logout) {
            getFriendsApplication().getFriends24Context().clearSession();
            getFriendsApplication().invalidateSessionInPreferences();
            finish();
            Utils.redirectToLogin(this);
        }
    }

    public void launchPlayApplication(String appPackage) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + appPackage));
        startActivity(intent);
    }
}
