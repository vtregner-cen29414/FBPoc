package cz.csas.startup.FBPoc;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.utils.FontsOverride;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class Friends24Application extends Application {
    private static final String TAG = "Friends24";

    /*
    private List<GraphUser> selectedFrieds;
    private List<GraphUser> newlySelectedFrieds;
    private List<Account> accounts;
    private GraphUser fbUser;
    private boolean appLogged;
    private Map<Account, List<Payment>> payments;
    private Map<Account, List<Collection>> collections;
    private String authHeader;
    */

    public List<GraphUser> getSelectedFrieds() {
        return Friends24Context.getInstance().getSelectedFrieds();
    }

    public void setSelectedFrieds(List<GraphUser> selectedFrieds) {
        Friends24Context.getInstance().setSelectedFrieds(selectedFrieds);
    }

    public List<GraphUser> getNewlySelectedFrieds() {
        return Friends24Context.getInstance().getNewlySelectedFrieds();
    }

    public void setNewlySelectedFrieds(List<GraphUser> newlySelectedFrieds) {
        Friends24Context.getInstance().setNewlySelectedFrieds(newlySelectedFrieds);
    }

    public List<Account> getAccounts() {
        return Friends24Context.getInstance().getAccounts();
    }

    public void setAccounts(List<Account> accounts) {
        Friends24Context.getInstance().setAccounts(accounts);
    }

    public GraphUser getFbUser() {
        return Friends24Context.getInstance().getFbUser();
    }

    public void setFbUser(GraphUser fbUser) {
        Friends24Context.getInstance().setFbUser(fbUser);
    }

    public boolean isAppLogged() {
        return Friends24Context.getInstance().isAppLogged();
    }

    public void setAppLogged(boolean appLogged) {
        Friends24Context.getInstance().setAppLogged(appLogged);
    }

    public Map<Account, List<Payment>> getPayments() {
        return Friends24Context.getInstance().getPayments();
    }

    public void setPayments(Map<Account, List<Payment>> payments) {
        Friends24Context.getInstance().setPayments(payments);
    }

    public String getAuthHeader() {
        return Friends24Context.getInstance().getAuthHeader();
    }

    public void setAuthHeader(String authHeader) {
        Friends24Context.getInstance().setAuthHeader(authHeader);
    }

    public Account getAccount(Long id) {
        return Friends24Context.getInstance().getAccount(id);
    }

    public Map<Account, List<Collection>> getCollections() {
        return Friends24Context.getInstance().getCollections();
    }

    public void setCollections(Map<Account, List<Collection>> collections) {
        Friends24Context.getInstance().setCollections(collections);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "app:onCreate");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Gotham-Light.otf");
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Gotham-Light.otf");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "app:onLowMemory");
    }

    public void clearSession() {
        setFbUser(null);
        setAuthHeader(null);
        setAccounts(null);
        setSelectedFrieds(null);
    }
}
