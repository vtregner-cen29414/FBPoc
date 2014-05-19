package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;

import java.text.SimpleDateFormat;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class CollectionDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_detail);
        Collection collection = (Collection) getIntent().getParcelableExtra("data");

        TextView accountRow1 = (TextView) findViewById(R.id.accountRow1);
        TextView accountRow2 = (TextView) findViewById(R.id.accountRow2);
        Account account = ((Friends24Application) getApplication()).getAccount(collection.getCollectionAccount());
        accountRow1.setText(AccountsAdapter.getAccountRow1(account));
        accountRow2.setText(AccountsAdapter.getAccountRow2(account));

        TextView collectionHeader1 = (TextView) findViewById(R.id.collectionHeader1);
        TextView collectionHeader2 = (TextView) findViewById(R.id.collectionHeader2);
        collectionHeader1.setText(collection.getName());
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        collectionHeader2.setText(collection.getCollectionAccount() + " " + collection.getCurrency() + " do " + sfd.format(collection.getDueDate()));
        ImageView image = (ImageView) findViewById(R.id.collectionImage);
        if (collection.getImage() != null) {
            image.setImageDrawable(new BitmapDrawable(getResources(), collection.getImage()));
        }
        else image.setVisibility(View.GONE);

        TextView descriptionView = (TextView) findViewById(R.id.collectionDescription);
        descriptionView.setText(collection.getDescription());
        TextView linkView = (TextView) findViewById(R.id.collectionLink);
        linkView.setText(Html.fromHtml("<a href=\"" + collection.getLink() + "\">"+getString(R.string.collectionLinkInfo)+"</a>"));
        linkView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void onNotify(View view) {
    }
}
