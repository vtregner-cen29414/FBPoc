package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cen29414 on 22.5.2014.
 */
public class NewCollectionActivity extends Activity {

    private static final String TAG = "Friends24";
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    public static final int SELECT_PICTURE_REQUEST_CODE = 500;

    private UiLifecycleHelper uiHelper;
    private Spinner accountSpinner;
    private Uri outputPhotoFileUri;
    private int numOfEmailParticipants = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_collection);

        accountSpinner = (Spinner) findViewById(R.id.accountSelector);
        final Friends24Application application = (Friends24Application) getApplication();
        AccountsAdapter adapter = new AccountsAdapter(this, R.layout.account_selector);
        accountSpinner.setAdapter(adapter);
        adapter.setData(application.getAccounts());
        Intent intent = getIntent();
        accountSpinner.setSelection(intent.getIntExtra("account", 0));

        TextView addPhotoView = (TextView) findViewById(R.id.addPhoto);
        TextView addLinkView = (TextView) findViewById(R.id.addLink);
        addPhotoView.setText(Html.fromHtml(getString(R.string.addPhoto)));
        addLinkView.setText(Html.fromHtml(getString(R.string.addLink)));

        TextView addFbParticipantView = (TextView) findViewById(R.id.addFbParticipantLink);
        TextView addEmailParticipantView = (TextView) findViewById(R.id.addEmailParticipantLink);
        addFbParticipantView.setText(Html.fromHtml(getString(R.string.addParticipant)));
        addEmailParticipantView.setText(Html.fromHtml(getString(R.string.addParticipant)));

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });

        uiHelper.onCreate(savedInstanceState);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (session.isClosed()) {
            Log.i(TAG, "FB session closed, redirect to login?");
        }
    }

    public void addLink(View view) {
        TextView addLinkView = (TextView) findViewById(R.id.addLink);
        EditText collectionLinkView = (EditText) findViewById(R.id.collectionLinkEdit);
        addLinkView.setVisibility(View.GONE);
        collectionLinkView.setVisibility(View.VISIBLE);
    }

    public void addPhoto(View view) {
        // Determine Uri of camera image to save.
        final File root = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final String fname = "cphoto.jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputPhotoFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputPhotoFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //final Intent galleryIntent = new Intent();
        //galleryIntent.setType("image/*");
        //galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputPhotoFileUri;
                } else {
                    selectedImageUri = data.getData();
                }

                setCollectionImage(isCamera, selectedImageUri);
            }
        }
    }

    private void setCollectionImage(boolean isCamera, Uri selectedImageUri) {
        ImageView imageView = (ImageView) findViewById(R.id.collectionImage);
        View imageWrapper = findViewById(R.id.collectionImageWrapper);
        imageWrapper.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        TextView addPhotoView = (TextView) findViewById(R.id.addPhoto);
        addPhotoView.setVisibility(View.GONE);
        // Get the dimensions of the View
        int targetW = imageView.getLayoutParams().width;
        int targetH = imageView.getLayoutParams().height;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        String imagePath = !isCamera ? getImagePath(selectedImageUri) : selectedImageUri.getPath();

        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);

        //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        //imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
    }

    private String getImagePath(Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    public void onImageDelete(View view) {
        View imageWrapper = findViewById(R.id.collectionImageWrapper);
        imageWrapper.setVisibility(View.GONE);
        TextView addPhotoView = (TextView) findViewById(R.id.addPhoto);
        addPhotoView.setVisibility(View.VISIBLE);
        outputPhotoFileUri = null;
    }

    public void onAddEmailParticipant(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup row = (ViewGroup) inflater.inflate(R.layout.email_participants_row, null);
        TextView index = (TextView) row.findViewById(R.id.pIndex);
        View deleteIcon = row.findViewById(R.id.deleteEmailParticipant);
        deleteIcon.setTag(row);
        index.setText(++numOfEmailParticipants+".");

        LinearLayout participants = (LinearLayout) findViewById(R.id.emailParticipants);
        participants.addView(row);
    }

    public void onPEmailDelete(View view) {
        Log.d(TAG, view.toString());
        ViewGroup rowToDelete  = (ViewGroup) view.getTag();
        LinearLayout participants = (LinearLayout) findViewById(R.id.emailParticipants);
        participants.removeView(rowToDelete);
        numOfEmailParticipants--;
        if (participants.getChildCount() > 0) {
            for (int i = 0; i < participants.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) participants.getChildAt(i);
                TextView index = (TextView) row.findViewById(R.id.pIndex);
                index.setText(String.valueOf(i+1)+".");
            }
        }


    }

    public void onAddFbParticipant(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup row = (ViewGroup) inflater.inflate(R.layout.fb_participants_row, null);
        View deleteIcon = row.findViewById(R.id.deleteFbParticipant);
        deleteIcon.setTag(row);

        LinearLayout participants = (LinearLayout) findViewById(R.id.fbParticipants);
        participants.addView(row);
    }
}
