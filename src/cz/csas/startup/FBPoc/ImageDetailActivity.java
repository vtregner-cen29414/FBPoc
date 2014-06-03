package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import cz.csas.startup.FBPoc.utils.Utils;

/**
 * Created by cen29414 on 20.5.2014.
 */
public class ImageDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagedetail);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (imageView.getDrawable() == null) {
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), loadImage(getIntent().getStringExtra("data"), imageView)));
                }
            }
        });
    }

    private Bitmap loadImage(String imagePath, ImageView imageView) {
        return Utils.scaleBitmapToView(imagePath, imageView);
    }
}
