package cz.csas.startup.FBPoc;

import android.app.Application;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import org.apache.http.client.HttpClient;

import java.util.List;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class Friends24Application extends Application {
    private List<GraphUser> selectedFrieds;
    private List<Account> accounts;
    private HttpClient httpClient;
    private GraphUser fbUser;
    private boolean appLogged;

    public List<GraphUser> getSelectedFrieds() {
        return selectedFrieds;
    }

    public void setSelectedFrieds(List<GraphUser> selectedFrieds) {
        this.selectedFrieds = selectedFrieds;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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
}
