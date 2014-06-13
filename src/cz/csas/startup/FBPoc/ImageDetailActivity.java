package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.service.GetCollectionImageTask;
import cz.csas.startup.FBPoc.utils.Utils;

/**
 * Created by cen29414 on 20.5.2014.
 */
public class ImageDetailActivity extends Activity {
    public static final String COLLECTION_ID = "collectionId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!((Friends24Application) getApplication()).getFriends24Context().isAppLogged()) {
            finish();
            Utils.redirectToLogin(this);
            return;
        }
        setContentView(R.layout.imagedetail);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (imageView.getDrawable() == null) {
                    imageView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    new GetCollectionImageTask(ImageDetailActivity.this, getIntent().getStringExtra(COLLECTION_ID), Math.max(imageView.getWidth(), imageView.getHeight())) {
                        @Override
                        protected void onPostExecute(AsyncTaskResult<byte[]> result) {
                            super.onPostExecute(result);
                            progressBar.setVisibility(View.GONE);
                            if (result.getStatus() == AsyncTaskResult.Status.OK) {
                                imageView.setImageBitmap(getBitmap());
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }.execute();
                }
            }
        });
    }
}
