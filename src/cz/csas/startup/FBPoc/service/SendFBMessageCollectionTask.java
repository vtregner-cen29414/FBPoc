package cz.csas.startup.FBPoc.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.facebook.Session;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.SASLXFacebookPlatformMechanism;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.FacebookCollectionParticipant;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

public class SendFBMessageCollectionTask extends AsyncTask<Collection, Void, Void> {
    private static final String TAG = "Friends24";

    private Exception ex;
    ProgressDialog progressDialog;
    private Context context;
    OnTaskCompleteListener<Void> listener;


    public SendFBMessageCollectionTask(Context context, ProgressDialog progressDialog, OnTaskCompleteListener<Void> listener) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.listener = listener;

    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Collection... params) {
        Collection collection = params[0];
        ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
        config.setSASLAuthenticationEnabled(true);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        config.setSendPresence(false);

        for (FacebookCollectionParticipant participant : collection.getFbParticipants()) {
            XMPPConnection xmpp = new XMPPConnection(config);
            try {
                xmpp.connect();
                xmpp.login(Session.getActiveSession().getApplicationId(), Session.getActiveSession().getAccessToken(), "Application");

                //send a chat message
                ChatManager chatmanager = xmpp.getChatManager();
                sendMessage(chatmanager, collection, participant);

            } catch (Exception e) {
                e.printStackTrace();
                ex = e;
            }
            finally {
                xmpp.disconnect();
            }
        }
        return null;
    }

    private void sendMessage(ChatManager chatmanager, Collection collection, FacebookCollectionParticipant recipient) {
        Chat newChat = chatmanager.createChat("-" + recipient.getFbUserId() + "@chat.facebook.com", new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message msg) {
                Log.d(TAG, "message sent = " + msg);
            }
        });
        Message message = new Message();
        StringBuilder body = new StringBuilder();
        body.append(context.getString(R.string.fbCollectionMessage1)).append(" '").append(collection.getName()).append("'.\n");
        body.append(context.getString(R.string.fbCollectionMessage2)).append("\n\n");
        body.append(getContext().getString(R.string.friends24_server_collection_accept_url))
            .append(collection.getId()).append("/")
            .append(recipient.getId());

        message.setBody(body.toString());
        try {
            newChat.sendMessage(message);
            Log.d(TAG, "Message for " + recipient.getFbUserId() + "/" + recipient.getFbUserName() + " sent!");
        } catch (XMPPException e) {
            Log.d(TAG, "Error sending Message for " + recipient.getFbUserId(), e);
            ex = e;
        }
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