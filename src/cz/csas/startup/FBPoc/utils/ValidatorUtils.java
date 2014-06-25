package cz.csas.startup.FBPoc.utils;

import android.content.Context;
import android.widget.EditText;
import cz.csas.startup.FBPoc.R;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 25.6.2014.
 */
public class ValidatorUtils {
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.11");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("2000000");

    public  static boolean validateAmount(Context context, boolean valid, EditText amountView) {
        if (amountView.getText().length() > 0) {
            try {
                BigDecimal amount = new BigDecimal(amountView.getText().toString());
                if (amount.compareTo(MIN_AMOUNT) < 0) {
                    amountView.setError(context.getString(R.string.minAmountErr));
                    valid = false;
                }
                else if (amount.compareTo(MAX_AMOUNT) > 0) {
                    amountView.setError(context.getString(R.string.maxAmountErr));
                    valid = false;
                }
                else {
                    amountView.setError(null);
                }
            } catch (NumberFormatException e) {
                amountView.setError(context.getString(R.string.invalidAmountFormat));
                valid = false;
            }
        }

        return valid;
    }
}
