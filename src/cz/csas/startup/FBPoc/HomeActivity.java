package cz.csas.startup.FBPoc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import cz.csas.startup.FBPoc.model.Account;

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

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

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
            appendAccountsView(application.getAccounts());
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

    /*private void appendAccountsView(List<Account> accounts) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (accounts.size() > 0) {
            LinearLayout accountListView = (LinearLayout) findViewById(R.id.accountList);
            LayoutInflater inflater = LayoutInflater.from(this);
            int row=1;
            for (Account account : accounts) {
                View view = inflater.inflate(R.layout.account_row, null);
                TextView aView = (TextView) view.findViewById(R.id.accountNumber);
                TextView aType = (TextView) view.findViewById(R.id.accountType);
                TextView aBalance = (TextView) view.findViewById(R.id.accountBalance);
                Drawable background = (row++%2 == 0) ? getResources().getDrawable(R.color.cell_even) : getResources().getDrawable(R.color.cell_odd);
                view.findViewById(R.id.rowMarkColor).setBackground(background);

                StringBuilder a = new StringBuilder();
                if (account.getPrefix() != null) a.append(account.getPrefix()).append("-");
                a.append(account.getNumber()).append("/0800");
                aView.setText(a.toString());
                aType.setText(account.getType());
                aBalance.setText(account.getBalance().toString() + " " + account.getCurrency());
                accountListView.addView(view);
            }
        }

    }*/


    private void appendAccountsView(List<Account> accounts) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (accounts.size() > 0) {
            TableLayout accountListView = (TableLayout) findViewById(R.id.accountList);
            LayoutInflater inflater = LayoutInflater.from(this);
            int row=1;
            for (Account account : accounts) {
                View view = inflater.inflate(R.layout.account_row, null);
                TextView aView = (TextView) view.findViewById(R.id.accountNumber);
                TextView aType = (TextView) view.findViewById(R.id.accountType);
                TextView aBalance = (TextView) view.findViewById(R.id.accountBalance);
                Drawable background = (row++%2 == 0) ? getResources().getDrawable(R.color.cell_even) : getResources().getDrawable(R.color.cell_odd);
                view.findViewById(R.id.rowMarkColor).setBackground(background);

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

    public void onPayments(View view) {
        ((Friends24Application) getApplication()).setPayments(null);
        Intent intent = new Intent(this, PaymentsActivity.class);
        startActivity(intent);
    }

    public void onCollections(View view) {
        ((Friends24Application) getApplication()).setCollections(null);
        Intent intent = new Intent(this, CollectionsActivity.class);
        startActivity(intent);
    }


}
