package cz.csas.startup.FBPoc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.EmailCollectionParticipant;
import cz.csas.startup.FBPoc.model.FacebookCollectionParticipant;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.text.MessageFormat;

/**
 * Created by cen29414 on 16.5.2014.
 */
public class CollectionConfirmationActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_confirmation);
        TextView newPayment = (TextView) findViewById(R.id.btnNewCollection);
        newPayment.setText(Html.fromHtml(getString(R.string.new_collection_link)));

        Intent intent = getIntent();
        Collection collection = (Collection) intent.getParcelableExtra("data");

        TextView accountRow1 = (TextView) findViewById(R.id.accountRow1);
        TextView accountRow2 = (TextView) findViewById(R.id.accountRow2);
        Friends24Application application = (Friends24Application) getApplication();
        Account account = application.getAccount(collection.getCollectionAccount());
        accountRow1.setText(AccountsAdapter.getAccountRow1(account));
        accountRow2.setText(AccountsAdapter.getAccountRow2(account));

        TextView count = (TextView) findViewById(R.id.participantCount);
        count.setText(MessageFormat.format(getString(R.string.collection_confirm_1), collection.getNumberOfParticipants()));

        GridLayout fbParticipantsView = (GridLayout) findViewById(R.id.fbParticipants);
        if (collection.getFbParticipants() != null) {
            for (int i=0; i<collection.getFbParticipants().size(); i++) {
                FacebookCollectionParticipant participant = collection.getFbParticipants().get(i);
                ViewGroup fbParticipantView = (ViewGroup) getLayoutInflater().inflate(R.layout.fb_participant, null);
                RoundedProfilePictureView pic = (RoundedProfilePictureView) fbParticipantView.findViewById(R.id.participantPic);
                pic.setProfileId(participant.getFbUserId());
                TextView name = (TextView) fbParticipantView.findViewById(R.id.participantName);
                name.setText(Utils.getFirstFacebookName(participant.getFbUserName()));
                fbParticipantsView.addView(fbParticipantView);
            }
        }

        ViewGroup emailParticipantsView = (ViewGroup) findViewById(R.id.emailParticipants);
        if (collection.getEmailParticipants() != null) {
            for (int i=0; i<collection.getEmailParticipants().size(); i++) {
                EmailCollectionParticipant participant = collection.getEmailParticipants().get(i);
                TextView child = new TextView(this);
                child.setGravity(Gravity.CENTER);
                child.setText(participant.getEmail());
                child.setCompoundDrawablesWithIntrinsicBounds(R.drawable.im_emailico, 0, 0, 0);
                child.setCompoundDrawablePadding(5);
                emailParticipantsView.addView(child);
            }
        }

    }

    public void onNewCollection(View view) {
        Intent intent = new Intent(this, NewCollectionActivity.class);
        startActivity(intent);
        finish();
    }
}
