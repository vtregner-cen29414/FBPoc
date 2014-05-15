package cz.csas.startup.FBPoc;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Utils;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class PaymentsActivity extends ListActivity {
    private static final String TAG = "Friends24";

    PaymentsAdapter paymentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_list);

        Spinner accountSpinner = (Spinner) findViewById(R.id.accountSelector);
        final Friends24Application application = (Friends24Application) getApplication();
        AccountsAdapter adapter = new AccountsAdapter(this, R.layout.account_selector);
        //adapter.setDropDownViewResource(R.layout.account_selector);
        accountSpinner.setAdapter(adapter);
        adapter.setData(application.getAccounts());

        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Account account = (Account) parent.getItemAtPosition(position);
                if (account != null) {
                    if (application.getPayments().get(account) == null) {
                        new GetPaymentsTask(PaymentsActivity.this, account, paymentsAdapter).execute();
                    }
                    else {
                        paymentsAdapter.setData(application.getPayments().get(account));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        paymentsAdapter = new PaymentsAdapter(this, R.layout.payment_row);
        setListAdapter(paymentsAdapter);
        getListView().addHeaderView(getLayoutInflater().inflate(R.layout.payment_list_header, null));

        if (application.getPayments() == null) {
            application.setPayments(new HashMap<Account, List<Payment>>(application.getAccounts().size()));
            for (Account account : application.getAccounts()) {
                application.getPayments().put(account, null);
            }
        }
    }

    public void onNewPayment(View view) {
        Intent intent = new Intent(this, NewPaymentActivity.class);
        Spinner accounts = (Spinner) findViewById(R.id.accountSelector);
        intent.putExtra("account", accounts.getSelectedItemPosition());
        startActivity(intent);
        
    }

    private static class GetPaymentsTask extends AsyncTask<Void, Void, List<Payment>> {
        public static final String URI = "payments/";

        Account account;
        ProgressDialog progressDialog;
        PaymentsAdapter paymentsAdapter;

        private GetPaymentsTask(Context context, Account account, PaymentsAdapter paymentsAdapter) {
            super(context, createUri(account), HttpGet.METHOD_NAME, null, true, true);
            this.account = account;
            this.paymentsAdapter = paymentsAdapter;
        }

        private static String createUri(Account account) {
            String url = URI;
            if (account.getPrefix() != null) url = url + account.getPrefix() + "-";
            url+=account.getNumber();
            return url;
        }


        @Override
        public List<Payment> parseResponseObject(JSONObject object) throws JSONException {
            JSONArray jpayments = object.getJSONArray("payments");
            List<Payment> payments = new ArrayList<Payment>();
            for (int i=0; i< jpayments.length(); i++) {
                JSONObject jpayment = jpayments.getJSONObject(i);
                Payment payment = new Payment();
                payment.setId(jpayment.getString("id"));
                payment.setRecipientId(jpayment.getString("recipientid"));
                payment.setRecipientName(jpayment.getString("recipientname"));
                payment.setAmount(new BigDecimal(jpayment.getString("amount")));
                payment.setCurrency(jpayment.getString("currency"));
                payment.setStatus(Payment.Status.valueOf(jpayment.getInt("status")));

                payments.add(payment);
            }
            return payments;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), getContext().getString(R.string.loading), null);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Payment>> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                getApplication().getPayments().put(account, result.getResult());
                paymentsAdapter.setData(result.getResult());
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }
        }
    }
}
