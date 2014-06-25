package cz.csas.startup.FBPoc.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cz.csas.startup.FBPoc.LoginActivity;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created with IntelliJ IDEA.
 * User: cen29414
 * Date: 19.8.13
 * Time: 12:36
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static void showErrorDialog(Context context, AsyncTaskResult taskResult) {
        if (taskResult.getStatus() == AsyncTaskResult.Status.INVALID_CREDENTIALS) {
            showMessage(context, R.string.invalid_password);
        }
        else if (taskResult.getStatus() == AsyncTaskResult.Status.LOCKED_PASSWORD) {
            showMessage(context, R.string.locked_password);
        }
        else if (taskResult.getStatus() == AsyncTaskResult.Status.NO_NETWORK) {
            showMessage(context, R.string.no_network_connection);
        }
        else if (taskResult.getStatus() != AsyncTaskResult.Status.OK && taskResult.getStatus() != AsyncTaskResult.Status.LOGIN_REQUIRED){
            showMessage(context, R.string.communicationError);
        }
    }
    public static Integer getErrorMessageForResult(AsyncTaskResult taskResult) {
        return getErrorMessageForResult(taskResult.getStatus());
    }

    public static Integer getErrorMessageForResult(AsyncTaskResult.Status taskResultStatus) {
        if (taskResultStatus == AsyncTaskResult.Status.INVALID_CREDENTIALS) {
            return R.string.invalid_password;
        }
        else if (taskResultStatus == AsyncTaskResult.Status.LOCKED_PASSWORD) {
            return R.string.locked_password;
        }
        else if (taskResultStatus == AsyncTaskResult.Status.NO_NETWORK) {
            return R.string.no_network_connection;
        }
        else if (taskResultStatus != AsyncTaskResult.Status.OK && taskResultStatus != AsyncTaskResult.Status.LOGIN_REQUIRED){
            return R.string.communicationError;
        }
        else return null;
    }


    public static void showToast(Context context, AsyncTaskResult taskResult) {
        if (taskResult.getStatus() == AsyncTaskResult.Status.INVALID_CREDENTIALS) {
            showToast(context, R.string.invalid_password);
        }
        else if (taskResult.getStatus() == AsyncTaskResult.Status.LOCKED_PASSWORD) {
            showToast(context, R.string.locked_password);
        }
        else if (taskResult.getStatus() == AsyncTaskResult.Status.NO_NETWORK) {
            showToast(context, R.string.no_network_connection);
        }
        else if (taskResult.getStatus() != AsyncTaskResult.Status.OK){
            showToast(context, R.string.communicationError);
        }
    }

    public static void showErrorDialog(Context context, AsyncTaskResult.Status errorStatus) {
        if (errorStatus == AsyncTaskResult.Status.INVALID_CREDENTIALS) {
            showMessage(context, R.string.invalid_password);
        }
        else if (errorStatus == AsyncTaskResult.Status.LOCKED_PASSWORD) {
            showMessage(context, R.string.locked_password);
        }
        else if (errorStatus == AsyncTaskResult.Status.NO_NETWORK) {
            showMessage(context, R.string.no_network_connection);
        }
        else if (errorStatus != AsyncTaskResult.Status.OK && errorStatus != AsyncTaskResult.Status.LOGIN_REQUIRED ){
            showMessage(context, R.string.communicationError);
        }
    }

    public static void showMessage(Context context, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error))
                .setMessage(context.getString(message))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
        AlertDialog errDialog = builder.create();
        errDialog.show();
    }

    public static void showMessage(Context context, int message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error))
                .setMessage(context.getString(message))
                .setPositiveButton(R.string.ok, listener);
        AlertDialog errDialog = builder.create();
        errDialog.show();
    }

    public static void showMessage(Context context, int message, int headerMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(headerMessage))
                .setMessage(context.getString(message))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
        AlertDialog errDialog = builder.create();
        errDialog.show();
    }

    public static void showMessage(Context context, int message, int headerMessage, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(headerMessage))
                .setMessage(context.getString(message))
                .setPositiveButton(R.string.ok, listener);
        AlertDialog errDialog = builder.create();
        errDialog.show();
    }

    public static void showToast(Context context, int message) {
        Toast.makeText(context, context.getResources().getString(message), Toast.LENGTH_LONG).show();
    }


    public static boolean positionIsKnown (int i) {
        return i > 0;

    }

    public static String getCurrencyDesc(Context context, String currency) {
        if ("CZK".equals(currency)) {
            return context.getString(R.string.CZK);
        }
        else return currency;
    }

    public static String getShortFacebookName(String name) {
        String[] split = name.split("\\s");
        String shortName = split[0];
        if (split.length > 1) shortName = shortName + " " + split[1].charAt(0) + ".";
        return shortName;
    }

    public static String getFirstFacebookName(String name) {
        String[] split = name.split("\\s");
        return split[0];
    }

    public static final void setAppFont(ViewGroup mContainer, Typeface mFont, boolean reflect)
    {
        if (mContainer == null || mFont == null) return;

        final int mCount = mContainer.getChildCount();

        // Loop through all of the children.
        for (int i = 0; i < mCount; ++i)
        {
            final View mChild = mContainer.getChildAt(i);
            if (mChild instanceof TextView)
            {
                // Set the font if it is a TextView.
                ((TextView) mChild).setTypeface(mFont);
            }
            else if (mChild instanceof ViewGroup)
            {
                // Recursively attempt another ViewGroup.
                setAppFont((ViewGroup) mChild, mFont, reflect);
            }
            else if (reflect)
            {
                try {
                    Method mSetTypeface = mChild.getClass().getMethod("setTypeface", Typeface.class);
                    mSetTypeface.invoke(mChild, mFont);
                } catch (Exception e) { /* Do something... */ }
            }
        }
    }

    public static void redirectToLogin(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    public static String getFormattedAmount(BigDecimal amount, String currency) {
        if (amount != null) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            nf.setCurrency(Currency.getInstance(currency));
            if (amount.compareTo(new BigDecimal(100)) < 0) {
                nf.setMaximumFractionDigits(2);
            } else {
                nf.setMaximumFractionDigits(0);
            }

            return nf.format(amount);
        }
        else return "";
    }

    public static Bitmap scaleBitmapToView(String imagePath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        if (targetW <= 0 || targetH <=0) {
            targetW = imageView.getLayoutParams().width;
            targetH = imageView.getLayoutParams().height;
        }

        if (targetH < 0 || targetW < 0) {
            targetW = imageView.getMaxWidth();
            targetH = imageView.getMaxHeight();
        }

        if (targetH < 0 && targetW < 0) {
            // don't scale image
            return BitmapFactory.decodeFile(imagePath);
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        boolean widthBigger = targetW > targetH;
        if (widthBigger && photoW < photoH) {
            int temp = photoH;
            photoH = photoW;
            photoW = temp;
        }

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }

    public static Bitmap scaleBitmapToView(byte[] imageData, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getLayoutParams().width;
        int targetH = imageView.getLayoutParams().height;
        if (targetH < 0 || targetW < 0) {
            targetW = imageView.getMaxWidth();
            targetH = imageView.getMaxHeight();
        }

        if (targetH < 0 && targetW < 0) {
            // don't scale image
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);

    }

    public static int convertToPx(Context context, int dp) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}
