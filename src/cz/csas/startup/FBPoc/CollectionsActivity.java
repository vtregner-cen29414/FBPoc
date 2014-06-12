package cz.csas.startup.FBPoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import cz.csas.startup.FBPoc.model.*;
import cz.csas.startup.FBPoc.service.AsyncTask;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.SwipeAccountSelector;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class CollectionsActivity extends FbAwareListActivity {
    private static final String TAG = "Friends24";

    private CollectionsAdapter collectionsAdapter;
    private SwipeAccountSelector accountSelector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_list);

        //Spinner accountSpinner = (Spinner) findViewById(R.id.accountSelector);
        final Friends24Application application = (Friends24Application) getApplication();
        /*AccountsAdapter adapter = new AccountsAdapter(this, R.layout.account_selector);
        accountSpinner.setAdapter(adapter);
        adapter.setData(application.getAccounts());

        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Account account = (Account) parent.getItemAtPosition(position);
                if (account != null) {
                    if (application.getCollections().get(account) == null) {
                        new GetCollectionsTask(CollectionsActivity.this, account, collectionsAdapter).execute();
                    } else {
                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.GONE);
                        ListView listView = (ListView) findViewById(android.R.id.list);
                        listView.setVisibility(View.VISIBLE);
                        collectionsAdapter.setData(application.getCollections().get(account));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        accountSelector = (SwipeAccountSelector) findViewById(R.id.accountSelector);
        accountSelector.setAccounts(R.layout.account_selector, application.getFriends24Context().getAccounts());
        accountSelector.setOnItemSelectedListener(new SwipeAccountSelector.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Account account, View view, int position) {
                if (account != null) {
                    if (application.getFriends24Context().getCollections().get(account) == null) {
                        new GetCollectionsTask(CollectionsActivity.this, account, collectionsAdapter).execute();
                    } else {
                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.GONE);
                        ListView listView = (ListView) findViewById(android.R.id.list);
                        listView.setVisibility(View.VISIBLE);
                        collectionsAdapter.setData(application.getFriends24Context().getCollections().get(account));
                        collectionsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        collectionsAdapter = new CollectionsAdapter(this, R.layout.collection_row);
        getListView().addHeaderView(getLayoutInflater().inflate(R.layout.collection_list_header, null), null, false);
        setListAdapter(collectionsAdapter);
        if (application.getFriends24Context().getCollections() == null) {
            application.getFriends24Context().setCollections(new HashMap<Account, List<Collection>>(application.getFriends24Context().getAccounts().size()));
            for (Account account : application.getFriends24Context().getAccounts()) {
                application.getFriends24Context().getCollections().put(account, null);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Account account = (Account) accountSelector.getSelectedItem();
        if (account != null && getFriendsApplication().getFriends24Context().getCollections() != null && getFriendsApplication().getFriends24Context().getCollections().get(account) == null) {
            new GetCollectionsTask(this, account, collectionsAdapter).execute();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, CollectionDetailActivity.class);
        intent.putExtra("data", (Parcelable) getListView().getItemAtPosition(position));
        startActivity(intent);
    }

    public void onNewCollection(View view) {
        final Friends24Application application = (Friends24Application) getApplication();
        application.getFriends24Context().setSelectedFrieds(null);
        application.saveSessionToPreferences();
        Intent intent = new Intent(this, NewCollectionActivity.class);
        intent.putExtra("account", accountSelector.getSelectedItemPosition());
        startActivity(intent);
    }

    private static class GetCollectionsTask extends AsyncTask<Void, Void, List<Collection>> {
        public static final String URI = "collections/";

        Account account;
        ProgressBar progressBar;
        CollectionsAdapter collectionsAdapter;
        ListView listView;

        private GetCollectionsTask(Context context, Account account, CollectionsAdapter collectionsAdapter) {
            super(context, URI+account.getId(), HttpGet.METHOD_NAME, null, true, true);
            this.account = account;
            this.collectionsAdapter = collectionsAdapter;
        }

        @Override
        public List<Collection> parseResponseObject(JSONObject object) throws JSONException {
            JSONArray jcollections = object.getJSONArray("collections");
            List<Collection> collections = new ArrayList<Collection>();
            for (int i=0; i< jcollections.length(); i++) {
                JSONObject jcollection = jcollections.getJSONObject(i);
                Collection collection = new Collection();
                collection.setId(jcollection.getString("id"));
                collection.setCreated(new Date(jcollection.getLong("created")));
                collection.setDueDate(new Date(jcollection.getLong("dueDate")));
                collection.setCollectionAccount(jcollection.getLong("collectionAccount"));
                if (!jcollection.isNull("targetAmount")) collection.setTargetAmount(new BigDecimal(jcollection.getString("targetAmount")));
                collection.setCurrency(jcollection.getString("currency"));
                collection.setName(jcollection.getString("name"));
                if (!jcollection.isNull("description")) collection.setDescription(jcollection.getString("description"));

                if (!jcollection.isNull("image")) {
                    byte[] bytes = Base64.decode(jcollection.getString("image"), Base64.NO_WRAP);
                    collection.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }

                collection.setHasImage(jcollection.getBoolean("hasImage"));

                if (!jcollection.isNull("link")) collection.setLink(jcollection.getString("link"));

                if (!jcollection.isNull("collectionFBParticipants")) {
                    List<FacebookCollectionParticipant> participants = new ArrayList<FacebookCollectionParticipant>();
                    collection.setFbParticipants(participants);
                    JSONArray jparticipants = jcollection.getJSONArray("collectionFBParticipants");
                    for (int j=0; j< jparticipants.length(); j++) {
                        JSONObject jparticipant = jparticipants.getJSONObject(j);
                        FacebookCollectionParticipant participant = new FacebookCollectionParticipant();
                        participant.setId(jparticipant.getLong("id"));
                        participant.setFbUserId(jparticipant.getString("fbUserId"));
                        participant.setFbUserName(jparticipant.getString("fbUserName"));
                        if (!jparticipant.isNull("amount")) participant.setAmount(new BigDecimal(jparticipant.getString("amount")));
                        participant.setStatus(CollectionParticipant.Status.valueOf(jparticipant.getInt("status")));
                        participants.add(participant);
                    }
                }

                if (!jcollection.isNull("collectionEmailParticipants")) {
                    List<EmailCollectionParticipant> participants = new ArrayList<EmailCollectionParticipant>();
                    collection.setEmailParticipants(participants);
                    JSONArray jparticipants = jcollection.getJSONArray("collectionEmailParticipants");
                    for (int j=0; j< jparticipants.length(); j++) {
                        JSONObject jparticipant = jparticipants.getJSONObject(j);
                        EmailCollectionParticipant participant = new EmailCollectionParticipant();
                        participant.setId(jparticipant.getLong("id"));
                        participant.setEmail(jparticipant.getString("email"));
                        if (!jparticipant.isNull("amount")) participant.setAmount(new BigDecimal(jparticipant.getString("amount")));
                        participant.setStatus(CollectionParticipant.Status.valueOf(jparticipant.getInt("status")));
                        participants.add(participant);
                    }
                }

                collections.add(collection);
            }
            return collections;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) ((Activity)getContext()).findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            listView = (ListView) ((Activity)getContext()).findViewById(android.R.id.list);
            listView.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Collection>> result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (result.getStatus().equals(AsyncTaskResult.Status.OK)) {
                getApplication().getFriends24Context().getCollections().put(account, result.getResult());
                collectionsAdapter.setData(result.getResult());
                collectionsAdapter.notifyDataSetChanged();
            }
            else {
                Utils.showErrorDialog(getContext(), result);
            }
        }
    }
}
