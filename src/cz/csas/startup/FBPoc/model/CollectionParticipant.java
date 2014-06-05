package cz.csas.startup.FBPoc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 19.5.2014.
 */
public abstract class CollectionParticipant implements Parcelable {
    private Long id;
    private BigDecimal amount;
    private Status status;

    public enum Status {
        PENDING(0), ACCEPTED(1), DONE(2), REFUSED(3);

        int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status valueOf(int value) {
            Status[] valueEnums = Status.values();
            for (Status valueEnum : valueEnums) {
                if (valueEnum.getValue() == value)
                {
                    return valueEnum;
                }
            }
            return null;
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id != null ? id : -1);
        dest.writeSerializable(this.amount);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
    }

    public CollectionParticipant() {
    }

    protected CollectionParticipant(Parcel in) {
        long tempId = in.readLong();
        this.id = tempId != -1 ? tempId : null;
        this.amount = (BigDecimal) in.readSerializable();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : Status.values()[tmpStatus];
    }
}
