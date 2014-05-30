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
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

/**
 * Created by cen29414 on 16.5.2014.
 */
public class PaymentConfirmationActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";

    private TextView recipientName;
    private RoundedProfilePictureView recipientPicture;
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


        recipientName = (TextView) findViewById(R.id.recipent_name);
        recipientPicture = (RoundedProfilePictureView) findViewById(R.id.recipient_pic);
        recipientPicture.setCropped(true);


        recipientName.setText(payment.getRecipientName());
        recipientPicture.setProfileId(payment.getRecipientId());
    }

    public void onNewPayment(View view) {
        Intent intent = new Intent(this, NewPaymentActivity.class);
        startActivity(intent);
        finish();
    }
}
