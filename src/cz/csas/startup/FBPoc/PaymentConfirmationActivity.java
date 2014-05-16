package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.ProfilePictureView;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Payment;

/**
 * Created by cen29414 on 16.5.2014.
 */
public class PaymentConfirmationActivity extends Activity {
    private static final String TAG = "Friends24";

    private TextView recipientName;
    private ProfilePictureView recipientPicture;
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_confirmation);
        TextView newPayment = (TextView) findViewById(R.id.btnNewPayment);
        newPayment.setText(Html.fromHtml(getString(R.string.new_payment_link)));

        Intent intent = getIntent();
        Payment payment = (Payment) intent.getSerializableExtra("data");

        TextView accountRow1 = (TextView) findViewById(R.id.accountRow1);
        TextView accountRow2 = (TextView) findViewById(R.id.accountRow2);
        Friends24Application application = (Friends24Application) getApplication();
        Account account = application.getAccount(payment.getSenderAccount());
        accountRow1.setText(AccountsAdapter.getAccountRow1(account));
        accountRow2.setText(AccountsAdapter.getAccountRow2(account));

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);

        recipientName = (TextView) findViewById(R.id.recipent_name);
        recipientPicture = (ProfilePictureView) findViewById(R.id.recipient_pic);
        recipientPicture.setCropped(true);


        recipientName.setText(payment.getRecipientName());
        recipientPicture.setProfileId(payment.getRecipientId());
    }

    public void onNewPayment(View view) {
        Intent intent = new Intent(this, NewPaymentActivity.class);
        startActivity(intent);
        finish();
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
}
