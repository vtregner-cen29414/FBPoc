package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.Util;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.CreatePayment;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.service.OnTaskCompleteListener;
import cz.csas.startup.FBPoc.service.SendFBMessageTask;
import cz.csas.startup.FBPoc.utils.Utils;
import org.apache.http.client.methods.HttpPost;
import org.jivesoftware.smack.SmackAndroid;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class NewPaymentActivity extends Activity {
    private static final String TAG = "Friends24";
    private static final int PICK_FRIENDS_ACTIVITY = 1;

    private UiLifecycleHelper uiHelper;
    private TextView recipientName;
    private ProfilePictureView recipientPicture;
    private EditText amountView;
    private EditText messageForRecipientView;
    private Spinner accountSpinner;
    SmackAndroid smackAndroid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_payment);

        recipientName = (TextView) findViewById(R.id.recipent_name);
        recipientPicture = (ProfilePictureView) findViewById(R.id.recipient_pic);
        recipientPicture.setCropped(true);

        accountSpinner = (Spinner) findViewById(R.id.accountSelector);
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

        amountView = (EditText) findViewById(R.id.amount);
        messageForRecipientView = (EditText) findViewById(R.id.messageForRecipient);
        smackAndroid = SmackAndroid.init(NewPaymentActivity.this);

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
            recipientName.setText(selectedFrieds.get(0).getName());
            recipientPicture.setProfileId(selectedFrieds.get(0).getId());
            recipientName.setError(null);
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
    protected void onStop() {
        super.onStop();
        uiHelper.onStop();
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
        smackAndroid.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public void onCretePayment(View view) {
        validateFields();
        Friends24Application application = (Friends24Application) getApplication();
        CreatePayment createPayment = new CreatePayment();
        createPayment.setAmount(new BigDecimal(amountView.getText().toString()));
        createPayment.setRecipientId(application.getSelectedFrieds().get(0).getId());
        createPayment.setRecipientName(application.getSelectedFrieds().get(0).getName());
        createPayment.setNote(messageForRecipientView.getText().toString());
        createPayment.setSenderAccount(((Account)accountSpinner.getSelectedItem()).getId());
        new CreatePaymentTask(this, createPayment).execute();

    }

    private void validateFields() {
        if (amountView.getText() == null || amountView.getText().length() == 0) {
            amountView.setError(getString(R.string.amountError));
        }
        else amountView.setError(null);

        if (messageForRecipientView.getText() == null || messageForRecipientView.getText().toString().trim().length() == 0) {
            messageForRecipientView.setError(getString(R.string.noteError));
        }
        else messageForRecipientView.setError(null);

        if (recipientPicture.getProfileId() == null) {
            recipientName.setError(getString(R.string.recipientError));
        }
        else recipientName.setError(null);
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
            return payment;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Payment> result) {
            super.onPostExecute(result);
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                SendFBMessageTask sendFBMessageTask = new SendFBMessageTask(getContext(), progressDialog, new OnTaskCompleteListener<Void>() {
                    @Override
                    public void onTaskComplete(Void aVoid) {
                        // TODO
                        Toast.makeText(getContext(), "Platba uspesne odeslana", Toast.LENGTH_LONG).show();
                        ((Activity) getContext()).finishActivity(0);
                    }

                    @Override
                    public void onTaskError(Throwable throwable) {
                        Log.e(TAG, "Error while sending message to FB", throwable);
                        Utils.showMessage(getContext(), R.string.sendFBMessageError, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO handle this use case
                                ((Activity) getContext()).finishActivity(0);
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
