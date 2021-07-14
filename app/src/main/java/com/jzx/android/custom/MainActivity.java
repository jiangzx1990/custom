package com.jzx.android.custom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;
import com.jzx.android.verifyview.VerifyView;

public class MainActivity extends AppCompatActivity {
    VerifyView verifyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyView = findViewById(R.id.verify2);
        verifyView.setVerifyCompleteListener(new VerifyView.VerifyCompleteListener() {
            @Override
            public void onVerifyComplete(String verify) {
                hideKeyBoard(verifyView);
                Snackbar bar = Snackbar.make(verifyView,"",Snackbar.LENGTH_LONG);
                if ("888888".equals(verify)){
                    hideKeyBoard(verifyView);
                    bar.setText("验证码正确");
                }else{
                    bar.setText("验证码错误，请重新输入");
                    verifyView.clear();
                }
                bar.show();
            }
        });
    }


    private void hideKeyBoard(View focusedView){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusedView.getWindowToken(),0);
    }
}