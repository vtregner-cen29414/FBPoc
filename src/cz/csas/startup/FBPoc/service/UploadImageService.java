package cz.csas.startup.FBPoc.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cz.csas.startup.FBPoc.Friends24Application;
import cz.csas.startup.FBPoc.R;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by cen29414 on 2.6.2014.
 */
public class UploadImageService extends IntentService {
    public static final String TAG="Friends24";

    public static final String IMAGE_URI = "imageUri";
    public static final String COLLECTION_ID = "collectionId";

    public UploadImageService() {
        super("UploadImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imageUri = intent.getStringExtra(IMAGE_URI);
        String collectionId = intent.getStringExtra(COLLECTION_ID);
        int id = 1;

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.imageUploadTitle))
                .setContentText(getString(R.string.imageUploadInProgress))
                .setSmallIcon(android.R.drawable.stat_sys_upload);
        mBuilder.setProgress(0, 0, true);
        mBuilder.setAutoCancel(false);
        mNotifyManager.notify(id, mBuilder.build());

        HttpClient httpClient = Friends24HttpClient.getNewHttpClient();
        Friends24Application application = (Friends24Application) getApplicationContext();
        HttpPost method = new HttpPost(getBaseUri()+"collections/"+collectionId+"/image");
        method.addHeader("Authorization", application.getAuthHeader());

        SimpleMultipartEntity entity = new SimpleMultipartEntity();
        method.setEntity(entity);
        FileInputStream fin = null;
        boolean ok = true;
        try {
            fin = new FileInputStream(imageUri);
            entity.addPart("file", "image.jpg", fin, "image/jpeg");
            HttpResponse response = httpClient.execute(method);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "Response status code:" + statusCode + "/" + response.getStatusLine().getReasonPhrase());

            switch (statusCode) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    Log.i(TAG, "Image uploaded succesfully");
                    break;
                 default:
                     Log.e(TAG, "Error while uploading image - " + statusCode);
                     ok = false;
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
            ok = false;
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.toString());
            ok = false;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            ok = false;
        }
        finally {
            if (fin != null) try {
                fin.close();
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }

        File file = new File(imageUri);
        if (file.exists()) file.delete();
        mBuilder.setProgress(0, 0, false);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentText(ok ? getString(R.string.imageUploadDone) : getString(R.string.imageUploadError));
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
        mNotifyManager.notify(id, mBuilder.build());
    }

    public String getBaseUri() {
        String baseUrl = getBaseContext().getString(R.string.friends24_server_url);
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        return baseUrl;
    }
}
