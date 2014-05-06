package cz.csas.startup.FBPoc;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 * Created by cen29414 on 6.5.2014.
 */
public abstract class Friend24AsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private String baseUrl = "http://friends24.apiary-mock.com/";

    private Context context;

    protected Friend24AsyncTask(Context context) {
        this.context = context;
    }

    protected HttpClient getHttpClient() {
        Friends24Application applicationContext = (Friends24Application) context.getApplicationContext();
        if (applicationContext.getHttpClient() == null) {
            applicationContext.setHttpClient(createNewHttpClient());
        }
        return applicationContext.getHttpClient();
    }


    public HttpClient createNewHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        return new DefaultHttpClient(ccm, params);

            /*SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            try {
                if (!context.getResources().getString(R.string.deployment).equals("PROD")) {
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);

                    SSLSocketFactory sf = new NoValidatingSSLSocketFactory(trustStore);
                    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    registry.register(new Scheme("https", sf, 443));
                }
                else {
                    registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                }

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
                return new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                return new DefaultHttpClient();
            }*/
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void showError(String message, Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String m = message != null ? message : "";
        m+=e != null ? e.getMessage() : "";
        builder.setTitle(R.string.error_dialog_title).
                setMessage("Chyba: " + m);
        builder.show();
    }

    public Context getContext() {
        return context;
    }
}
