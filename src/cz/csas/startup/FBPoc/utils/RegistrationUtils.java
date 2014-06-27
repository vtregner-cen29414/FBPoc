package cz.csas.startup.FBPoc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: cen29414
 * Date: 29.8.13
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationUtils {
    public static final String TAG="Friends24";

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getGcmRegistrationId(Context context, String user) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Constants.GCM_REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "GCM Registration not found.");
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed, GCM reg id must be updated.");
            return null;
        }

        String registeredUser = prefs.getString(Constants.USERNAME, null);
        if (registeredUser == null || !registeredUser.equals(user)) {
            Log.i(TAG, "User has changed, GCM req id must be updated");
            return null;
        }
        return registrationId;
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Constants.FRIENDS24_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
