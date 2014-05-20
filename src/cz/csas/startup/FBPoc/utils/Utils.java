package cz.csas.startup.FBPoc.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;

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


}
