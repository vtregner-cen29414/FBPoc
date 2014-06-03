package cz.csas.startup.FBPoc;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import com.facebook.android.Util;
import cz.csas.startup.FBPoc.model.*;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;
import org.apache.http.client.methods.HttpGet;

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

        TextView collectionHeader1 = (TextView) findViewById(R.id.collectionHeader1);
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

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
            new GetCollectionImageTask(this, collection.getId()).execute();
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
        if (collection.getImageLocalPath() != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra("data", collection.getImageLocalPath());
            startActivity(intent);
        }
    }

    public class GetCollectionImageTask extends AsyncTask<String, Void, byte[]> {
        public static final String URI = "collections/{id}/image";

        public GetCollectionImageTask(Context context, String collectionId) {
            super(context, URI.replace("{id}", collectionId), HttpGet.METHOD_NAME, null, false, false);
            getHttpClient().setBinaryResponse(true);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<byte[]> result) {
            super.onPostExecute(result);
            imageProgressBar.setVisibility(View.GONE);
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                byte[] bytes = result.getResult();
                collection.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                if (collection.getImage() == null) {
                    // try BASE64 - apiary mock
                    try {
                        bytes = Base64.decode(result.getResult(), Base64.NO_WRAP);
                        collection.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "cannot decode image base64 data: " + e.getMessage());
                    }
                }
                if (collection.getImage() != null) {
                    //image.setImageDrawable(new BitmapDrawable(getResources(), collection.getImage()));
                    collection.setImageLocalPath(saveImageLocal(bytes, collection.getId()));
                    image.setImageBitmap(Utils.scaleBitmapToView(bytes, image));
                }

                image.setVisibility(collection.getImage() != null ? View.VISIBLE : View.GONE);
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }
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
}
