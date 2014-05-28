package cz.csas.startup.FBPoc;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.CollectionParticipant;
import cz.csas.startup.FBPoc.model.EmailCollectionParticipant;
import cz.csas.startup.FBPoc.model.FacebookCollectionParticipant;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by cen29414 on 22.5.2014.
 */
public class NewCollectionActivity extends FbAwareActivity {

    private static final String TAG = "Friends24";
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    public static final int SELECT_PICTURE_REQUEST_CODE = 500;
    public static final String COLLECTION = "COLLECTION";

    private Spinner accountSpinner;
    private Uri outputPhotoFileUri;
    private int numOfEmailParticipants = 0;

    private Collection collection;

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

        if (savedInstanceState != null) {
            collection = savedInstanceState.getParcelable(COLLECTION);
        }
        else collection = new Collection();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(0);
        refreshCurrentProgress();

        EditText targetAmount = (EditText) findViewById(R.id.amount);
        targetAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) collection.setTargetAmount(new BigDecimal(s.toString()));
                else collection.setTargetAmount(null);

                refreshCurrentProgress();
            }
        });

        ensureOpenFacebookSession();

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
            else if (requestCode == PICK_FRIENDS_ACTIVITY) {
                appendNewFbRow(getFriendsApplication().getNewlySelectedFrieds());
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(COLLECTION, collection);
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
        EditText amountView = (EditText) row.findViewById(R.id.amount);
        addTextChangeLister(amountView);
        if (collection.getEmailParticipants() == null) collection.setEmailParticipants(new ArrayList<EmailCollectionParticipant>());
        EmailCollectionParticipant participant = new EmailCollectionParticipant();
        collection.getEmailParticipants().add(participant);
        amountView.setTag(participant);
        deleteIcon.setTag(row);
        index.setText(++numOfEmailParticipants+".");
        LinearLayout participants = (LinearLayout) findViewById(R.id.emailParticipants);
        participants.addView(row);
        amountView.requestFocus();
    }

    public void onPEmailDelete(View view) {
        ViewGroup rowToDelete  = (ViewGroup) view.getTag();
        LinearLayout participants = (LinearLayout) findViewById(R.id.emailParticipants);
        EditText amountView = (EditText) rowToDelete.findViewById(R.id.amount);

        collection.getEmailParticipants().remove((EmailCollectionParticipant)amountView.getTag());
        participants.removeView(rowToDelete);

        numOfEmailParticipants--;
        if (participants.getChildCount() > 0) {
            for (int i = 0; i < participants.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) participants.getChildAt(i);
                TextView index = (TextView) row.findViewById(R.id.pIndex);
                index.setText(String.valueOf(i+1)+".");
            }
        }
        refreshCurrentProgress();
    }

    public void onAddFbParticipant(View view) {
        Intent intent = new Intent();
        intent.setData(PickerActivity.FRIEND_PICKER);
        intent.setClass(this, PickerActivity.class);
        intent.putExtra(PickerActivity.MULTI_SELECTION, true);
        intent.putExtra(PickerActivity.TITLE, getString(R.string.pickParticipant));
        startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
    }

    private void appendNewFbRow(List<GraphUser> participants) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout participantsView = (LinearLayout) findViewById(R.id.fbParticipants);
        for (GraphUser participant : participants) {
            ViewGroup row = (ViewGroup) inflater.inflate(R.layout.fb_participants_row, null);
            View deleteIcon = row.findViewById(R.id.deleteFbParticipant);
            deleteIcon.setTag(row);
            RoundedProfilePictureView pic = (RoundedProfilePictureView) row.findViewById(R.id.participantPic);
            pic.setProfileId(participant.getId());
            TextView name = (TextView) row.findViewById(R.id.participantName);
            name.setText(participant.getFirstName());
            row.setTag(participant);

            EditText amountView = (EditText) row.findViewById(R.id.amount);
            addTextChangeLister(amountView);
            if (collection.getFbParticipants() == null) collection.setFbParticipants(new ArrayList<FacebookCollectionParticipant>());
            FacebookCollectionParticipant fbParticipant = new FacebookCollectionParticipant();
            collection.getFbParticipants().add(fbParticipant);
            amountView.setTag(fbParticipant);

            participantsView.addView(row);
            amountView.requestFocus();
        }

    }

    public void onPFBDelete(View view) {
        ViewGroup rowToDelete  = (ViewGroup) view.getTag();
        LinearLayout participants = (LinearLayout) findViewById(R.id.fbParticipants);
        View amountView = rowToDelete.findViewById(R.id.amount);
        collection.getFbParticipants().remove((FacebookCollectionParticipant)amountView.getTag());
        participants.removeView(rowToDelete);
        getFriendsApplication().getSelectedFrieds().remove((GraphUser)rowToDelete.getTag());
        refreshCurrentProgress();
    }

    protected void addTextChangeLister(final EditText amountView) {
        amountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CollectionParticipant participant = (CollectionParticipant) amountView.getTag();
                if (s.toString() != null && s.toString().length() > 0) {
                    participant.setAmount(new BigDecimal(s.toString()));
                }
                else participant.setAmount(null);

                refreshCurrentProgress();
            }
        });
    }

    private void refreshCurrentProgress() {
        BigDecimal sum = BigDecimal.ZERO;
        if (collection.getEmailParticipants() != null) {
            for (CollectionParticipant collectionParticipant : collection.getEmailParticipants()) {
                if (collectionParticipant.getAmount() != null) sum = sum.add(collectionParticipant.getAmount());
            }
        }
        if (collection.getFbParticipants() != null) {
            for (CollectionParticipant collectionParticipant : collection.getFbParticipants()) {
                if (collectionParticipant.getAmount() != null) sum = sum.add(collectionParticipant.getAmount());
            }
        }
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(collection.getTargetAmount() != null ? collection.getTargetAmount().intValue() : 0);
        progressBar.setProgress(sum.intValue());
        TextView current = (TextView) findViewById(R.id.splittedAmountLabelView);
        String targetAmount = collection.getTargetAmount() != null ? collection.getTargetAmount().toString() : "";
        current.setText(sum.toString() + " " + getString(R.string.from) + " " + targetAmount);
    }

    private boolean validateFields() {
        boolean valid = true;
        EditText collectionNameView = (EditText) findViewById(R.id.collectionName);
        EditText amountView = (EditText) findViewById(R.id.amount);
        EditText collectionDescriptionView = (EditText) findViewById(R.id.collectionDescription);
        EditText collectionLinkView = (EditText) findViewById(R.id.collectionLinkEdit);
        EditText collectionDueDateView = (EditText) findViewById(R.id.collectionDueDate);

        if (collectionNameView.length() < 1) {
            collectionNameView.setError("Povinné pole");
            valid = false;
        }
        else {
            collectionNameView.setError(null);
            collection.setName(collectionNameView.getText().toString());
        }

        collection.setTargetAmount(amountView.length() > 0 ? new BigDecimal(amountView.getText().toString()) : null);
        collection.setDescription(collectionDescriptionView.length() > 0 ? collectionDescriptionView.getText().toString() : null);

        if (collectionLinkView.length() > 0) {
            try {
                String s = collectionLinkView.getText().toString();
                if (!s.toLowerCase().startsWith("http://")) s = "http://" + s;
                new URL(s);
                collectionLinkView.setError(null);
            } catch (MalformedURLException e) {
                collectionLinkView.setError("Není platné URL");
                valid = false;
            }
        }
        else {
            collectionLinkView.setError(null);
            collection.setLink(null);
        }

        if (collectionDueDateView.length() < 1) {
            collectionDueDateView.setError("Povinné pole");
            valid = false;
        }
        else {
            collectionDueDateView.setError(null);
            SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
            sfd.setLenient(false);
            try {
                collection.setDueDate(sfd.parse(collectionDueDateView.getText().toString()));
                collectionDueDateView.setError(null);
            } catch (ParseException e) {
                collectionDueDateView.setError("Zadejte platné datum");
                valid = false;
            }
        }

        ViewGroup fbParticipants = (ViewGroup) findViewById(R.id.fbParticipants);
        if (fbParticipants.getChildCount() > 0) {
            for (int i=0; i<fbParticipants.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) fbParticipants.getChildAt(i);
                EditText participantAmountView = (EditText) row.findViewById(R.id.amount);
                FacebookCollectionParticipant participant = (FacebookCollectionParticipant) participantAmountView.getTag();
                GraphUser fbGraphUser = (GraphUser) row.getTag();
                participant.setFbUserId(fbGraphUser.getId());
                participant.setFbUserName(fbGraphUser.getName());
            }
        }

        ViewGroup emailParticipants = (ViewGroup) findViewById(R.id.emailParticipants);
        if (emailParticipants.getChildCount() > 0) {
            for (int i=0; i<emailParticipants.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) emailParticipants.getChildAt(i);
                EditText participantAmountView = (EditText) row.findViewById(R.id.amount);
                EmailCollectionParticipant participant = (EmailCollectionParticipant) participantAmountView.getTag();
                TextView emailView = (TextView) row.findViewById(R.id.pEmail);
                if (emailView.length() > 0) {
                    String email = emailView.getText().toString();
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        participant.setEmail(email);
                        emailView.setError(null);
                    }
                    else {
                        emailView.setError("Zadejte validní email adresu");
                        valid = false;
                    }

                }
                else {
                    emailView.setError("Povinné pole");
                    valid = false;
                }
            }
        }

        return valid;
    }


    public void onCalendarPick(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void onCreteCollection(View view) {
        validateFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_collection_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_split_whole_amount:
                splitWholeAmount();
                return true;
            case R.id.action_split_remaining_amount:
                splitRemainingAmount();
                return true;
            case R.id.action_sum_amount:
                sumTargetAmount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void splitWholeAmount() {
        if (collection.getTargetAmount() != null) {
            int numOfParticipants = 0;
            ViewGroup fbView = (ViewGroup) findViewById(R.id.fbParticipants);
            numOfParticipants+=fbView.getChildCount();
            ViewGroup emailView = (ViewGroup) findViewById(R.id.emailParticipants);
            numOfParticipants+=emailView.getChildCount();
            if (numOfParticipants > 0) {
                BigDecimal amount = collection.getTargetAmount().divide(new BigDecimal(numOfParticipants), 0, BigDecimal.ROUND_HALF_DOWN);
                setAmountToParticipants(false, numOfParticipants, amount);
                refreshCurrentProgress();
            }
        }
        else {
            Utils.showMessage(this, R.string.targetAmmountNotSet, R.string.warning);
        }

    }

    private void setAmountToParticipants(boolean onlyEmpty, int numOfParticipants, BigDecimal amount) {
        ViewGroup last = null;
        ViewGroup fbView = (ViewGroup) findViewById(R.id.fbParticipants);
        ViewGroup emailView = (ViewGroup) findViewById(R.id.emailParticipants);
        BigDecimal currentSum = BigDecimal.ZERO;
        if (fbView.getChildCount() > 0) {
            for (int i=0;i<fbView.getChildCount();i++) {
                ViewGroup row = (ViewGroup) fbView.getChildAt(i);
                if (setAmountToParticipant(onlyEmpty,amount, row)) last = row;
                EditText amountView = (EditText) row.findViewById(R.id.amount);
                if (amountView.length() > 0) currentSum = currentSum.add(((CollectionParticipant) amountView.getTag()).getAmount());
            }
        }
        if (emailView.getChildCount() > 0) {
            for (int i=0;i<emailView.getChildCount();i++) {
                ViewGroup row = (ViewGroup) emailView.getChildAt(i);
                if (setAmountToParticipant(onlyEmpty,amount, row)) last = row;
                EditText amountView = (EditText) row.findViewById(R.id.amount);
                if (amountView.length() > 0) currentSum = currentSum.add(((CollectionParticipant) amountView.getTag()).getAmount());
            }
        }
        if (last != null) {
            BigDecimal remaining = collection.getTargetAmount().subtract(currentSum);
            setAmountToParticipant(false, amount.add(remaining), last);
        }
    }


    private boolean setAmountToParticipant(boolean onlyEmpty, BigDecimal amount, ViewGroup row) {
        EditText amountView = (EditText) row.findViewById(R.id.amount);
        boolean added;
        if (!onlyEmpty || amountView.length() == 0) {
            amountView.setText(amount.toString());
            ((CollectionParticipant) amountView.getTag()).setAmount(amount);
            added = true;
        }
        else added = false;
        return added;
    }

    private void splitRemainingAmount() {
        if (collection.getTargetAmount() != null) {
            int numOfParticipants = 0;
            ViewGroup fbView = (ViewGroup) findViewById(R.id.fbParticipants);
            numOfParticipants+=fbView.getChildCount();
            ViewGroup emailView = (ViewGroup) findViewById(R.id.emailParticipants);
            numOfParticipants+=emailView.getChildCount();
            BigDecimal currentSum = BigDecimal.ZERO;
            int emptyParticipants = 0;
            if (numOfParticipants > 0) {
                if (fbView.getChildCount() > 0) {
                    for (int i = 0; i < fbView.getChildCount(); i++) {
                        ViewGroup row = (ViewGroup) fbView.getChildAt(i);
                        EditText amountView = (EditText) row.findViewById(R.id.amount);
                        BigDecimal amount = ((CollectionParticipant) amountView.getTag()).getAmount();
                        if (amount != null) currentSum = currentSum.add(amount);
                        else emptyParticipants++;
                    }
                }
                if (emailView.getChildCount() > 0) {
                    for (int i = 0; i < emailView.getChildCount(); i++) {
                        ViewGroup row = (ViewGroup) emailView.getChildAt(i);
                        EditText amountView = (EditText) row.findViewById(R.id.amount);
                        BigDecimal amount = ((CollectionParticipant) amountView.getTag()).getAmount();
                        if (amount != null) currentSum = currentSum.add(amount);
                        else emptyParticipants++;
                    }
                }

                BigDecimal remaining = collection.getTargetAmount().subtract(currentSum);
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    if (emptyParticipants > 0) {
                        BigDecimal amount = remaining.divide(new BigDecimal(emptyParticipants), 0, BigDecimal.ROUND_HALF_DOWN);
                        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                            setAmountToParticipants(true, numOfParticipants, amount);
                        }
                    } else {
                        Utils.showMessage(this, R.string.noEmptyParticipants, R.string.warning);
                    }
                }
            }
        }
    }

    private void sumTargetAmount() {
        ViewGroup fbView = (ViewGroup) findViewById(R.id.fbParticipants);
        ViewGroup emailView = (ViewGroup) findViewById(R.id.emailParticipants);
        BigDecimal currentSum = BigDecimal.ZERO;
        if (fbView.getChildCount() > 0) {
            for (int i = 0; i < fbView.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) fbView.getChildAt(i);
                EditText amountView = (EditText) row.findViewById(R.id.amount);
                BigDecimal amount = ((CollectionParticipant) amountView.getTag()).getAmount();
                if (amount != null) currentSum = currentSum.add(amount);
            }
        }
        if (emailView.getChildCount() > 0) {
            for (int i = 0; i < emailView.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) emailView.getChildAt(i);
                EditText amountView = (EditText) row.findViewById(R.id.amount);
                BigDecimal amount = ((CollectionParticipant) amountView.getTag()).getAmount();
                if (amount != null) currentSum = currentSum.add(amount);
            }
        }

        if (currentSum.compareTo(BigDecimal.ZERO) > 0) {
            EditText targetAmount = (EditText) findViewById(R.id.amount);
            targetAmount.setText(currentSum.toString());
            collection.setTargetAmount(currentSum);
            refreshCurrentProgress();
        }

    }



    public class DatePickerFragment extends DialogFragment  implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(NewCollectionActivity.this, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            EditText collectionDueDateView = (EditText) findViewById(R.id.collectionDueDate);
            collectionDueDateView.setText(day+"."+(month+1)+"."+year);
        }
    }

}
