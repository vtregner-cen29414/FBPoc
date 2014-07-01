package cz.csas.startup.FBPoc;

import com.facebook.model.GraphUser;
import com.google.gson.GsonBuilder;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.utils.GraphUserGSonSerializer;

import java.util.List;
import java.util.Map;

/**
 * Created by cen29414 on 10.6.2014.
 */
public class Friends24Context {
    private List<GraphUser> selectedFrieds;
    private List<GraphUser> newlySelectedFrieds;
    private List<Account> accounts;
    private GraphUser fbUser;
    private boolean appLogged;
    private transient Map<Account, List<Payment>> payments;
    private transient Map<Account, List<Collection>> collections;
    private String authHeader;
    private String loggedUser;

    public String getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(String loggedUser) {
        this.loggedUser = loggedUser;
    }

    public List<GraphUser> getSelectedFriends() {
        return selectedFrieds;
    }

    public void setSelectedFrieds(List<GraphUser> selectedFrieds) {
        this.selectedFrieds = selectedFrieds;
    }

    public List<GraphUser> getNewlySelectedFrieds() {
        return newlySelectedFrieds;
    }

    public void setNewlySelectedFrieds(List<GraphUser> newlySelectedFrieds) {
        this.newlySelectedFrieds = newlySelectedFrieds;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public GraphUser getFbUser() {
        return fbUser;
    }

    public void setFbUser(GraphUser fbUser) {
        this.fbUser = fbUser;
    }

    public boolean isAppLogged() {
        return appLogged;
    }

    public void setAppLogged(boolean appLogged) {
        this.appLogged = appLogged;
    }

    public Map<Account, List<Payment>> getPayments() {
        return payments;
    }

    public void setPayments(Map<Account, List<Payment>> payments) {
        this.payments = payments;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public Account getAccount(Long id) {
        if (accounts != null) {
            for (Account account : accounts) {
                if (account.getId().equals(id)) return account;
            }
        }
        return null;
    }

    public Map<Account, List<Collection>> getCollections() {
        return collections;
    }

    public void setCollections(Map<Account, List<Collection>> collections) {
        this.collections = collections;
    }

    public Friends24Context() {      }

    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GraphUser.class, new GraphUserGSonSerializer());
        return builder.create().toJson(this);
    }

    public static Friends24Context fromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GraphUser.class, new GraphUserGSonSerializer());
        return builder.create().fromJson(json, Friends24Context.class);
    }

    public void clearSession() {
        setFbUser(null);
        setAuthHeader(null);
        setAccounts(null);
        setSelectedFrieds(null);
        setAppLogged(false);
    }
}
