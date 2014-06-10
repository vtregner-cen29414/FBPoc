package cz.csas.startup.FBPoc;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.Payment;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cen29414 on 10.6.2014.
 */
public class Friends24Context implements Parcelable {
    private static final String TAG = "Friends24";
    private static Friends24Context instance;

    public synchronized static Friends24Context getInstance() {
        if (instance == null) {
            Log.d(TAG, "Creating new Friends24Context");
            instance = new Friends24Context();
        }
        return instance;
    }
    private List<GraphUser> selectedFrieds;
    private List<GraphUser> newlySelectedFrieds;
    private List<Account> accounts;
    private GraphUser fbUser;
    private boolean appLogged;
    private Map<Account, List<Payment>> payments;
    private Map<Account, List<Collection>> collections;
    private String authHeader;

    public List<GraphUser> getSelectedFrieds() {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(this.selectedFrieds, dest);
        writeToParcel(this.newlySelectedFrieds, dest);
        dest.writeList(this.accounts);
        dest.writeString(this.fbUser.getInnerJSONObject().toString());
        dest.writeByte(appLogged ? (byte) 1 : (byte) 0);
        dest.writeMap(this.payments);
        dest.writeMap(this.collections);
        dest.writeString(this.authHeader);
    }

    private void writeToParcel(List<GraphUser> graphUsers, Parcel dest) {
        List<String> tmpList = new ArrayList<String>();
        for (GraphUser tmp : graphUsers) {
            tmpList.add(tmp.getInnerJSONObject().toString());
        }
        dest.writeList(tmpList);
    }

    private List<GraphUser> readGraphUsersFromParcel(Parcel in) {
        List<String> tmpList = new ArrayList<String>();
        in.readStringList(tmpList);
        List<GraphUser> list = new ArrayList<GraphUser>();
        for (String tmpString : tmpList) {
            GraphUser user = null;
            try {
                user = (GraphUser) GraphObject.Factory.create(new JSONObject(tmpString),GraphUser.class);
            } catch (JSONException e) {
                throw new RuntimeException("Cannot read graphUsers");
            }
            list.add(user);
        }
        return list;
    }

    private GraphUser readGraphUserFromParcel(Parcel in) {
        String json = in.readString();
        try {
            GraphUser user = (GraphUser) GraphObject.Factory.create(new JSONObject(json), GraphUser.class);
            return user;
        } catch (JSONException e) {
            throw new RuntimeException("Cannot read graphUsers");
        }
    }



    public Friends24Context() {
    }

    private Friends24Context(Parcel in) {
        this.selectedFrieds = readGraphUsersFromParcel(in);
        this.newlySelectedFrieds = readGraphUsersFromParcel(in);
        this.accounts = new ArrayList<Account>();
        in.readList(this.accounts, ArrayList.class.getClassLoader());
        this.fbUser = readGraphUserFromParcel(in);
        this.appLogged = in.readByte() != 0;
        this.payments = new HashMap<Account, List<Payment>>();
        in.readMap(this.payments, HashMap.class.getClassLoader());
        this.collections = new HashMap<Account, List<Collection>>();
        in.readMap(this.collections, HashMap.class.getClassLoader());
        this.authHeader = in.readString();
    }

    public static Parcelable.Creator<Friends24Context> CREATOR = new Parcelable.Creator<Friends24Context>() {
        public Friends24Context createFromParcel(Parcel source) {
            return new Friends24Context(source);
        }

        public Friends24Context[] newArray(int size) {
            return new Friends24Context[size];
        }
    };
}
