package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.SwipeAccountSelector;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class PaymentsActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";

    private ExpandablePaymentsAdapter paymentsAdapter;
    private SwipeAccountSelector accountSelector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getFriendsApplication().getFriends24Context().isAppLogged()) return;
        setContentView(R.layout.payment_list);

        final Friends24Application application = (Friends24Application) getApplication();


        accountSelector = (SwipeAccountSelector) findViewById(R.id.accountSelector);
        accountSelector.setAccounts(R.layout.account_selector, application.getFriends24Context().getAccounts());
        accountSelector.setOnItemSelectedListener(new SwipeAccountSelector.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Account account, View view, int position) {
                if (account != null) {
                    if (application.getFriends24Context().getPayments().get(account) == null) {
                        new GetPaymentsTask(PaymentsActivity.this, account, paymentsAdapter).execute();
                    } else {
                        appendPayments(account);
                    }
                }
            }
        });


        paymentsAdapter = new ExpandablePaymentsAdapter(this, R.layout.payment_row, R.layout.payment_row_expanded);
        ExpandableListView listView = (ExpandableListView) findViewById(android.R.id.list);
        listView.addHeaderView(getLayoutInflater().inflate(R.layout.payment_list_header, null));
        listView.setAdapter(paymentsAdapter);

        if (application.getFriends24Context().getPayments() == null) {
            application.getFriends24Context().setPayments(new HashMap<Account, List<Payment>>(application.getFriends24Context().getAccounts().size()));
            for (Account account : application.getFriends24Context().getAccounts()) {
                application.getFriends24Context().getPayments().put(account, null);
            }
            application.saveSessionToPreferences();
        }

        setupDrawer();
    }

    private void appendPayments(Account account) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        ListView listView = (ListView) findViewById(android.R.id.list);
        paymentsAdapter.setData(getFriendsApplication().getFriends24Context().getPayments().get(account));
        paymentsAdapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onResume() {
        super.onResume();
        Account account = (Account) accountSelector.getSelectedItem();
        if (account != null) {
            if (getFriendsApplication().getFriends24Context().getPayments() != null && getFriendsApplication().getFriends24Context().getPayments().get(account) == null) {
                new GetPaymentsTask(PaymentsActivity.this, account, paymentsAdapter).execute();
            }
            else appendPayments(account);
        }
    }

    public void onNewPayment(View view) {
        Intent intent = new Intent(this, NewPaymentActivity.class);
        intent.putExtra("account", accountSelector.getSelectedItemPosition());
        startActivity(intent);
        
    }

    private static class GetPaymentsTask extends AsyncTask<Void, Void, List<Payment>> {
        public static final String URI = "payments/";

        Account account;
        ProgressBar progressBar;
        ExpandablePaymentsAdapter paymentsAdapter;
        ListView listView;

        private GetPaymentsTask(Context context, Account account, ExpandablePaymentsAdapter paymentsAdapter) {
            super(context, URI+account.getId(), HttpGet.METHOD_NAME, null, true, true);
            this.account = account;
            this.paymentsAdapter = paymentsAdapter;
        }

        @Override
        public List<Payment> parseResponseObject(JSONObject object) throws JSONException {
            JSONArray jpayments = object.getJSONArray("payments");
            List<Payment> payments = new ArrayList<Payment>();
            for (int i=0; i< jpayments.length(); i++) {
                JSONObject jpayment = jpayments.getJSONObject(i);
                Payment payment = new Payment();
                payment.setId(jpayment.getString("id"));
                payment.setRecipientId(jpayment.getString("recipientId"));
                payment.setRecipientName(jpayment.getString("recipientName"));
                payment.setAmount(new BigDecimal(jpayment.getString("amount")));
                payment.setCurrency(jpayment.getString("currency"));
                if (!jpayment.isNull("note")) payment.setNote(jpayment.getString("note"));
                payment.setPaymentDate(new Date(jpayment.getLong("created")));
                payment.setStatus(Payment.Status.valueOf(jpayment.getInt("status")));

                payments.add(payment);
            }
            return payments;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) ((Activity)getContext()).findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            listView = (ListView) ((Activity)getContext()).findViewById(android.R.id.list);
            listView.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Payment>> result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                if (getApplication().getFriends24Context().getPayments() != null) {
                    getApplication().getFriends24Context().getPayments().put(account, result.getResult());
                    paymentsAdapter.setData(result.getResult());
                    paymentsAdapter.notifyDataSetChanged();
                    getApplication().saveSessionToPreferences();
                }
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }
        }
    }
}
