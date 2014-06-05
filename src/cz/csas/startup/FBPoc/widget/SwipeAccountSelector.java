package cz.csas.startup.FBPoc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cz.csas.startup.FBPoc.R;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.utils.Utils;

import java.util.List;

/**
 * Created by cen29414 on 5.6.2014.
 */
public class SwipeAccountSelector extends RelativeLayout {

    private int layoutResourceId;
    private List<Account> accounts;
    private int currentPosition;
    private LayoutInflater layoutInflater;
    private GestureListener gestureListener;
    private OnItemSelectedListener onItemSelectedListener;
    private GestureDetector gesturedetector;


    public SwipeAccountSelector(Context context) {
        super(context);
    }

    public SwipeAccountSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeAccountSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAccounts(int layoutResourceId, List<Account> accounts) {
        this.layoutResourceId = layoutResourceId;
        this.accounts = accounts;
        layoutInflater = LayoutInflater.from(getContext());
        for (int i = 0; i<accounts.size(); i++) {
            View view = getView(i, null, this);
            view.setVisibility(GONE);
            this.addView(view);
        }
        this.getChildAt(0).setVisibility(VISIBLE);
        this.gestureListener = new GestureListener();
        this.gesturedetector = new GestureDetector(getContext(), gestureListener);
    }

    public Account getSelectedItem() {
        return accounts.get(currentPosition);
    }

    public GestureListener getGestureListener() {
        return gestureListener;
    }

    protected Account getItem(int possition) {
        return accounts.get(possition);
    }

    public static String getAccountRow2(Account account) {
        StringBuilder a = new StringBuilder();
        if (account.getPrefix() != null) a.append(account.getPrefix()).append("-");
        a.append(account.getNumber()).append("/0800");
        return a.toString();
    }

    public static String getAccountRow1(Account account) {
        return account.getType() + " - " + Utils.getFormattedAmount(account.getBalance(), account.getCurrency());
    }


    protected View getView(int position, View convertView, ViewGroup parent) {
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

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been selected.
     *
     * @param listener The callback that will run
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public int getSelectedItemPosition() {
        return currentPosition;
    }


    static class AccountHolder {
        TextView accountRow1;
        TextView accountRow2;

    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 20;
        private static final int SWIPE_MAX_OFF_PATH = 100;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        private Animation.AnimationListener endAnimationLister;

        GestureListener() {
            endAnimationLister = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(getItem(currentPosition), getChildAt(currentPosition), currentPosition);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            };

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,

                               float velocityY) {

            float dX = e2.getX() - e1.getX();

            float dY = e1.getY() - e2.getY();

            if (Math.abs(dY) < SWIPE_MAX_OFF_PATH &&

                    Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY &&

                    Math.abs(dX) >= SWIPE_MIN_DISTANCE) {

                if (dX > 0) {
                    swipeRight();

                } else {
                    swipeLeft();
                }

                return true;

            }

            return false;

        }

        private void swipeLeft() {
            if (currentPosition < accounts.size()-1) {
                View currentView = getChildAt(currentPosition);
                Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_out);
                currentView.startAnimation(fadeInAnimation);
                currentView.setVisibility(View.GONE);

                View nextView = getChildAt(++currentPosition);
                fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_in);
                nextView.startAnimation(fadeInAnimation);
                nextView.setVisibility(View.VISIBLE);
                fadeInAnimation.setAnimationListener(endAnimationLister);
            }
        }

        private void swipeRight() {
            //Now Set your animation
            if (currentPosition > 0) {
                View currentView = getChildAt(currentPosition);
                Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_right_out);
                currentView.startAnimation(fadeInAnimation);
                currentView.setVisibility(View.GONE);

                View nextView = getChildAt(--currentPosition);
                fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_right_in);
                nextView.startAnimation(fadeInAnimation);
                nextView.setVisibility(View.VISIBLE);
                fadeInAnimation.setAnimationListener(endAnimationLister);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            boolean toLeft = e.getX() < getWidth()/2;
            if (toLeft) swipeRight();
            else swipeLeft();
            return true;
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Account account, View view, int position);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        boolean consumed = gesturedetector.onTouchEvent(ev);
        if (!consumed && ev.getAction() ==  MotionEvent.ACTION_DOWN) return true;
        else return consumed;
    }
}
