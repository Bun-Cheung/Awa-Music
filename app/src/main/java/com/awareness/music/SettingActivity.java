package com.awareness.music;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.awareness.music.entity.BarrierParamEntity;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierQueryRequest;
import com.huawei.hms.kit.awareness.barrier.BarrierStatusMap;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.BluetoothBarrier;
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier;
import com.huawei.hms.kit.awareness.status.BluetoothStatus;
import com.huawei.hms.kit.awareness.status.HeadsetStatus;

import java.util.List;
import java.util.Set;

public class SettingActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener {
    private final String TAG = getClass().getSimpleName();
    private CheckBox mHeadsetCheckBox;
    private CheckBox mTimeCheckBox;
    private CheckBox mBehaviorCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        queryBarrier();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        toolbar.setTitle("Setting");
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
        mHeadsetCheckBox = findViewById(R.id.cb_awareness_headset);
        mHeadsetCheckBox.setOnCheckedChangeListener(this);
        mTimeCheckBox = findViewById(R.id.cb_awareness_time);
        mTimeCheckBox.setOnCheckedChangeListener(this);
        mBehaviorCheckBox = findViewById(R.id.cb_awareness_behavior);
        mBehaviorCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) {
            return;
        }
        if (isChecked) {
            addBarrier(buttonView.getId());
        } else {
            deleteBarrier(buttonView.getId());
        }
    }

    private void addBarrier(int viewId) {
        BarrierUpdateRequest.Builder requestBuilder = new BarrierUpdateRequest.Builder();
        switch (viewId) {
            case R.id.cb_awareness_headset:
                AwarenessBarrier headsetBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED);
                int deviceType = 0;
                AwarenessBarrier bluetoothBarrier = BluetoothBarrier.keep(deviceType, BluetoothStatus.CONNECTED);
                AwarenessBarrier barrier = AwarenessBarrier.or(headsetBarrier, bluetoothBarrier);
                Intent intent = new Intent(this, BarrierReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                requestBuilder.addBarrier(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL, barrier,
                        pendingIntent);
                break;
            case R.id.cb_awareness_time:
                List<BarrierParamEntity> timeBarrierList = MockData.getTimeBarrierList(this);
                for (BarrierParamEntity entity : timeBarrierList) {
                    requestBuilder.addBarrier(entity.getBarrierLabel(), entity.getBarrier(),
                            entity.getPendingIntent());
                }
                break;
            case R.id.cb_awareness_behavior:
                List<BarrierParamEntity> behaviorBarrierList = MockData.getBehaviorBarrierList(this);
                for (BarrierParamEntity entity : behaviorBarrierList) {
                    requestBuilder.addBarrier(entity.getBarrierLabel(), entity.getBarrier(),
                            entity.getPendingIntent());
                }
                break;
            default:
                break;
        }
        Awareness.getBarrierClient(this).updateBarriers(requestBuilder.build())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "setup success", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "setup failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "add barrier failed");
                    e.printStackTrace();
                });
    }

    private void queryBarrier() {
        BarrierQueryRequest request = BarrierQueryRequest.all();
        Awareness.getBarrierClient(this).queryBarriers(request)
                .addOnSuccessListener(barrierQueryResponse -> {
                    BarrierStatusMap map = barrierQueryResponse.getBarrierStatusMap();
                    Set<String> labelSet = map.getBarrierLabels();
                    if (labelSet.contains(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL)) {
                        mHeadsetCheckBox.setChecked(true);
                    }
                    if (labelSet.contains(Constant.MORNING_LABEL)) {
                        mTimeCheckBox.setChecked(true);
                    }
                    if (labelSet.contains(Constant.RUNNING_LABEL)) {
                        mBehaviorCheckBox.setChecked(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "query barrier failed");
                    e.printStackTrace();
                });
    }


    private void deleteBarrier(int viewId) {
        BarrierUpdateRequest.Builder requestBuilder = new BarrierUpdateRequest.Builder();
        switch (viewId) {
            case R.id.cb_awareness_headset:
                requestBuilder.deleteBarrier(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL);
                break;
            case R.id.cb_awareness_time:
                for (BarrierParamEntity entity : MockData.getTimeBarrierList(this)) {
                    requestBuilder.deleteBarrier(entity.getBarrierLabel());
                }
                break;
            case R.id.cb_awareness_behavior:
                for (BarrierParamEntity entity : MockData.getBehaviorBarrierList(this)) {
                    requestBuilder.deleteBarrier(entity.getBarrierLabel());
                }
                break;
            default:
                break;
        }
        Awareness.getBarrierClient(this).updateBarriers(requestBuilder.build())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "delete barrier success"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "delete barrier failed");
                    e.printStackTrace();
                });
    }
}
