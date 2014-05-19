package cz.csas.startup.FBPoc.model;

import android.os.Parcel;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class FacebookCollectionParticipant extends CollectionParticipant implements android.os.Parcelable {
    private String fbUserId;
    private String fbUserName;

    public String getFbUserId() {
        return fbUserId;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getFbUserName() {
        return fbUserName;
    }

    public void setFbUserName(String fbUserName) {
        this.fbUserName = fbUserName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.fbUserId);
        dest.writeString(this.fbUserName);
    }

    public FacebookCollectionParticipant() {
    }

    private FacebookCollectionParticipant(Parcel in) {
        super(in);
        this.fbUserId = in.readString();
        this.fbUserName = in.readString();
    }

    public static Creator<FacebookCollectionParticipant> CREATOR = new Creator<FacebookCollectionParticipant>() {
        public FacebookCollectionParticipant createFromParcel(Parcel source) {
            return new FacebookCollectionParticipant(source);
        }

        public FacebookCollectionParticipant[] newArray(int size) {
            return new FacebookCollectionParticipant[size];
        }
    };
}
