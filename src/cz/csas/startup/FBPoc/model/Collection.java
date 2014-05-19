package cz.csas.startup.FBPoc.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class Collection implements Parcelable {
    private String id;
    private Long collectionAccount;
    private String name;
    private BigDecimal targetAmount;
    private String currency;
    private String description;
    private Bitmap image;
    private String link;
    private Date dueDate;
    private Date created;
    private List<FacebookCollectionParticipant> fbParticipants;
    private List<EmailCollectionParticipant> emailParticipants;

    public Long getCollectionAccount() {
        return collectionAccount;
    }

    public void setCollectionAccount(Long collectionAccount) {
        this.collectionAccount = collectionAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<FacebookCollectionParticipant> getFbParticipants() {
        return fbParticipants;
    }

    public void setFbParticipants(List<FacebookCollectionParticipant> fbParticipants) {
        this.fbParticipants = fbParticipants;
    }

    public List<EmailCollectionParticipant> getEmailParticipants() {
        return emailParticipants;
    }

    public void setEmailParticipants(List<EmailCollectionParticipant> emailParticipants) {
        this.emailParticipants = emailParticipants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.collectionAccount);
        dest.writeString(this.name);
        dest.writeSerializable(this.targetAmount);
        dest.writeString(this.currency);
        dest.writeString(this.description);
        dest.writeParcelable(this.image, 0);
        dest.writeString(this.link);
        dest.writeLong(dueDate != null ? dueDate.getTime() : -1);
        dest.writeLong(created != null ? created.getTime() : -1);
        dest.writeTypedList(fbParticipants);
        dest.writeTypedList(emailParticipants);
    }

    public Collection() {
    }

    private Collection(Parcel in) {
        this.id = in.readString();
        this.collectionAccount = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.targetAmount = (BigDecimal) in.readSerializable();
        this.currency = in.readString();
        this.description = in.readString();
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.link = in.readString();
        long tmpDueDate = in.readLong();
        this.dueDate = tmpDueDate == -1 ? null : new Date(tmpDueDate);
        long tmpCreated = in.readLong();
        this.created = tmpCreated == -1 ? null : new Date(tmpCreated);
        fbParticipants = new ArrayList<FacebookCollectionParticipant>();
        emailParticipants = new ArrayList<EmailCollectionParticipant>();
        in.readTypedList(fbParticipants, FacebookCollectionParticipant.CREATOR);
        in.readTypedList(emailParticipants, EmailCollectionParticipant.CREATOR);
    }

    public static Parcelable.Creator<Collection> CREATOR = new Parcelable.Creator<Collection>() {
        public Collection createFromParcel(Parcel source) {
            return new Collection(source);
        }

        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };
}
