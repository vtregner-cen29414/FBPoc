package cz.csas.startup.FBPoc.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import cz.csas.startup.FBPoc.R;

/**
 * Created by cen29414 on 23.6.2014.
 */
public class AdapterTableLayout extends TableLayout {

    /**
     * The adapter containing the data to be displayed by this view
     */
    ListAdapter mAdapter;

    /**
     * Should be used by subclasses to listen to changes in the dataset
     */
    AdapterDataSetObserver mDataSetObserver;

    OnItemClickListener mListener;

    View headerView;

    public AdapterTableLayout(Context context) {
        super(context);
    }

    public AdapterTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addHeaderView(View v) {
        headerView = v;
    }

    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * Gets the data associated with the specified position in the list.
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public Object getItemAtPosition(int position) {
        ListAdapter adapter = mAdapter;
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

    protected void fillWithAdapterData() {
        if (mAdapter != null) {
            if (mAdapter.getCount() > 0) {
                removeAllViews();
                if (headerView != null) {
                    this.addView(headerView);
                }
                addDivider();
                for (int i=0; i< mAdapter.getCount(); i++) {
                    View view = mAdapter.getView(i, null, this);
                    view.setTag(view.getId(), i);
                    view.setBackground(getContext().getResources().getDrawable(R.drawable.selected_row_item));
                    this.addView(view);
                    if (mListener != null) {
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mListener.onItemClick(v, (Integer) v.getTag(v.getId()));
                            }
                        });
                    }


                    // na 4.1 mi nefunguje divider atribut u LinearLayout proto takto programove
                    if (i < mAdapter.getCount()) {
                        addDivider();
                    }
                }
            }
            else {
                removeAllViews();
            }

        }
        else {
            removeAllViews();
        }
    }

    private void addDivider() {
        View divider = new View(getContext());
        divider.setBackground(new ColorDrawable(R.color.dividerColor));
        divider.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        this.addView(divider);
    }


    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            fillWithAdapterData();

        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            fillWithAdapterData();
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
