package cz.csas.startup.FBPoc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cz.csas.startup.FBPoc.model.Payment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class ExpandablePaymentsAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "Friends24";

    private final int groupLayoutId;
    private final int childLayoutId;
    private LayoutInflater layoutInflater;
    private List<Payment> orders;
    protected Context context;


    public ExpandablePaymentsAdapter(Context context, int groupLayoutId, int childLayoutId) {
        super();
        this.context = context;
        this.groupLayoutId = groupLayoutId;
        this.childLayoutId = childLayoutId;
        layoutInflater = LayoutInflater.from(context);
        orders = new ArrayList<Payment>();
    }


    public void setData(List<Payment> orders) {
        this.orders = orders;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getGroupCount() {
        return orders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return orders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return orders.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        PaymentHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(groupLayoutId, parent, false);
            holder = new PaymentHolder();
            holder.recipientNameView = (TextView) convertView.findViewById(R.id.recipientName);
            holder.amountView = (TextView) convertView.findViewById(R.id.paymentAmount);
            holder.statusView = (ImageView) convertView.findViewById(R.id.paymentStatus);
            holder.paymentDate = (TextView) convertView.findViewById(R.id.paymentDate);
            holder.rowMarker  = convertView.findViewById(R.id.rowMarkColor);
            convertView.setTag(holder);

        }
        else holder = (PaymentHolder) convertView.getTag();

        Payment payment = (Payment) getGroup(groupPosition);
        holder.recipientNameView.setText(!isExpanded ? payment.getShortRecipentName() : payment.getRecipientName());

        holder.amountView.setText(payment.getAmount() + " " + payment.getCurrency());
        holder.amountView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.");
        if (payment.getPaymentDate() != null) holder.paymentDate.setText(sfd.format(payment.getPaymentDate()));

        fillStatus(holder, payment);

        holder.statusView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        setRowMarker(groupPosition, holder);



        return convertView;
    }

    private void fillStatus(PaymentHolder holder, Payment payment) {
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
    }

    private void setRowMarker(int groupPosition, PaymentHolder holder) {
        Drawable background = (groupPosition%2 == 0) ? getContext().getResources().getDrawable(R.color.cell_odd) : getContext().getResources().getDrawable(R.color.cell_even);
        holder.rowMarker.setBackground(background);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PaymentHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(childLayoutId, parent, false);
            holder = new PaymentHolder();
            holder.note = (TextView) convertView.findViewById(R.id.messageForRecipient);
            holder.rowMarker  = convertView.findViewById(R.id.rowMarkColor);
            holder.amountView = (TextView) convertView.findViewById(R.id.paymentAmount);
            holder.statusView = (ImageView) convertView.findViewById(R.id.paymentStatus);
            convertView.setTag(holder);

        }
        else holder = (PaymentHolder) convertView.getTag();

        Payment payment = (Payment) getGroup(groupPosition);
        holder.note.setText(payment.getNote() != null ? payment.getNote() : "");
        holder.amountView.setText(payment.getAmount() + " " + payment.getCurrency());
        fillStatus(holder, payment);

        setRowMarker(groupPosition, holder);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class PaymentHolder {
        TextView recipientNameView;
        TextView amountView;
        TextView paymentDate;
        TextView note;
        ImageView statusView;
        View rowMarker;
    }

}
