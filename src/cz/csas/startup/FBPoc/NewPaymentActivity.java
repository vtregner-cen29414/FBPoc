package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.CreatePayment;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.service.OnTaskCompleteListener;
import cz.csas.startup.FBPoc.service.SendFBMessagePaymentTask;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;
import cz.csas.startup.FBPoc.widget.SwipeAccountSelector;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class NewPaymentActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";
    private static final int PICK_FRIENDS_ACTIVITY = 1;

    private TextView recipientName;
    private RoundedProfilePictureView recipientPicture;
    private EditText amountView;
    private EditText messageForRecipientView;
    private SwipeAccountSelector accountSelector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getFriendsApplication().getFriends24Context().isAppLogged()) return;
        setContentView(R.layout.new_payment);

        recipientName = (TextView) findViewById(R.id.recipent_name);
        recipientName.setText(Html.fromHtml(getString(R.string.selectRecipient)));

        recipientPicture = (RoundedProfilePictureView) findViewById(R.id.recipient_pic);
        recipientPicture.setCropped(true);

        accountSelector = (SwipeAccountSelector) findViewById(R.id.accountSelector);
        final Friends24Application application = (Friends24Application) getApplication();
        /*AccountsAdapter adapter = new AccountsAdapter(this, R.layout.account_selector);
        accountSelector.setAdapter(adapter);
        adapter.setData(application.getAccounts());*/
        accountSelector.setAccounts(R.layout.account_selector, application.getFriends24Context().getAccounts());
        Intent intent = getIntent();
        accountSelector.setSelection(intent.getIntExtra("account", 0));



        amountView = (EditText) findViewById(R.id.amount);
        messageForRecipientView = (EditText) findViewById(R.id.messageForRecipient);
        initSmack();
        setupDrawer();

    }

    public void onSelectRecipient(View view) {
        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
            Intent intent = new Intent();
            intent.setData(PickerActivity.FRIEND_PICKER);
            intent.setClass(this, PickerActivity.class);
            intent.putExtra(PickerActivity.MULTI_SELECTION, false);
            startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
        }
        else {
            Log.e(TAG, "FB session not opened!");
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FRIENDS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            List<GraphUser> selectedFrieds = ((Friends24Application) getApplication()).getFriends24Context().getNewlySelectedFrieds();
            if (selectedFrieds != null && selectedFrieds.size() > 0) {
                recipientName.setText(selectedFrieds.get(0).getName());
                recipientPicture.setProfileId(selectedFrieds.get(0).getId());
                recipientName.setError(null);
            }
        }
    }

    public void onCretePayment(View view) {
        if (validateFields()) {
            Friends24Application application = (Friends24Application) getApplication();
            CreatePayment createPayment = new CreatePayment();
            createPayment.setAmount(new BigDecimal(amountView.getText().toString()));
            createPayment.setRecipientId(application.getFriends24Context().getNewlySelectedFrieds().get(0).getId());
            createPayment.setRecipientName(application.getFriends24Context().getNewlySelectedFrieds().get(0).getName());
            createPayment.setNote(messageForRecipientView.getText().toString());
            createPayment.setSenderAccount(((Account) accountSelector.getSelectedItem()).getId());
            new CreatePaymentTask(this, createPayment).execute();
        }
    }

    private boolean validateFields() {
        boolean valid = true;
        if (amountView.getText() == null || amountView.getText().length() == 0) {
            amountView.setError(getString(R.string.amountError));
            valid = false;
        }
        else amountView.setError(null);

        if (messageForRecipientView.getText() == null || messageForRecipientView.getText().toString().trim().length() == 0) {
            messageForRecipientView.setError(getString(R.string.noteError));
            valid = false;
        }
        else messageForRecipientView.setError(null);

        if (recipientPicture.getProfileId() == null) {
            recipientName.setError(getString(R.string.recipientError));
            valid = false;
        }
        else recipientName.setError(null);
        return valid;
    }

    private class CreatePaymentTask extends AsyncTask<CreatePayment, Void, Payment> {
        public static final String URI = "addPaymentOrder";

        ProgressDialog progressDialog;
        CreatePayment createPayment;

        public CreatePaymentTask(Context context, CreatePayment payment) {
            super(context, URI, HttpPost.METHOD_NAME, payment, true, false);
            this.createPayment = payment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), null, getContext().getString(R.string.waitPlease));
        }

        @Override
        public Payment parseResponseObject(JSONObject object) throws JSONException {
            Payment payment = new Payment();
            payment.setId(object.getString("id"));
            payment.setPaymentDate(new Date(object.getLong("created")));
            payment.setSenderAccount(object.getLong("senderAccount"));
            payment.setRecipientId(object.getString("recipientId"));
            payment.setRecipientName(object.getString("recipientName"));
            payment.setAmount(new BigDecimal(object.getString("amount")));
            payment.setCurrency(object.getString("currency"));
            payment.setNote(object.getString("note"));
            payment.setStatus(Payment.Status.valueOf(object.getInt("status")));

            // TODO just becase use of mock
            payment.setRecipientId(createPayment.getRecipientId());
            payment.setAmount(createPayment.getAmount());
            payment.setNote(createPayment.getNote());
            payment.setSenderAccount(createPayment.getSenderAccount());
            return payment;
        }

        @Override
        protected void onPostExecute(final AsyncTaskResult<Payment> result) {
            super.onPostExecute(result);
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                SendFBMessagePaymentTask sendFBMessageTask = new SendFBMessagePaymentTask(getContext(), progressDialog, new OnTaskCompleteListener<Void>() {
                    @Override
                    public void onTaskComplete(Void aVoid) {
                        getFriendsApplication().getFriends24Context().getPayments().put((Account) accountSelector.getSelectedItem(), null); // clear cached data
                        getFriendsApplication().saveSessionToPreferences();
                        Intent intent = new Intent(getContext(), PaymentConfirmationActivity.class);
                        intent.putExtra("data", result.getResult());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onTaskError(Throwable throwable) {
                        Log.e(TAG, "Error while sending message to FB", throwable);
                        Utils.showMessage(getContext(), R.string.sendFBMessageError, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO handle this use case
                                finish();
                            }
                        });
                    }
                });
                sendFBMessageTask.execute(result.getResult());
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }
        }
    }

}
