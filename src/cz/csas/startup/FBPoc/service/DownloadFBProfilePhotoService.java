package cz.csas.startup.FBPoc.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import cz.csas.startup.FBPoc.CollectionDetailActivity;
import cz.csas.startup.FBPoc.Friends24Application;
import cz.csas.startup.FBPoc.PaymentsActivity;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.model.CollectionParticipant;
import cz.csas.startup.FBPoc.model.EmailCollectionParticipant;
import cz.csas.startup.FBPoc.model.FacebookCollectionParticipant;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.utils.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cen29414 on 2.6.2014.
 */
public class DownloadFBProfilePhotoService extends IntentService {
    public static final String TAG="Friends24";


    public DownloadFBProfilePhotoService() {
        super("DownloadFBProfilePhotoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "DownloadFBProfilePhotoService started");

        String notificationType = intent.getStringExtra("notificationType");
        if (Constants.NOTIFICATION_PAYMENT_STATUS_CHANGED.equals(notificationType)) {
            Payment payment = new Payment();
            payment.setId(intent.getStringExtra("id"));
            payment.setNote(intent.getStringExtra("note"));
            payment.setRecipientName(intent.getStringExtra("recipientName"));
            payment.setStatus(Payment.Status.valueOf(Integer.valueOf(intent.getStringExtra("status"))));
            sendNotification(this, notificationType, payment);
        }
        else if (Constants.NOTIFICATION_COLLECTION_PARTICIPANT_STATUS_CHANGED.equals(notificationType)) {
            boolean isFbParticipant = Boolean.parseBoolean(intent.getStringExtra("isFbParticipant"));
            CollectionParticipant participant = isFbParticipant ? new FacebookCollectionParticipant() : new EmailCollectionParticipant();
            if (isFbParticipant) {
                ((FacebookCollectionParticipant) participant).setFbUserName(intent.getStringExtra("fbName"));
                ((FacebookCollectionParticipant) participant).setFbUserId(intent.getStringExtra("fbId"));
            }
            else {
                ((EmailCollectionParticipant) participant).setEmail(intent.getStringExtra("email"));
            }
            participant.setStatus(CollectionParticipant.Status.valueOf(Integer.valueOf(intent.getStringExtra("status"))));
            CollectionParticipantStatus collectionParticipantStatus = new CollectionParticipantStatus();
            collectionParticipantStatus.participant = participant;
            collectionParticipantStatus.id =  intent.getStringExtra("id");
            collectionParticipantStatus.name= intent.getStringExtra("name");
            sendNotification(this, notificationType, collectionParticipantStatus);
        }
        else {
            Log.e(TAG, "Unsupported notification: '" + notificationType + "'");
        }

    }

    // Put the GCM message into a notification and post it.
    private void sendNotification(Context context, String notificationType, Object data) {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Class activityForNotification = getActivityForNotification(notificationType);
        Intent intent = new Intent(this, activityForNotification);
        //intent.putExtra(Constants.NOTIFICATION_DATA, data);
        //intent.putExtra(Constants.FROM_NOTIFICATION, true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        // TODO nefunguje, backstack se do aplikace nejak nedostane. Prozatim reseno vytvorenim backstacku primo v LoginTask pri presmerovani
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(activityForNotification);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrationPattern = { 0, 200, 150, 200, 150, 300};  // tree short vibrations
        Bitmap bm = getLargeIcon(data);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_bw)
                        .setLargeIcon(bm)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setLights(0xff0169b3, 100, 3000) // blue
                        .setVibrate(vibrationPattern)
                        .setDefaults(Notification.DEFAULT_SOUND);

        updateNotificationBuilder(mBuilder, notificationType, data);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }



    private Bitmap getLargeIcon(Object data) {
        if (data instanceof CollectionParticipantStatus && ((CollectionParticipantStatus) data).participant instanceof FacebookCollectionParticipant) {
            String fbUserId = ((FacebookCollectionParticipant) ((CollectionParticipantStatus) data).participant).getFbUserId();
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                URL url = null;
                try {
                    int height = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
                    int width = (int) getResources().getDimension(android.R.dimen.notification_large_icon_width);
                    Log.d(TAG, "DIM: " + height+"/"+width);
                    url = new URL("http://graph.facebook.com/"+ fbUserId + "/picture?height="+height);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setInstanceFollowRedirects(true);
                    conn.setDoInput(false);
                    // Starts the query
                    conn.connect();
                    int status = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + status);
                    if (status != HttpURLConnection.HTTP_OK) {
                        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_SEE_OTHER) {

                            // get redirect url from "location" header field
                            String newUrl = conn.getHeaderField("Location");
                            Log.d(TAG, "Redirecting to " + newUrl);

                            // open the new connnection again
                            conn = (HttpURLConnection) new URL(newUrl).openConnection();
                            conn.setReadTimeout(10000 /* milliseconds */);
                            conn.setConnectTimeout(15000 /* milliseconds */);
                            conn.setRequestMethod("GET");
                        }
                    }

                    status = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + status);
                    if (status == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        Log.d(TAG, "User profile photo decoded: " + (bitmap != null));
                        if (bitmap != null) return bitmap;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "Cannot fetch facebook user photo", e);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot fetch facebook user photo", e);
                }

            }
        }

        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_bw);
    }

    private void updateNotificationBuilder(NotificationCompat.Builder builder, String notificationType, Object data) {
        if (notificationType.equals(Constants.NOTIFICATION_PAYMENT_STATUS_CHANGED)) {
            Payment order = (Payment) data;
            String contentText = order.getRecipientName();
            String statusMsg="";
            if (order.getStatus() == Payment.Status.ACCEPTED) statusMsg = "byla provedena";
            if (order.getStatus() == Payment.Status.REFUSED) statusMsg = "byla zamítnuta";
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .addLine("Platba " + order.getNote())
                    .addLine(statusMsg);

            String title = "Změna stavu platby";
            builder.setContentTitle(title)
                    .setStyle(inboxStyle)
                    .setContentText(contentText)
                    .setTicker(title) ;
        }
        else if (notificationType.equals(Constants.NOTIFICATION_COLLECTION_PARTICIPANT_STATUS_CHANGED)) {
            CollectionParticipantStatus participantStatus = (CollectionParticipantStatus) data;
            String partName = participantStatus.participant instanceof  FacebookCollectionParticipant ?
                    ((FacebookCollectionParticipant) participantStatus.participant).getFbUserName() :
                    ((EmailCollectionParticipant) participantStatus.participant).getEmail();

            String msg="";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.ACCEPTED) msg = "Slíbil příspět";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.REFUSED) msg = "Odmítl příspět";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.DONE) msg = "Příspěl";

            msg = msg + " na Vaši sbírku '" + participantStatus.name + "'";

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                    .bigText(msg);

            builder.setContentTitle(partName)
                    .setStyle(bigTextStyle)
                    .setContentText(msg)
                    .setTicker("Změna stavu sbírky. " + partName + " " + msg);
        }

    }

    private Class getActivityForNotification(String notificationType) {
        if (notificationType.equals(Constants.NOTIFICATION_PAYMENT_STATUS_CHANGED)) return PaymentsActivity.class;
        else if (notificationType.equals(Constants.NOTIFICATION_COLLECTION_PARTICIPANT_STATUS_CHANGED)) return CollectionDetailActivity.class;
        else return null;
    }

    private static class CollectionParticipantStatus {
        String id;
        String name;
        CollectionParticipant participant;
    }


}
