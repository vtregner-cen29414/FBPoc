package cz.csas.startup.FBPoc.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import cz.csas.startup.FBPoc.CollectionDetailActivity;
import cz.csas.startup.FBPoc.PaymentsActivity;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.model.CollectionParticipant;
import cz.csas.startup.FBPoc.model.EmailCollectionParticipant;
import cz.csas.startup.FBPoc.model.FacebookCollectionParticipant;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.utils.Constants;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = "Friends24";
    Context ctx;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GcmBroadcastReceiver:receive");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        ctx = context;
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String notificationType = intent.getStringExtra("notificationType");
            if (Constants.NOTIFICATION_PAYMENT_STATUS_CHANGED.equals(notificationType)) {
                Payment payment = new Payment();
                payment.setId(intent.getStringExtra("id"));
                payment.setNote(intent.getStringExtra("note"));
                payment.setRecipientName(intent.getStringExtra("recipientName"));
                payment.setStatus(Payment.Status.valueOf(intent.getIntExtra("status", 0)));
                sendNotification(notificationType, payment);
            }
            else if (Constants.NOTIFICATION_COLLECTION_PARTICIPANT_STATUS_CHANGED.equals(notificationType)) {
                boolean isFbParticipant = intent.getBooleanExtra("isFbParticipant", false);
                CollectionParticipant participant = isFbParticipant ? new FacebookCollectionParticipant() : new EmailCollectionParticipant();
                if (isFbParticipant) {
                    ((FacebookCollectionParticipant) participant).setFbUserName(intent.getStringExtra("fbName"));
                }
                else {
                    ((EmailCollectionParticipant) participant).setEmail(intent.getStringExtra("email"));
                }
                participant.setStatus(CollectionParticipant.Status.valueOf(intent.getIntExtra("status", 0)));
                CollectionParticipantStatus collectionParticipantStatus = new CollectionParticipantStatus();
                collectionParticipantStatus.participant = participant;
                collectionParticipantStatus.id =  intent.getStringExtra("id");
                collectionParticipantStatus.name= intent.getStringExtra("name");
                sendNotification(notificationType, collectionParticipantStatus);
            }
            else {
                Log.e(TAG, "Unsupported notification: '" + notificationType + "'");
            }
        }
        setResultCode(Activity.RESULT_OK);
    }

    // Put the GCM message into a notification and post it.
    private void sendNotification(String notificationType, Object data) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Class activityForNotification = getActivityForNotification(notificationType);
        Intent intent = new Intent(ctx, activityForNotification);
        //intent.putExtra(Constants.NOTIFICATION_DATA, data);
        //intent.putExtra(Constants.FROM_NOTIFICATION, true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        // TODO nefunguje, backstack se do aplikace nejak nedostane. Prozatim reseno vytvorenim backstacku primo v LoginTask pri presmerovani
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(activityForNotification);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrationPattern = { 0, 200, 150, 200, 150, 300};  // tree short vibrations
        Bitmap bm = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_about)
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
            String contentText = partName;

            String statusMsg="";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.ACCEPTED) statusMsg = "slíbil příspět";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.REFUSED) statusMsg = "odmítl příspět";
            if (participantStatus.participant.getStatus() == CollectionParticipant.Status.DONE) statusMsg = "příspěl";

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .addLine(statusMsg)
                    .addLine("na sbírku "+participantStatus.name);

            String title = "Změna stavu sbírky";
            builder.setContentTitle(title)
                    .setStyle(inboxStyle)
                    .setContentText(contentText)
                    .setTicker(title);
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