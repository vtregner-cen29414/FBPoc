package cz.csas.startup.FBPoc.service;

import android.os.Parcel;
import android.os.Parcelable;

public class AsyncTaskResult<T> {
    private T result;
    private Exception error;
    private Status status;
    private String location;


    public T getResult() {
        return result;
    }
    public Exception getError() {
        return error;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AsyncTaskResult(T result) {
        super();
        this.result = result;
        this.setStatus(Status.OK);
    }

    public AsyncTaskResult(Status status, T result) {
        super();
        this.result = result;
        this.setStatus(status);
    }


    public AsyncTaskResult(Status status, Exception error) {
        super();
        this.error = error;
        this.status = status;
    }

    public AsyncTaskResult(Status status, String location) {
        super();
        this.status = status;
        this.location = location;
    }

    public AsyncTaskResult(Status status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public enum Status implements Parcelable {
        OK,
        INVALID_CREDENTIALS,
        LOCKED_PASSWORD,
        NO_NETWORK,
        OTHER_ERROR,
        RESOURCE_NOT_EXISTS,
        FORBIDDEN,
        OK_REDIRECT,
        LOGIN_REQUIRED;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<Status> CREATOR = new Creator<Status>() {
            @Override
            public Status createFromParcel(final Parcel source) {
                return Status.values()[source.readInt()];
            }

            @Override
            public Status[] newArray(final int size) {
                return new Status[size];
            }
        };
    }


}