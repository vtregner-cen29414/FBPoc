package cz.csas.startup.FBPoc;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cz.csas.startup.FBPoc.model.*;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.service.GetCollectionImageTask;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class CollectionDetailActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";

    Collection collection;
    ProgressBar imageProgressBar;
    ImageView image;
    GestureDetector gesturedetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_detail);
        collection = getIntent().getParcelableExtra("data");


        TextView accountRow1 = (TextView) findViewById(R.id.accountRow1);
        TextView accountRow2 = (TextView) findViewById(R.id.accountRow2);
        Account account = ((Friends24Application) getApplication()).getAccount(collection.getCollectionAccount());
        accountRow1.setText(AccountsAdapter.getAccountRow1(account));
        accountRow2.setText(AccountsAdapter.getAccountRow2(account));

        final TextView collectionHeader1 = (TextView) findViewById(R.id.collectionHeader1);
        TextView collectionHeader2 = (TextView) findViewById(R.id.collectionHeader2);
        collectionHeader1.setText(collection.getName().toUpperCase());
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        collectionHeader2.setText(Utils.getFormattedAmount(collection.getTargetAmount(), collection.getCurrency()) + " do " + sfd.format(collection.getDueDate()));
        image = (ImageView) findViewById(R.id.collectionImage);
        imageProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        if (collection.getImage() != null) {
            image.setImageDrawable(new BitmapDrawable(getResources(), collection.getImage()));
            image.setVisibility(View.VISIBLE);
        }
        else {
            imageProgressBar.setVisibility(collection.isHasImage() ? View.VISIBLE : View.GONE);
            image.setVisibility(View.GONE);

        }

        TextView descriptionView = (TextView) findViewById(R.id.collectionDescription);
        descriptionView.setText(collection.getDescription());
        TextView linkView = (TextView) findViewById(R.id.collectionLink);
        linkView.setVisibility(collection.getLink() != null ? View.VISIBLE : View.GONE);
        if (collection.getLink() != null) {
            linkView.setText(Html.fromHtml("<a href=\"" + collection.getLink() + "\">" + getString(R.string.collectionLinkInfo) + "</a>"));
            linkView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(collection.getTargetAmount().intValue());
        BigDecimal currentCollectedAmount = collection.getCurrentCollectedAmount();
        progressBar.setProgress(currentCollectedAmount.intValue());

        TextView currentAmount = (TextView) findViewById(R.id.currentAmount);
        currentAmount.setText(Utils.getFormattedAmount(currentCollectedAmount,collection.getCurrency()));
        TextView targetAmount = (TextView) findViewById(R.id.targetAmount);
        targetAmount.setText("z " + Utils.getFormattedAmount(collection.getTargetAmount(),collection.getCurrency()));

        TextView numberOfParticipants = (TextView) findViewById(R.id.numberOfParticipants);
        numberOfParticipants.setText("(" + collection.getNumberOfParticipants() + ")");

        if (savedInstanceState == null) {
            appendParticipants(collection);
        }

        if (collection.isHasImage()) {
            ViewGroup imageContainer = (ViewGroup) findViewById(R.id.collectionImageContainer);
            GetCollectionImageTask task = new GetCollectionImageTask(this, collection.getId(), Utils.convertToPx(this, imageContainer.getLayoutParams().height)) {
                @Override
                protected void onPostExecute(AsyncTaskResult<byte[]> result) {
                    super.onPostExecute(result);
                    imageProgressBar.setVisibility(View.GONE);
                    if (result.getStatus() == AsyncTaskResult.Status.OK) {
                        image.setImageBitmap(getBitmap());
                        collection.setImage(getBitmap());
                    }
                    image.setVisibility(getBitmap() != null ? View.VISIBLE : View.GONE);
                }
            };
            task.execute();
        }

        View btnNotify = findViewById(R.id.btnNotify);
        TextView collectionExpiredView = (TextView) findViewById(R.id.collectionExpiredNotification);
        boolean expired = collection.getDueDate().before(new Date());
        btnNotify.setVisibility(expired ? View.GONE : View.VISIBLE);
        collectionExpiredView.setVisibility(expired ? View.VISIBLE : View.GONE);
        if (expired) {
            sfd = new SimpleDateFormat("dd.MM.");
            collectionExpiredView.setText(collectionExpiredView.getText() + " " + sfd.format(collection.getDueDate()));
        }

        FrameLayout flContainer = (FrameLayout) findViewById(R.id.flContainer);
        LinearLayout ivLayer1 = (LinearLayout)findViewById(R.id.lay1);
        LinearLayout ivLayer2 = (LinearLayout)findViewById(R.id.lay2);

        gesturedetector = new GestureDetector(this, new MyGestureListener(ivLayer1, ivLayer2));

        flContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gesturedetector.onTouchEvent(event);

                return true;

            }

        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gesturedetector.onTouchEvent(ev);

    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        appendParticipants(collection);
    }


    private void appendParticipants(Collection collection) {
        TableLayout participantsList = (TableLayout) findViewById(R.id.participantList);
        LayoutInflater inflater = LayoutInflater.from(this);
        int row=1;
        participantsList.removeAllViews();

        if (collection.getFbParticipants() != null) {
            for (FacebookCollectionParticipant participant : collection.getFbParticipants()) {
                View view = inflater.inflate(R.layout.participants_row, null);

                RoundedProfilePictureView profilePictureView = (RoundedProfilePictureView) view.findViewById(R.id.participantPic);
                profilePictureView.setProfileId(participant.getFbUserId());
                profilePictureView.setVisibility(View.VISIBLE);

                TextView participantName = (TextView) view.findViewById(R.id.participantName);
                participantName.setText(Utils.getShortFacebookName(participant.getFbUserName()));

                row = appendParticipantCommonValues(collection, row, participant, view);

                participantsList.addView(view);
            }

        }

        if (collection.getEmailParticipants() != null) {
            for (EmailCollectionParticipant participant : collection.getEmailParticipants()) {
                View view = inflater.inflate(R.layout.participants_row, null);

                RoundedProfilePictureView profilePictureView = (RoundedProfilePictureView) view.findViewById(R.id.participantPic);
                profilePictureView.setVisibility(View.GONE);

                TextView participantName = (TextView) view.findViewById(R.id.participantName);
                participantName.setText(participant.getEmail());

                row = appendParticipantCommonValues(collection, row, participant, view);

                participantsList.addView(view);
            }
        }

    }

    private int appendParticipantCommonValues(Collection collection, int row, CollectionParticipant participant, View view) {
        TextView amount = (TextView) view.findViewById(R.id.amount);
        if (participant.getAmount() != null) {
            amount.setText(Utils.getFormattedAmount(participant.getAmount(),collection.getCurrency()));
        }

        ImageView status = (ImageView) view.findViewById(R.id.participantStatus);
        if (participant.getStatus().equals(CollectionParticipant.Status.DONE)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentaccepted));
        }
        else if (participant.getStatus().equals(CollectionParticipant.Status.REFUSED)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentrefused));
        }
        else if (participant.getStatus().equals(CollectionParticipant.Status.ACCEPTED)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentpending));
        }
        else {
            status.setImageDrawable(getResources().getDrawable(R.drawable.collectionwithoutreaction));
        }

        Drawable background = (row++%2 == 0) ? getResources().getDrawable(R.color.cell_even) : getResources().getDrawable(R.color.cell_odd);
        view.findViewById(R.id.rowMarkColor).setBackground(background);
        return row;
    }

    public void onNotify(View view) {
    }

    public void onImageDetail(View view) {
        if (collection.getImage() != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.COLLECTION_ID, collection.getId());
            startActivity(intent);
        }
    }

    private String saveImageLocal(byte[] data, String collectionId) {
        final File root = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File file = new File(root, "temp_" + collectionId + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot save image to local storage", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Cannot save image to local storage", e);
            return null;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        LinearLayout ivLayer1;
        LinearLayout ivLayer2;


        private static final int SWIPE_MIN_DISTANCE = 20;

        private static final int SWIPE_MAX_OFF_PATH = 100;

        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        MyGestureListener(LinearLayout ivLayer1, LinearLayout ivLayer2) {
            this.ivLayer1 = ivLayer1;
            this.ivLayer2 = ivLayer2;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,

                               float velocityY) {

            float dX = e2.getX() - e1.getX();

            float dY = e1.getY() - e2.getY();

            if (Math.abs(dY) < SWIPE_MAX_OFF_PATH &&

                    Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY &&

                    Math.abs(dX) >= SWIPE_MIN_DISTANCE) {

                if (dX > 0) {

                    Toast.makeText(getApplicationContext(), "Right Swipe",
                            Toast.LENGTH_SHORT).show();
                    //Now Set your animation

                    if(ivLayer2.getVisibility()==View.GONE)
                    {
                        Animation fadeInAnimation = AnimationUtils.loadAnimation(CollectionDetailActivity.this, R.anim.slide_right_in);
                        ivLayer2.startAnimation(fadeInAnimation);
                        ivLayer2.setVisibility(View.VISIBLE);
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "Left Swipe",
                            Toast.LENGTH_SHORT).show();

                    if(ivLayer2.getVisibility()==View.VISIBLE)
                    {
                        Animation fadeInAnimation = AnimationUtils.loadAnimation(CollectionDetailActivity.this, R.anim.slide_left_out);
                        ivLayer2.startAnimation(fadeInAnimation);
                        ivLayer2.setVisibility(View.GONE);
                    }

                }

                return true;

            } else if (Math.abs(dX) < SWIPE_MAX_OFF_PATH &&

                    Math.abs(velocityY) >= SWIPE_THRESHOLD_VELOCITY &&

                    Math.abs(dY) >= SWIPE_MIN_DISTANCE) {

                if (dY > 0) {

                    Toast.makeText(getApplicationContext(), "Up Swipe",
                            Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(getApplicationContext(), "Down Swipe",
                            Toast.LENGTH_SHORT).show();
                }

                return true;

            }

            return false;

        }

    }
}
