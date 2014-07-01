package cz.csas.startup.FBPoc.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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
            Intent serviceIntent = new Intent(context, NotificationService.class);
            serviceIntent.putExtras(intent.getExtras());
            context.startService(serviceIntent);
        }

        setResultCode(Activity.RESULT_OK);
    }
}