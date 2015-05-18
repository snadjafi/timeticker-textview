package com.shervin.timeticker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateUtils;
import android.util.AttributeSet;

public class TimeTickerTextView extends AppCompatTextView {

    //region Variables
    private IntentFilter mIntentFilter;
    private long mReferenceTime;
    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                updateText();
            }
        }
    };
    //endregion

    //region Constructors
    public TimeTickerTextView(Context context) {
        super(context);
        init();
    }

    public TimeTickerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeTickerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    //endregion

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(mTimeChangedReceiver, mIntentFilter);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mTimeChangedReceiver);
    }

    @Override public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.referenceTime = mReferenceTime;
        return savedState;
    }

    @Override public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState)state;
        mReferenceTime = savedState.referenceTime;
        super.onRestoreInstanceState(savedState.getSuperState());
        updateText();
    }

    public void setReferenceTime(long referenceTime) {
        mReferenceTime = referenceTime;
        updateText();
    }

    private void init() {
        // to make sure that onSavedInstanceState and #onRestoreInstanceState are called.
        if (getId() < 0) {
            setId(R.id.time_ticker_id);
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        setReferenceTime(System.currentTimeMillis());
    }

    private CharSequence getRelativeTimeSpan() {
        long now = System.currentTimeMillis();
        long difference = now - mReferenceTime;
        return (difference >= 0 &&  difference<= DateUtils.MINUTE_IN_MILLIS) ?
                getContext().getString(R.string.now) :
                DateUtils.getRelativeTimeSpanString(
                        mReferenceTime, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    private void updateText() {
        setText(getRelativeTimeSpan());
    }

    private static class SavedState extends BaseSavedState {

        //region Variable
        private long referenceTime;
        //endregion

        //region Constructors
        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            referenceTime = in.readLong();
        }
        //endregion

        @Override public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(referenceTime);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
