package com.awareness.music;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.BluetoothBarrier;
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier;
import com.huawei.hms.kit.awareness.status.BluetoothStatus;
import com.huawei.hms.kit.awareness.status.HeadsetStatus;

public class SettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        RadioGroup rgScenario = findViewById(R.id.rg_scenario);
        rgScenario.setOnCheckedChangeListener(this);
        RadioGroup rgRecommendation = findViewById(R.id.rg_recommendation);
        rgRecommendation.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String label;
        if (group.getId() == R.id.rg_scenario) {
            label = "";
            AwarenessBarrier headsetBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED);
            int deviceType = 0;
            AwarenessBarrier bluetoothBarrier = BluetoothBarrier.keep(deviceType, BluetoothStatus.CONNECTED);
            AwarenessBarrier barrier = AwarenessBarrier.or(headsetBarrier, bluetoothBarrier);
            switch (checkedId) {
                case R.id.rb_sendNotification:
                    break;
                case R.id.rb_openActivity:
                    break;
                default:
                    break;
            }
        }
    }

    private void addBarrier(String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest.Builder requestBuilder = new BarrierUpdateRequest.Builder();
        requestBuilder.addBarrier(label, barrier, pendingIntent);
        Awareness.getBarrierClient(this).updateBarriers(requestBuilder.build())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "add barrier success"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "add barrier failed", e);
                    e.printStackTrace();
                });
    }
}
