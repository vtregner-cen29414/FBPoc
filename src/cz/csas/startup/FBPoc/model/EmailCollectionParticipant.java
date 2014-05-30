package cz.csas.startup.FBPoc.model;

import android.os.Parcel;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class EmailCollectionParticipant extends CollectionParticipant implements android.os.Parcelable {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EmailCollectionParticipant() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.email);
    }

    private EmailCollectionParticipant(Parcel in) {
        super(in);
        this.email = in.readString();
    }

    public static Creator<EmailCollectionParticipant> CREATOR = new Creator<EmailCollectionParticipant>() {
        public EmailCollectionParticipant createFromParcel(Parcel source) {
            return new EmailCollectionParticipant(source);
        }

        public EmailCollectionParticipant[] newArray(int size) {
            return new EmailCollectionParticipant[size];
        }
    };

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("email", getEmail());
            object.putOpt("amount", getAmount());
            return object;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
