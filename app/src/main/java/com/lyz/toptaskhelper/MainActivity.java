package com.lyz.toptaskhelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lyz.toptaskhelper.tools.TaskManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TaskManager.init(getApplicationContext());
    }
}
