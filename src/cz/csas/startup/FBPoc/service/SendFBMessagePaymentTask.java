package cz.csas.startup.FBPoc.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.facebook.Session;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.SASLXFacebookPlatformMechanism;
import cz.csas.startup.FBPoc.model.Payment;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

public class SendFBMessagePaymentTask extends AsyncTask<Payment, Void, Void> {
    private static final String TAG = "Friends24";

    private Exception ex;
    ProgressDialog progressDialog;
    private Context context;
    OnTaskCompleteListener<Void> listener;


    public SendFBMessagePaymentTask(Context context, ProgressDialog progressDialog, OnTaskCompleteListener<Void> listener) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.listener = listener;

    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Payment... params) {
        Payment payment = params[0];
        ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
        config.setSASLAuthenticationEnabled(true);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        config.setSendPresence(false);
        XMPPConnection xmpp = new XMPPConnection(config);
        try {
            xmpp.connect();
            xmpp.login(Session.getActiveSession().getApplicationId(), Session.getActiveSession().getAccessToken(), "Application");

            //send a chat message
            ChatManager chatmanager = xmpp.getChatManager();
            Chat newChat = chatmanager.createChat("-" + payment.getRecipientId() + "@chat.facebook.com", new MessageListener() {
                @Override
                public void processMessage(Chat chat, Message msg) {
                    Log.d(TAG, "message sent = " + msg);
                }
            });
            Message message = new Message();
            StringBuilder body = new StringBuilder();
            body.append(payment.getNote()).append("\n\n");

            if (payment.getStatus() == Payment.Status.PENDING) {
                // we don't know account number, user must enter it
                body.append(getContext().getString(R.string.fb_message_accout_not_known)).append("\n");
                body.append(getContext().getString(R.string.friends24_server_payment_accept_url)).append(payment.getId());
            }
            else if (payment.getStatus() == Payment.Status.ACCEPTED) {
                // we know account number, payment was already proceeded
                body.append(getContext().getString(R.string.fb_message_account_known)).append("\n");
                body.append(getContext().getString(R.string.friends24_server_payment_accept_url)).append(payment.getId());

            }
            else {
                // could not happen
                return null;
            }



            message.setBody(body.toString());
            newChat.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
            ex = e;
        }
        finally {
            xmpp.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
        if (ex == null) {
            if (listener != null) listener.onTaskComplete(aVoid);
        }
        else {
            if (listener != null) listener.onTaskError(ex);
        }

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}