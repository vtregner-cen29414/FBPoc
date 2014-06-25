package cz.csas.startup.FBPoc.utils;

import android.graphics.Typeface;
import cz.csas.startup.FBPoc.Friends24Application;

/**
 * Created by cen29414 on 25.6.2014.
 */
public class GothamFont {
    public static Typeface LIGHT;
    public static Typeface MEDIUM;
    public static Typeface BOLD;

    static {
        LIGHT = Typeface.createFromAsset(Friends24Application.getApplicationBaseContext().getAssets(), "fonts/Gotham-Light.otf");
        MEDIUM = Typeface.createFromAsset(Friends24Application.getApplicationBaseContext().getAssets(), "fonts/Gotham-Medium.otf");
        BOLD = Typeface.createFromAsset(Friends24Application.getApplicationBaseContext().getAssets(), "fonts/Gotham-Bold.otf");
    }
}
