package com.example.danhui_cosmos_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        startLoading();

    }
    private void startLoading() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("로딩 이벤트 종료 테스트","가동됨");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

        }, 2000);

    }
}
