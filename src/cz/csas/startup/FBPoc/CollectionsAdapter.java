package cz.csas.startup.FBPoc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.csas.startup.FBPoc.model.Collection;

import java.util.List;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class CollectionsAdapter extends ArrayAdapter<Collection> {
    private static final String TAG = "Friends24";

    private final int layoutResourceId;
    private LayoutInflater layoutInflater;


    public CollectionsAdapter(Context context, int resource) {
        super(context, resource);
        this.layoutResourceId = resource;
        layoutInflater = LayoutInflater.from(context);

    }


    public void setData(List<Collection> orders) {
        clear();
        if (orders != null) addAll(orders);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CollectionHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new CollectionHolder();
            holder.name = (TextView) convertView.findViewById(R.id.collectionName);
            holder.progressView = (ImageView) convertView.findViewById(R.id.collectionProgress);
            holder.rowMarker  = convertView.findViewById(R.id.rowMarkColor);
            convertView.setTag(holder);

        }
        else holder = (CollectionHolder) convertView.getTag();

        Collection collection = getItem(position);
        holder.name.setText(collection.getName());

        int progressDrawableRes;
        Collection.Status collectionProgress = collection.getCurrentCollectionProgress();
        if (collectionProgress == Collection.Status.DONE) {
            progressDrawableRes =R.drawable.paymentaccepted;
        }
        else if (collectionProgress == Collection.Status.INPROGRESS) {
            progressDrawableRes =R.drawable.paymentpending;
        }
        else {
            progressDrawableRes =R.drawable.paymentrefused;
        }

        holder.progressView.setImageDrawable(getContext().getResources().getDrawable(progressDrawableRes));

        Drawable background = (position%2 == 0) ? getContext().getResources().getDrawable(R.color.cell_odd) : getContext().getResources().getDrawable(R.color.cell_even);
        holder.rowMarker.setBackground(background);

        return convertView;
    }

    private

    static class CollectionHolder {
        TextView name;
        View rowMarker;
        ImageView progressView;
    }

}
