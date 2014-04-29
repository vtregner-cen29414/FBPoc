package cz.csas.startup.FBPoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new AccountHolder();
            holder.accountView = (TextView) convertView.findViewById(R.id.accountNumber);
            holder.accountType = (TextView) convertView.findViewById(R.id.accountType);
            holder.accountBalance = (TextView) convertView.findViewById(R.id.accountBalance);
            convertView.setTag(holder);

        }
        else holder = (AccountHolder) convertView.getTag();

        StringBuilder a = new StringBuilder();
        Account account = getItem(position);

        if (account.getPrefix() != null) a.append(account.getPrefix()).append("-");
        a.append(account.getNumber()).append("/0800");
        holder.accountView.setText(a.toString());
        holder.accountType.setText(account.getType());
        holder.accountBalance.setText(account.getBalance().toString() + " " + account.getCurrency());

        return convertView;
    }

    static class AccountHolder {
        TextView accountView;
        TextView accountType;
        TextView accountBalance;

    }

}
