package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;

public class DuoquScrollView extends ScrollView implements IViewAttr {

    public TypedArray mDuoquAttr = null;

    public DuoquScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDuoquAttr = context.obtainStyledAttributes(attrs,
                R.styleable.duoqu_attr);

    }

    @Override
    public Object obtainStyledAttributes(byte styleType, int styleId) {
        return ViewManger.obtainStyledAttributes(mDuoquAttr, styleType, styleId);
    }

}
