package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by cen29414 on 20.5.2014.
 */
public class ImageDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagedetail);
        Bitmap image = getIntent().getParcelableExtra("data");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), image));
    }
}
