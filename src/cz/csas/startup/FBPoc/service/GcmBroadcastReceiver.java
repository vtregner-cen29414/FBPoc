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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            Intent serviceIntent = new Intent(context, DownloadFBProfilePhotoService.class);
            serviceIntent.putExtras(intent.getExtras());
            context.startService(serviceIntent);
        }

        setResultCode(Activity.RESULT_OK);
    }
}