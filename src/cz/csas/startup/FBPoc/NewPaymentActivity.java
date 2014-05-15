package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;

import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class NewPaymentActivity extends Activity {
    private static final String TAG = "Friends24";
    private static final int PICK_FRIENDS_ACTIVITY = 1;

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_payment);

        Spinner accountSpinner = (Spinner) findViewById(R.id.accountSelector);
        final Friends24Application application = (Friends24Application) getApplication();
        AccountsAdapter adapter = new AccountsAdapter(this, R.layout.account_selector);
        accountSpinner.setAdapter(adapter);
        adapter.setData(application.getAccounts());
        Intent intent = getIntent();
        accountSpinner.setSelection(intent.getIntExtra("account", 0));

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);
    }

    public void onSelectRecipient(View view) {
        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
            Intent intent = new Intent();
            intent.setData(PickerActivity.FRIEND_PICKER);
            intent.setClass(this, PickerActivity.class);
            startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
        }
        else {
            Log.e(TAG, "FB session not opened!");
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (session.isClosed()) {
            Log.i(TAG, "FB session closed, redirect to login?");
        }
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
        if (requestCode == PICK_FRIENDS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            List<GraphUser> selectedFrieds = ((Friends24Application) getApplication()).getSelectedFrieds();
            //fillSelectedFriend(selectedFrieds);
        }
        else uiHelper.onActivityResult(requestCode, resultCode, data);
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
}
