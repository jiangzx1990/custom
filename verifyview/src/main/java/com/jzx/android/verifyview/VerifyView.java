package com.jzx.android.verifyview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.Nullable;

public class VerifyView extends View {
    private int textSize;//default 24dp
    private final int solidCircleSize;

    private int strokeWidth;
    private int strokeColorNotInput = 0xffe4e4e4;
    private int strokeColorFilled = 0xff333333;

    private int verifyCodeLength = 4;
    private int strokeGap; // default 8dp
    private int strokeHeight; // default 1dp

    private final Paint textPaint;

    private final Paint strokePaint;

    public VerifyView(Context context) {
        this(context,null);
    }

    public VerifyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VerifyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,24.0f,metrics);
        strokeGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8.0f,metrics);
        strokeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,1.0f,metrics);

        TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.VerifyView);
        textSize = array.getDimensionPixelSize(R.styleable.VerifyView_verifyTextSize,textSize);
        int textColor = array.getColor(R.styleable.VerifyView_verifyTextColor,0xff333333);
        strokeColorNotInput = array.getColor(R.styleable.VerifyView_verifyStrokeColorNotInput,strokeColorNotInput);
        strokeColorFilled = array.getColor(R.styleable.VerifyView_verifyStrokeColorFilled,strokeColorFilled);
        strokeGap = array.getDimensionPixelOffset(R.styleable.VerifyView_verifyStrokeGap,strokeGap);
        strokeHeight = array.getDimensionPixelOffset(R.styleable.VerifyView_verifyStrokeHeight,strokeHeight);
        verifyCodeLength = array.getInteger(R.styleable.VerifyView_verifyLength,verifyCodeLength);

        solidCircleSize = array.getDimensionPixelOffset(R.styleable.VerifyView_verifySolidCircleSize,0);

        array.recycle();

        if (verifyCodeLength < 1){
            throw new RuntimeException("verifyLength 不能小于 1，当前值为 :" + verifyCodeLength);
        }

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(strokeColorNotInput);

        setFocusableInTouchMode(true);
        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED){
            width = verifyCodeLength * textSize + (verifyCodeLength - 1) * strokeGap;
        }

        strokeWidth = (width - (verifyCodeLength - 1) * strokeGap) / verifyCodeLength;

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED){
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            if (solidCircleSize == 0){
                height = (int) (fontMetrics.bottom - fontMetrics.top) + strokeHeight;
            }else{
                height = solidCircleSize * 2 + strokeHeight;
            }
        }

        setMeasuredDimension(width,height);
    }

    private final Point start = new Point();
    private final Point end = new Point();

    private final StringBuilder inputs = new StringBuilder();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画线
        int y = getMeasuredHeight() - strokeHeight;
        strokePaint.setColor(strokeColorFilled);
        for (int i=0; i < verifyCodeLength; i++){
            start.x = i * strokeWidth + i * strokeGap;
            start.y = y;

            end.x = start.x + strokeWidth;
            end.y = start.y;
            if (inputs.length() == 0 || i >inputs.length() - 1){
                strokePaint.setColor(strokeColorNotInput);
            }
            canvas.drawLine(start.x, start.y,end.x,end.y,strokePaint);
        }

        if (solidCircleSize == 0){
            drawText(canvas);
        }else{
            drawBitmap(canvas);
        }
    }

    private void drawText(Canvas canvas){
        String text;
        //画文字
        for (int i = 0 ,length = inputs.length() ; i < length ; i++){
            text = String.valueOf(inputs.charAt(i));
            canvas.drawText(text,
                    (strokeWidth * i) + (strokeGap * i) + strokeWidth / 2.0f - textPaint.measureText(text) / 2,
                    getMeasuredHeight() / 2.0f + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent(),
                    textPaint);
        }
    }

    private void drawBitmap(Canvas canvas){
        textPaint.setColor(strokeColorFilled);
        for (int i = 0,length = inputs.length() ; i < length ; i++){
            canvas.drawCircle((strokeWidth * i) + (strokeGap * i) + strokeWidth / 2.0f - solidCircleSize /2.0f,
                    getMeasuredHeight() / 2.0f,
                    solidCircleSize / 2.0f,
                    textPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            requestFocus();
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this,InputMethodManager.RESULT_SHOWN);
            imm.restartInput(this);
        }
        return true;
    }

    private void appendText(String text){
        if (inputs.length() < verifyCodeLength){
            inputs.append(text);
            invalidate();

            if (listener != null &&inputs.length() == verifyCodeLength){
                listener.onVerifyComplete(inputs.toString());
            }
        }
    }

    private void deleteLast(){
        if (inputs.length() > 0){
            inputs.deleteCharAt(inputs.length() - 1);
            invalidate();
        }
    }

    public void clear(){
        if (inputs.length() > 0){
            inputs.delete(0,inputs.length());
            invalidate();
        }
    }

    public String getVerifyCode(){
        return inputs.toString();
    }

    private VerifyCompleteListener listener;

    public void setVerifyCompleteListener(VerifyCompleteListener listener){
        this.listener = listener;
    }

    public interface VerifyCompleteListener{
        void onVerifyComplete(String verify);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        return new BaseInputConnection(this,false){
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP){
                    switch (event.getKeyCode()){
                        case KeyEvent.KEYCODE_0:
                            appendText("0");
                            break;
                        case KeyEvent.KEYCODE_1:
                            appendText("1");
                            break;
                        case KeyEvent.KEYCODE_2:
                            appendText("2");
                            break;
                        case KeyEvent.KEYCODE_3:
                            appendText("3");
                            break;
                        case KeyEvent.KEYCODE_4:
                            appendText("4");
                            break;
                        case KeyEvent.KEYCODE_5:
                            appendText("5");
                            break;
                        case KeyEvent.KEYCODE_6:
                            appendText("6");
                            break;
                        case KeyEvent.KEYCODE_7:
                            appendText("7");
                            break;
                        case KeyEvent.KEYCODE_8:
                            appendText("8");
                            break;
                        case KeyEvent.KEYCODE_9:
                            appendText("9");
                            break;
                        case KeyEvent.KEYCODE_DEL:
                            deleteLast();
                            break;
                    }
                }
                return true;
            }
        };
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable p = super.onSaveInstanceState();
        bundle.putParcelable("super",p);
        bundle.putString("custom",inputs.toString());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable p = bundle.getParcelable("super");
        String custom = bundle.getString("custom");
        if (custom != null && custom.length() > 0){
            if (inputs.length() > 0){
                inputs.delete(0,inputs.length());
            }
            inputs.append(custom);
        }
        super.onRestoreInstanceState(p);
    }
}
