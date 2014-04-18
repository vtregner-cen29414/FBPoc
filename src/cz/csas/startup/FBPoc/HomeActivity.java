package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {
    private static final String TAG = "Friends24";

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    private static final String FRIENDS_KEY = "friends";

    UiLifecycleHelper uiHelper;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;

    private ProfilePictureView selectedFriendProfilePictureView;
    private TextView selectedFriendName;


    private Button pickFriendButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);

        // Find the user's profile picture custom view
        profilePictureView = (ProfilePictureView) findViewById(R.id.currentUser_profile_pic);
        profilePictureView.setCropped(true);

// Find the user's name view
        userNameView = (TextView) findViewById(R.id.currentUser);
        pickFriendButton = (Button) findViewById(R.id.pickFriend);

        selectedFriendName = (TextView) findViewById(R.id.selected_friend_name);
        selectedFriendProfilePictureView = (ProfilePictureView) findViewById(R.id.selected_friend_profile_pic);

        List<GraphUser> selectedFrieds = ((Friends24Application) getApplication()).getSelectedFrieds();
        if (selectedFrieds != null) {
            fillSelectedFriend(selectedFrieds);
        }
        else {
            selectedFriendName.setVisibility(View.GONE);
            selectedFriendProfilePictureView.setVisibility(View.GONE);
        }

        setComponentsVisibility(View.GONE);

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);

    }

    private void setComponentsVisibility(int visibility) {
        profilePictureView.setVisibility(visibility);
        userNameView.setVisibility(visibility);
        pickFriendButton.setVisibility(visibility);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FRIENDS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            List<GraphUser> selectedFrieds = ((Friends24Application) getApplication()).getSelectedFrieds();
            fillSelectedFriend(selectedFrieds);
        }
        else {
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fillSelectedFriend(List<GraphUser> selectedFrieds) {
        if (selectedFrieds != null) {
            selectedFriendProfilePictureView.setProfileId(selectedFrieds.get(0).getId());
            selectedFriendProfilePictureView.setVisibility(View.VISIBLE);
            selectedFriendName.setText(selectedFrieds.get(0).getName());
            selectedFriendName.setVisibility(View.VISIBLE);
        }
    }


    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");

            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        profilePictureView.setProfileId(user.getId());
                        userNameView.setText(user.getName());
                        setComponentsVisibility(View.VISIBLE);
                    }
                }
            }).executeAsync();

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            setComponentsVisibility(View.GONE);
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


    public void onPickFriend(View view) {
        startPickerActivity(PickerActivity.FRIEND_PICKER, PICK_FRIENDS_ACTIVITY);
    }

    private void startPickerActivity(Uri data, int requestCode) {
        Intent intent = new Intent();
        intent.setData(data);
        intent.setClass(this, PickerActivity.class);
        startActivityForResult(intent, requestCode);
    }


    private List<GraphUser> restoreByteArray(byte[] bytes) {
        try {
            @SuppressWarnings("unchecked")
            List<String> usersAsString =
                    (List<String>) (new ObjectInputStream
                            (new ByteArrayInputStream(bytes)))
                            .readObject();
            if (usersAsString != null) {
                List<GraphUser> users = new ArrayList<GraphUser>
                        (usersAsString.size());
                for (String user : usersAsString) {
                    GraphUser graphUser = GraphObject.Factory
                            .create(new JSONObject(user),
                                    GraphUser.class);
                    users.add(graphUser);
                }
                return users;
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Unable to deserialize users.", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to deserialize users.", e);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to deserialize users.", e);
        }
        return null;
    }

    private byte[] getByteArray(List<GraphUser> users) {
        // convert the list of GraphUsers to a list of String
        // where each element is the JSON representation of the
        // GraphUser so it can be stored in a Bundle
        List<String> usersAsString = new ArrayList<String>(users.size());

        for (GraphUser user : users) {
            usersAsString.add(user.getInnerJSONObject().toString());
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(outputStream).writeObject(usersAsString);
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Unable to serialize users.", e);
        }
        return null;
    }

}
