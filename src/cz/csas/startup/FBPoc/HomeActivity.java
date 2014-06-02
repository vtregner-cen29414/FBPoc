package cz.csas.startup.FBPoc;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class HomeActivity extends FbAwareActivity {

    private static final String TAG = "Friends24";

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    private static final String FRIENDS_KEY = "friends";

    private RoundedProfilePictureView profilePictureView;
    //private ImageView profilePictureView;
    private TextView userNameView;
    //AccountsAdapter adapter;
    private boolean isFetching=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Friends24Application application = (Friends24Application) getApplication();
        if (application.getAccounts() == null || application.getAuthHeader() == null) {
            Utils.redirectToLogin(this);
            return;
        }

        // Find the user's profile picture custom view
        profilePictureView = (RoundedProfilePictureView) findViewById(R.id.currentUser_profile_pic);
        profilePictureView.setCropped(true);
        profilePictureView.setBorderColor(getResources().getColor(R.color.profileBorderColor));
        profilePictureView.setBorderWidth(5f);


// Find the user's name view
        userNameView = (TextView) findViewById(R.id.currentUser);

        //adapter = new AccountsAdapter(this, R.layout.account_row);

        if (application.getAccounts() != null) {
            appendAccountsView(application.getAccounts());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureOpenFacebookSession();
    }

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
                aBalance.setText(Utils.getFormattedAmount(account.getBalance(), account.getCurrency()));
                accountListView.addView(view);
            }
        }

    }


    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
        super.onSessionStateChange(session, state, exception);
        if (state.isOpened()) {
            GraphUser fbUser = ((Friends24Application) getApplication()).getFbUser();
            profilePictureView.setProfileId(fbUser.getId());
            userNameView.setText(fbUser.getName().toUpperCase());
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFriendsApplication().clearSession();
    }
}
