package cz.csas.startup.FBPoc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.csas.startup.FBPoc.model.Payment;

import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class PaymentsAdapter extends ArrayAdapter<Payment> {
    private static final String TAG = "Friends24";

    private final int layoutResourceId;
    private LayoutInflater layoutInflater;


    public PaymentsAdapter(Context context, int resource) {
        super(context, resource);
        this.layoutResourceId = resource;
        layoutInflater = LayoutInflater.from(context);

    }


    public void setData(List<Payment> orders) {
        clear();
        if (orders != null) addAll(orders);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PaymentHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new PaymentHolder();
            holder.recipientNameView = (TextView) convertView.findViewById(R.id.recipientName);
            holder.amountView = (TextView) convertView.findViewById(R.id.paymentAmount);
            holder.statusView = (ImageView) convertView.findViewById(R.id.paymentStatus);
            convertView.setTag(holder);

        }
        else holder = (PaymentHolder) convertView.getTag();

        StringBuilder a = new StringBuilder();
        Payment payment = getItem(position);
        holder.recipientNameView.setText(payment.getRecipientName());
        holder.amountView.setText(payment.getAmount() + " " + payment.getCurrency());
        if (payment.getStatus() == Payment.Status.PENDING) {
            holder.statusView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.paymentpending));
        }
        else if (payment.getStatus() == Payment.Status.ACCEPTED) {
            holder.statusView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.paymentaccepted));
        }
        else if (payment.getStatus() == Payment.Status.REFUSED) {
            holder.statusView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.paymentrefused));
        }
        else {
            Log.e(TAG, "Unexpected payment type: " + payment.getStatus());
            holder.statusView.setImageDrawable(null);
        }


        return convertView;
    }

    static class PaymentHolder {
        TextView recipientNameView;
        TextView amountView;
        ImageView statusView;
    }

}
