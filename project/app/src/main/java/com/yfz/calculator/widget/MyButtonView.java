package com.yfz.calculator.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import java.util.HashMap;
/**
 * 作者：游丰泽 Franco
 * 简介：仿IOS解锁屏幕密码的view
 * Github: https://github.com/FENGZEYOU123/Android_IosUnlockPasswordView
 * CSDN:https://blog.csdn.net/ruiruiddd/article/details/117356567
 */
public class MyButtonView extends FrameLayout{
        //两个功能，返回 和 删除
        private static final String FUNCTION_BACK = "返回",FUNCTION_DELETE = "删除";
        //画笔
        private Paint mPaint;
        //view-背景颜色
        private final int mView_backgroundColor = Color.WHITE;
        //按钮-二维数组
        private int[][] mNumberArray = new int[][]{{1,2,3},{4,5,6},{7,8,9},{10,0,11}};
        //按钮-将int和按钮数据以哈希map的形式储存对应起来
        private HashMap<Integer,ButtonData> mButtonMap = new HashMap<>();
        //按钮-颜色-未选中
        private int mButton_color_normal = Color.LTGRAY;
        //按钮-颜色-选中
        private int mButton_Color_selected = Color.DKGRAY;
        //按钮-文字-颜色-未选中
        private int mButton_Color_text_normal = Color.WHITE;
        //按钮-文字-颜色
        private int mButton_Color_text_selected = Color.LTGRAY;
        //按钮-文字-大小
        private int mButton_Size_text = 40;
        //按钮-长宽
        private int mButton_width_height = 0;
        //按钮-之间的间距
        private int mButton_margin = 80;
        //按钮-布局-大小
        private int mButton_layout_height_Width = 0;
        //按钮-布局-上方margin,使其处于view中间
        private int mButton_layout_margin_top = 0;
        //密码-记录输入后的密码
        private String mPassword_input = "";
        //密码-位置rect
        private Rect mPassword_rect = new Rect();
        //密码-文字大小
        private int mPassword_text_size = 10;
        //密码-文字颜色
        private int mPassword_text_color = Color.BLACK;
        //密码-正确密码设置,默认123456
        private int mPassword_correct = 123456;
        //按压震动
        private final int SHAKE_TIME = 50;
        //接口事件
        private OnPasswordListener mOnPasswordListener;
        public MyButtonView(Context context) {
            super(context);
            initial(context);
        }
        public MyButtonView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initial(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mButton_margin = getMeasuredWidth() <= getMeasuredHeight() ? getMeasuredWidth() /20 : getMeasuredHeight() /20;
            mButton_Size_text = getMeasuredWidth() <= getMeasuredHeight() ? getMeasuredWidth() /10 : getMeasuredHeight() /10;
            mPassword_text_size = getMeasuredWidth() <= getMeasuredHeight() ? getMeasuredWidth() /20 : getMeasuredHeight() /20;
            //设置长宽占满全屏
        setMeasuredDimension(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        private void initial(Context context){
            setWillNotDraw(false);
            mPaint=new Paint();
            mPaint.setAntiAlias(true);
            setBackgroundColor(mView_backgroundColor);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    doSelectedInt(event.getX(),event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    doCalculatePasswordInput();
                    doClearAll();
                    break;
            }
            doRefreshUI();
            return true;
        }

        /**
         * 计算展示的输入密码
         */
        private void doCalculatePasswordInput(){
            if(null != getButtonSelected()) {
                if(!getButtonSelected().isFunction){
                    if(mPassword_input.length() < String.valueOf(mPassword_correct).length()) {
                        mPassword_input = mPassword_input + getButtonSelected().getNumber();
                    }
                    if(mPassword_input.length() >= String.valueOf(mPassword_correct).length()) {
                        boolean isCorrect = mPassword_input.equals(String.valueOf(mPassword_correct));
                        if(null != mOnPasswordListener){
                            mOnPasswordListener.onListenerPasswordResult(mPassword_input,isCorrect);
                        }
                    }
                }else {
                    switch (getButtonSelected().getNumber()){
                        case FUNCTION_BACK:
                            if(null != mOnPasswordListener){
                                mOnPasswordListener.onListenerBack(true);
                            }
                            break;
                        case FUNCTION_DELETE:
                            if(mPassword_input.length()>=1) {
                                mPassword_input = mPassword_input.substring(0, mPassword_input.length() - 1);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        /**
         * 计算按压时，选中的数字
         */
        private void doSelectedInt(float x, float y){
            if(null != mButtonMap){
                for(int i=0;i<=mButtonMap.size();i++){
                    if(null != mButtonMap.get(i)) {
                        if (mButtonMap.get(i).getRectF().contains(x, y)) {
                            mButtonMap.get(i).setSelected(true);
                        }else {
                            mButtonMap.get(i).setSelected(false);
                        }
                    }
                }
            }
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            onDraw_initialValues(canvas); //初始化一些数据
            onDraw_doDrawButtons(canvas); //画按钮布局
            onDraw_doDrawPassword(canvas); //画展示输入过的密码
        }

        /**
         * 初始化一些数据
         * @param canvas
         */
        private void onDraw_initialValues(Canvas canvas){
            //canvas可绘制的高度
            int mCanvasHeight = canvas.getHeight(); //画板高
            //canvas可绘制的宽
            int mCanvasWidth = canvas.getWidth();   //画板宽
            mButton_layout_height_Width = mCanvasWidth <= mCanvasHeight ? mCanvasWidth : mCanvasHeight;  //按钮布局的大小
            //每个按钮的长宽 = (布局总长宽 - 4个按钮间距 ) / 3个按钮数量
            mButton_width_height = (mButton_layout_height_Width - (mNumberArray[0].length*2) * mButton_margin ) / (mNumberArray[0].length) ;
            //计算按钮布局上方margin，使其处于view中间
            mButton_layout_margin_top = (mCanvasHeight - mButton_layout_height_Width)/2;
        }

        /**
         * 画按钮基本轮廓
         * @param canvas
         */
        private void onDraw_doDrawButtons(Canvas canvas){
            for(int i = 0; i< mNumberArray.length; i++){
                for(int j = 0; j< mNumberArray[i].length; j++){
                    if(!mButtonMap.containsKey(mNumberArray[i][j])){
                        RectF rectF = new RectF();
                        rectF.left = mButton_margin * (j+2) + mButton_width_height * j;
                        rectF.right = rectF.left + mButton_width_height ;
                        rectF.top = mButton_layout_margin_top+mButton_margin * (i+2) + mButton_width_height * i;
                        rectF.bottom = rectF.top + mButton_width_height ;
                        mButtonMap.put(mNumberArray[i][j], new ButtonData(
                                String.valueOf(mNumberArray[i][j]),
                                rectF,
                                mButton_color_normal,
                                mButton_Color_selected,
                                mButton_Color_text_normal,
                                mButton_Color_text_selected,
                                mButton_Size_text,
                                false));
                    }
                    ButtonData buttonData = getButton(mNumberArray[i][j]);
                    if(null != buttonData) {
                        mPaint.setStyle(Paint.Style.FILL);
                        mPaint.setColor(buttonData.getColorBackground());
                        canvas.drawArc(buttonData.getRectF(), 0f, 360f, true, mPaint);
                        onDraw_doDrawButtonText(canvas,buttonData); //画按钮文字
                    }
                }
            }
        }

        /**
         * 画按钮中的文字
         */
        private void onDraw_doDrawButtonText(Canvas canvas,ButtonData buttonData){
            mPaint.setTextSize(buttonData.getTextSize());
            mPaint.setFakeBoldText(true);
            mPaint.getTextBounds(
                    buttonData.getNumber(),
                    0,
                    buttonData.getNumber().length(),
                    buttonData.getRectText());
            mPaint.setColor(buttonData.getTextColor());
            canvas.drawText(
                    buttonData.getNumber(),
                    buttonData.getNumberStartX(),
                    buttonData.getNumberStartY(),
                    mPaint);
        }
        /**
         * 画输入过的密码
         */
        private void onDraw_doDrawPassword(Canvas canvas){
            mPaint.setTextSize(mPassword_text_size);
            mPaint.setFakeBoldText(true);
            mPaint.setColor(mPassword_text_color);
            mPaint.getTextBounds(
                    mPassword_input,
                    0,
                    mPassword_input.length(),
                    mPassword_rect);
            canvas.drawText(
                    mPassword_input,
                    (canvas.getWidth())/2 - (mPassword_rect.left+mPassword_rect.right)/2,
                    mButton_layout_margin_top/2 - (mPassword_rect.top+mPassword_rect.bottom)/2,
                    mPaint);
        }
        /**
         * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
         */
        private int px2dp(Context context, float pxValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }
        /**
         * 将px值转换为sp值，保证文字大小不变
         * @param pxValue
         * @return
         */
        public int px2sp(Context context, float pxValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (pxValue / fontScale + 0.5f);
        }
        /**
         * 刷新UI
         */
        private void doRefreshUI(){
            invalidate();
        }
        /**
         * 清除记录-当手指离开所选按钮或从屏幕抬起
         */
        private void doClearAll(){
            if(null != mButtonMap) {
                for (int i = 0; i <= mButtonMap.size(); i++) {
                    if(null != mButtonMap.get(i)) {
                        mButtonMap.get(i).setSelected(false);
                    }
                }
            }
        }
        /**
         * 获取当前选中的按钮数据
         */
        private ButtonData getButtonSelected(){
            if(null != mButtonMap) {
                for (int i = 0; i<=mButtonMap.size(); i++) {
                    if(null != mButtonMap.get(i)) {
                        if (mButtonMap.get(i).isSelected()) {
                            return mButtonMap.get(i);
                        }
                    }
                }
            }
            return null;
        }
        /**
         * 根据key获取按钮数据
         */
        private ButtonData getButton(int number){
            if(null != mButtonMap) {
                return mButtonMap.get(number);
            }
            return null;
        }

        //得到正确密码
        public int getPassword_correct() {
            return mPassword_correct;
        }
        //设置正确密码
        public void setPassword_correct(int Password_correct) {
            this.mPassword_correct = Password_correct;
        }

        /**
         * 按钮表单信息-记录rectF位置-当前数字
         */
        private class ButtonData {
            private String number = "";   //按钮数字
            private String character = "";   //按钮所代表的英文字母
            private RectF rectF = null;  //按钮的位置 rectF
            private int colorNormal = -1;
            private int colorSelected = -1;
            private int colorTextNormal = -1;
            private int colorTextSelected = -1;
            private boolean isSelected = false;
            private Rect rectText = null; //文字矩形位置
            private int textSize = 20; //文字大小
            private boolean isFunction = false;
            public ButtonData(String number, RectF rectF, int colorNormal, int colorSelected,int colorTextNormal,int colorTextSelected, int textSize,boolean isSelected){
                this.number = number;
                this.rectF = rectF;
                this.colorNormal = colorNormal;
                this.colorSelected = colorSelected;
                this.isSelected = isSelected;
                this.colorTextNormal = colorTextNormal;
                this.colorTextSelected = colorTextSelected;
                this.textSize= textSize;
                this.isFunction = false;
                rectText = new Rect();
                doSetNotNumber(number);

            }
            private void doSetNotNumber(String number){
                if(number.equals("10")|| number.equals("11")){
                    if(number.equals("11")){
                        this.number = FUNCTION_DELETE;
                    }
                    if(number.equals("10")){
                        this.number = FUNCTION_BACK;
                    }
                    this.isFunction = true;
                    this.textSize= this.textSize / 2;
                    this.colorTextNormal = Color.DKGRAY;
                    this.colorTextSelected = Color.BLACK;
                    this.colorNormal = Color.TRANSPARENT;
                    this.colorSelected = Color.TRANSPARENT;

                }

            }
            public float getNumberStartX() {
                if(null != rectF && null != rectText) {
                    return (rectF.left+rectF.right)/2 - (rectText.left+rectText.right)/2;
                }else {
                    return -1f;
                }
            }
            public float getNumberStartY() {
                if(null != rectF && null != rectText) {
                    return (rectF.top+rectF.bottom)/2 - (rectText.top+rectText.bottom)/2;
                }else {
                    return -1f;
                }
            }
            public String getNumber() {
                return number;
            }
            public void setNumber(String number) {
                this.number = number;
            }
            public String getCharacter() {
                return character;
            }
            public void setCharacter(String character) {
                this.character = character;
            }
            public RectF getRectF() {
                return rectF;
            }
            public void setRectF(RectF rectF) {
                this.rectF = rectF;
            }
            public int getColorBackground() {
                return isSelected? colorSelected :colorNormal;
            }
            public int getTextColor() {
                return isSelected? colorTextSelected :colorTextNormal;
            }
            public int getTextSize(){
                return textSize;
            }
            public boolean isSelected() {
                return isSelected;
            }
            public boolean isFunction() {
                return isFunction;
            }

            public void setSelected(boolean selected) {
                isSelected = selected;
            }

            public Rect getRectText() {
                return rectText;
            }

        }
        /**
         * 接口，操作返回，或者输入密码结束后的结果
         */
        public interface OnPasswordListener{
            void onListenerBack(boolean isBack);
            void onListenerPasswordResult(String passwordInput, boolean isCorrect);

        }
        /**
         * 添加接口
         */
        public void setOnPasswordListener(OnPasswordListener onEventListener){
            if(null == mOnPasswordListener){
                mOnPasswordListener = onEventListener;
            }
        }
    }

