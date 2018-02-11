package cn.com.xy.sms.sdk.ui.popu.simplepart;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBottomTwo;
import cn.com.xy.sms.sdk.ui.popu.util.SimpleButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;

public class SimpleBubbleBottom extends RelativeLayout {

    public static final String TAG = "SimpleBubbleBottom";
    public Activity mContext;
    public boolean mDisLogo = true; // Whether show the logo
    public View mDuoquBottomSplitLine; // Separator
    public View mDuoquBtnSplitLine;
    public View mBtn1 = null, mBtn2 = null; // button1 button2
    public TextView mTextView1 = null, mTextView2 = null;
    public JSONArray mJsonArray;
    public HashMap<String, Object> mExtend;
    public int mSize = 0; // button numbers
    private static final int FIRST_TEXTVIEW_PADDING_BUTTOM = (int) ViewUtil
            .getDimension(R.dimen.duoqu_first_textview_padding_bottom);
    private static final int SECOND_TEXTVIEW_PADDING_BUTTOM = (int) ViewUtil
            .getDimension(R.dimen.duoqu_second_textview_padding_bottom);
    private static final String BOTTOM_SPLIT_LINE_BG = "#EAEAEA";

    public SimpleBubbleBottom(Activity mContext, JSONArray jsonArray, HashMap<String, Object> extend) throws Exception {

        super(mContext);
        this.mExtend = extend;
        this.mContext = mContext;

        inflate(mContext, R.layout.duoqu_simple_bubble_bottom_two, this);

        //LogManager.i(TAG, "SimpleBubbleBottom jsonArray=" + jsonArray);
        initViews();
        setContent(jsonArray,extend);
    }

    private void initViews(){
        mDuoquBottomSplitLine = findViewById(R.id.duoqu_bottom_split_line);
        mDuoquBtnSplitLine = findViewById(R.id.duoqu_btn_split_line);
        mBtn1 = findViewById(R.id.duoqu_btn_1);
        mBtn2 = findViewById(R.id.duoqu_btn_2);
        mTextView1 = (TextView) findViewById(R.id.duoqu_btn_text_1);
        mTextView2 = (TextView) findViewById(R.id.duoqu_btn_text_2);

    }
    public void setContent(JSONArray jsonArray,HashMap<String, Object> extend) {

        try {
            this.mExtend =  extend;
            this.mJsonArray = jsonArray;
           
            if (jsonArray != null && jsonArray.length() > 0) {
                mSize = jsonArray.length();
            }
            //LogManager.i(TAG, "setContent=" + jsonArray);
            if (mSize == 0) {
                setVisibility(View.GONE);
                return;
            }

            setVisibility(View.VISIBLE);
            if (mSize == 1) {
                mDuoquBtnSplitLine.setVisibility(View.GONE);
                mBtn1.setVisibility(View.VISIBLE);
                mTextView1.setVisibility(View.VISIBLE);
                mBtn2.setVisibility(View.GONE);
                mTextView2.setVisibility(View.GONE);
                SimpleButtonUtil.setBotton(mContext, mTextView1, mTextView1, jsonArray.getJSONObject(0), mDisLogo,
                        mExtend);

            } else if (mSize >= 2) {
                mDuoquBtnSplitLine.setVisibility(View.INVISIBLE);
                mBtn1.setVisibility(View.VISIBLE);
                mTextView1.setVisibility(View.VISIBLE);
                
                JSONObject actionObj1 = jsonArray.getJSONObject(0);
                JSONObject actionObj2 = jsonArray.getJSONObject(1); 
                mBtn2.setVisibility(View.VISIBLE);
                mTextView2.setVisibility(View.VISIBLE);
                SimpleButtonUtil.setBotton(mContext, mTextView1, mTextView1, actionObj1, mDisLogo, mExtend);
                SimpleButtonUtil.setBotton(mContext, mTextView2, mTextView2, actionObj2, mDisLogo, mExtend);
            }

            mDuoquBottomSplitLine.setVisibility(View.VISIBLE);
            ViewManger.setViewBg(Constant.getContext(), mDuoquBottomSplitLine, BOTTOM_SPLIT_LINE_BG,
                    R.drawable.duoqu_line, 2, true);

            // Button icon and text position adjustment
            mTextView1.setPadding(0, 0, 0, FIRST_TEXTVIEW_PADDING_BUTTOM);
            mTextView2.setPadding(0, 0, 0, SECOND_TEXTVIEW_PADDING_BUTTOM);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    public void setButtonClickAble(boolean isClickAble){
        if(null==mBtn1) {
            return;
        }
        mBtn1.setClickable(isClickAble);
        if(null!=mBtn2){
            mBtn2.setClickable(isClickAble);
        }
        
        if(mJsonArray == null){
            return;
        }
        try{
            int len = mJsonArray.length();
            if(len >= 1){
                if(mTextView1 != null){
                    mBtn1.setVisibility(View.VISIBLE);
                    SimpleButtonUtil.setBottonValue(mContext,mTextView1,
                            mJsonArray.getJSONObject(0), mDisLogo,isClickAble);
                }
            } 
            if(len >= 2){
                if(mTextView2 != null){
                    mBtn2.setVisibility(View.VISIBLE);
                    SimpleButtonUtil.setBottonValue(mContext,mTextView2,
                            mJsonArray.getJSONObject(1), mDisLogo,isClickAble);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
