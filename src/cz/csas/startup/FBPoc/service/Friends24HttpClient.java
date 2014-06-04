package cz.csas.startup.FBPoc.service;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import cz.csas.startup.FBPoc.Friends24Application;
import cz.csas.startup.FBPoc.LoginActivity;
import cz.csas.startup.FBPoc.R;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;

/**
 * Created with IntelliJ IDEA.
 * User: cen29414
 * Date: 22.8.13
 * Time: 7:44
 * To change this template use File | Settings | File Templates.
 */
public class Friends24HttpClient<REQ, RES> {
    public static final String TAG="Friends24";

    private String uri;
    private String httpMethod;
    private REQ jsonReq;
    private Context context;
    private JsonResponseParser<RES> responseParser;
    private boolean doAuthorization = true;
    private boolean followRedirect = false;
    private boolean binaryResponse = false;


    public Friends24HttpClient(Context context, String uri, String httpMethod, REQ jsonReq, JsonResponseParser<RES> responseParser, boolean followRedirect) {
        this.httpMethod = httpMethod;
        this.jsonReq = jsonReq;
        this.context = context;
        this.responseParser = responseParser;
        this.followRedirect = followRedirect;
        setUri(uri);
    }

    public boolean isBinaryResponse() {
        return binaryResponse;
    }

    public void setBinaryResponse(boolean binaryResponse) {
        this.binaryResponse = binaryResponse;
    }

    public void setUri(String uri) {
        String baseUrl = context.getString(R.string.friends24_server_url);
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        this.uri = (!uri.startsWith("http")) ? baseUrl+uri : uri;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return !(networkInfo == null || !networkInfo.isConnected());
    }


    public AsyncTaskResult<RES> executeRequest() {
        if (!checkNetwork()) return new AsyncTaskResult<RES>(AsyncTaskResult.Status.NO_NETWORK);

        HttpRequestBase httpReq = null;
        try {
            if (HttpGet.METHOD_NAME.equals(httpMethod)) {
                httpReq = new HttpGet(uri);
            }
            else if (HttpPost.METHOD_NAME.equals(httpMethod)) {
                httpReq = new HttpPost(uri);
                if (jsonReq != null) {
                    ByteArrayEntity entity = new ByteArrayEntity(jsonReq.toString().getBytes(HTTP.UTF_8));
                    entity.setContentType("application/json");
                    ((HttpPost)httpReq).setEntity(entity);
                }
            }
            else {
                throw new UnsupportedOperationException("Method not supported");
            }


            if (doAuthorization) {
                Friends24Application application = (Friends24Application) context.getApplicationContext();
                if (application.getAuthHeader() != null) {
                    httpReq.addHeader("Authorization", application.getAuthHeader());
                }
                else {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                    return new AsyncTaskResult<RES>(AsyncTaskResult.Status.OTHER_ERROR);
                }
            }
            HttpClient client = getNewHttpClient();
            HttpResponse response = client.execute(httpReq);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "Response status code:" + statusCode + "/" + response.getStatusLine().getReasonPhrase());

            switch (statusCode) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    AsyncTaskResult.Status status = statusCode == HttpStatus.SC_OK ? AsyncTaskResult.Status.OK : AsyncTaskResult.Status.OK_REDIRECT;
                    if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY && followRedirect) {
                        this.uri = response.getHeaders("Location")[0].getValue();
                        this.httpMethod = HttpGet.METHOD_NAME;
                        return executeRequest();
                    }

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    String responseContent = null;
                    if (!binaryResponse) {
                        responseContent = new String(outputStream.toByteArray(), HTTP.UTF_8);
                        Log.d(TAG, "Response received:");
                        Log.d(TAG, responseContent);
                    }

                    if (status == AsyncTaskResult.Status.OK_REDIRECT) {
                        return new AsyncTaskResult<RES>(status, response.getHeaders("Location")[0].getValue());
                    }
                    else if (responseParser != null) {
                        JSONObject r = new JSONObject(responseContent);
                        return new AsyncTaskResult<RES>(status, responseParser.parseResponseObject(r));
                    }
                    else if (binaryResponse) {
                        return new AsyncTaskResult<RES>(status, (RES) outputStream.toByteArray());
                    }
                    else {
                        return new AsyncTaskResult<RES>(status);
                    }
                case HttpStatus.SC_UNAUTHORIZED:
                    Header reason = response.getFirstHeader("reason");
                    if (reason != null) {
                        status = AsyncTaskResult.Status.OTHER_ERROR;
                        if ("bad_credentials".equals(reason.getValue()))
                            status = AsyncTaskResult.Status.INVALID_CREDENTIALS;
                        else if ("locked".equals(reason.getValue())) status = AsyncTaskResult.Status.LOCKED_PASSWORD;
                        return new AsyncTaskResult<RES>(status);
                    }
                    else return new AsyncTaskResult<RES>(AsyncTaskResult.Status.INVALID_CREDENTIALS);
                case HttpStatus.SC_FORBIDDEN:
                    return new AsyncTaskResult<RES>(AsyncTaskResult.Status.FORBIDDEN);
                case HttpStatus.SC_NOT_FOUND:
                    return new AsyncTaskResult<RES>(AsyncTaskResult.Status.RESOURCE_NOT_EXISTS);
                default:
                    return new AsyncTaskResult<RES>(AsyncTaskResult.Status.OTHER_ERROR);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while communicating with Friends24", e);
            return new AsyncTaskResult<RES>(AsyncTaskResult.Status.OTHER_ERROR, e);
        } catch (JSONException e) {
            Log.e(TAG, "Error while communicating with Friends24", e);
            return new AsyncTaskResult<RES>(AsyncTaskResult.Status.OTHER_ERROR, e);
        } catch (Exception t) {
            Log.e(TAG, "Error while communicating with Friends24", t);
            return new AsyncTaskResult<RES>(AsyncTaskResult.Status.OTHER_ERROR, t);
        }

    }

    public static HttpClient getNewHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new NoValidatingSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public REQ getJsonReq() {
        return jsonReq;
    }

    public void setJsonReq(REQ jsonReq) {
        this.jsonReq = jsonReq;
    }

    public boolean isDoAuthorization() {
        return doAuthorization;
    }

    public void setDoAuthorization(boolean doAuthorization) {
        this.doAuthorization = doAuthorization;
    }

    public String getUri() {
        return uri;
    }
}
