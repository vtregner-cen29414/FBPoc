package cz.csas.startup.FBPoc.service;

/**
 * Created by cen29414 on 16.5.2014.
 */
public interface OnTaskCompleteListener<Result> {
    void onTaskComplete(Result result);
    void onTaskError(Throwable throwable);
}
