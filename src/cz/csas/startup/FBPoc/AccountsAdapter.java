package cz.csas.startup.FBPoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.csas.startup.FBPoc.model.Account;

import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class AccountsAdapter extends ArrayAdapter<Account> {

    private final int layoutResourceId;
    private LayoutInflater layoutInflater;


    public AccountsAdapter(Context context, int resource) {
        super(context, resource);
        this.layoutResourceId = resource;
        layoutInflater = LayoutInflater.from(context);

    }


    public void setData(List<Account> orders) {
        clear();
        if (orders != null) addAll(orders);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = getView(position, convertView, parent);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        // set height if dropdown item to 96dp
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(96*scale+0.5f)));
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new AccountHolder();
            holder.accountRow1 = (TextView) convertView.findViewById(R.id.accountRow1);
            holder.accountRow2 = (TextView) convertView.findViewById(R.id.accountRow2);
            convertView.setTag(holder);

        }
        else holder = (AccountHolder) convertView.getTag();

        Account account = getItem(position);

        holder.accountRow2.setText(getAccountRow2(account));
        holder.accountRow1.setText(getAccountRow1(account));

        return convertView;
    }

    public static String getAccountRow2(Account account) {
        StringBuilder a = new StringBuilder();
        if (account.getPrefix() != null) a.append(account.getPrefix()).append("-");
        a.append(account.getNumber()).append("/0800");
        return a.toString();
    }

    public static String getAccountRow1(Account account) {
        return account.getType() + " - " + account.getBalance() + account.getCurrency();
    }

    static class AccountHolder {
        TextView accountRow1;
        TextView accountRow2;

    }

}
