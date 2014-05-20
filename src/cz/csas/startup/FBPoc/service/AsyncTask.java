package cz.csas.startup.FBPoc.service;

import android.content.Context;
import cz.csas.startup.FBPoc.Friends24Application;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for mrev async task
 */
public class AsyncTask<Params, Progress, Result> extends android.os.AsyncTask<Params, Process, AsyncTaskResult<Result>> implements JsonResponseParser<Result> {

    private Friends24HttpClient<Params,Result> friends24HttpClient;
    private Context context;

    public AsyncTask(Context context, String uri, String httpMethod, Params jsonRequest, boolean jsonResponse, boolean followRedirect) {
        super();
        this.context = context;
        this.friends24HttpClient = new Friends24HttpClient<Params, Result>(context, uri, httpMethod, jsonRequest, jsonResponse? this : null, followRedirect);
    }

    @Override
    protected AsyncTaskResult<Result> doInBackground(Params... params) {
        if (params != null && params.length > 0) friends24HttpClient.setJsonReq(params[0]);
        return friends24HttpClient.executeRequest();
    }

    /**
     * Subclasses should implement this method to parse response
     * @param object
     * @return
     * @throws org.json.JSONException
     */
    @Override
    public Result parseResponseObject(JSONObject object) throws JSONException {
        return null;
    }

    public Friends24HttpClient<Params, Result> getHttpClient() {
        return friends24HttpClient;
    }

    public Context getContext() {
        return context;
    }

    public Friends24Application getApplication() {
        return (Friends24Application) getContext().getApplicationContext();
    }
}
