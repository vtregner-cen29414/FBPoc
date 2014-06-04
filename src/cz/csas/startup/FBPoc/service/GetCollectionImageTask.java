package cz.csas.startup.FBPoc.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import cz.csas.startup.FBPoc.utils.Utils;
import org.apache.http.client.methods.HttpGet;

/**
* Created by cen29414 on 4.6.2014.
*/
public class GetCollectionImageTask extends AsyncTask<String, Void, byte[]> {
    private static final String TAG = "Friends24";
    public static final String URI = "collections/{id}/image";
    private Bitmap bitmap;

    public GetCollectionImageTask(Context context, String collectionId, Integer size) {
        super(context, getUri(collectionId, size), HttpGet.METHOD_NAME, null, false, false);
        getHttpClient().setBinaryResponse(true);
    }

    private static String getUri(String collectionId, Integer size) {
        String uri = URI.replace("{id}", collectionId);
        return size != null ? uri+"?size="+size : uri;
    }

    @Override
    protected AsyncTaskResult<byte[]> doInBackground(String... params) {
        AsyncTaskResult<byte[]> result =  super.doInBackground(params);
        if (result.getStatus() == AsyncTaskResult.Status.OK) {
            byte[] bytes = result.getResult();
            if (!isMock()) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
            else {
                // try BASE64 - apiary mock
                try {
                    bytes = Base64.decode(result.getResult(), Base64.NO_WRAP);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "cannot decode image base64 data: " + e.getMessage());
                }
            }
        }
        return result;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<byte[]> result) {
        super.onPostExecute(result);
        if (!result.getStatus().equals(AsyncTaskResult.Status.OK)) {
            Utils.showErrorDialog(getContext(), result);
        }
    }
}
