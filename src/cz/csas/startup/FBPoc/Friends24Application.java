package cz.csas.startup.FBPoc;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ViewConfiguration;
import cz.csas.startup.FBPoc.utils.FontsOverride;
import org.jivesoftware.smack.SmackAndroid;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class Friends24Application extends Application {
    private static final String TAG = "Friends24";
    private static final String F_24_CTX_PREFS_NAME = "F24CTX";
    private static final String CTX_KEY = "CTX";
    public static final String TIMESTAMP_KEY = "TIMESTAMP_KEY";

    Friends24Context friends24Context;
    private SmackAndroid smackAndroid;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "app:onCreate");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Gotham-Light.otf");
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Gotham-Light.otf");
        Friends24Context ctx = loadSessionFromPreferences();
        if (ctx != null) {
            friends24Context = ctx;
            Log.d(TAG, "Friends24 session context restored from preferences!");
        }
        else friends24Context = new Friends24Context();
        smackAndroid = SmackAndroid.init(this);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            // presumably, not relevant
        }
    }

    private Friends24Context loadSessionFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(F_24_CTX_PREFS_NAME, MODE_PRIVATE);
        if (preferences.contains(CTX_KEY)) {
            Long timestampstr = preferences.getLong(TIMESTAMP_KEY, -1);
            if (timestampstr != -1) {
                Calendar timestamp = Calendar.getInstance();
                timestamp.setTime(new Date(timestampstr));
                timestamp.add(Calendar.MINUTE, 15);
                if (timestamp.getTime().after(new Date())) {
                    String ctx = preferences.getString(CTX_KEY, null);
                    return Friends24Context.fromJson(ctx);
                }
            }
        }
        return null;
    }

    public void saveSessionToPreferences() {
        SharedPreferences preferences = getSharedPreferences(F_24_CTX_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(TIMESTAMP_KEY, new Date().getTime());
        edit.putString(CTX_KEY, friends24Context.toJson());
        edit.commit();
        Log.d(TAG, "Friends24 session context saved!");
    }

    public void invalidateSessionInPreferences() {
        SharedPreferences preferences = getSharedPreferences(F_24_CTX_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.remove(CTX_KEY);
        edit.commit();
        Log.d(TAG, "Friends24 session context cleared!");
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "app:onLowMemory");
    }

    public void onApplicationExit() {
        try {
            if (smackAndroid != null) smackAndroid.onDestroy();
        } catch (Throwable e) {
            Log.w(TAG, e);
        }
    }



    public Friends24Context getFriends24Context() {
        return friends24Context;
    }
}
