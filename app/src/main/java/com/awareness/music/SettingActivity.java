package com.awareness.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
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
    private final int PERMISSIONS_REQUEST_CODE = 940;
    private CheckBox mHeadsetCheckBox;
    private CheckBox mTimeCheckBox;
    private CheckBox mBehaviorCheckBox;
    private BarrierUpdateRequest.Builder mRequestBuilder;

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
            addBarrier(buttonView);
        } else {
            deleteBarrier(buttonView);
        }
    }

    private void addBarrier(CompoundButton buttonView) {
        mRequestBuilder = new BarrierUpdateRequest.Builder();
        boolean permissionHasGranted = true;
        switch (buttonView.getId()) {
            case R.id.cb_awareness_headset:
                AwarenessBarrier headsetBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED);
                int deviceType = 0;
                AwarenessBarrier bluetoothBarrier = BluetoothBarrier.keep(deviceType, BluetoothStatus.CONNECTED);
                AwarenessBarrier barrier = AwarenessBarrier.or(headsetBarrier, bluetoothBarrier);
                Intent intent = new Intent(this, BarrierReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mRequestBuilder.addBarrier(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL, barrier,
                        pendingIntent);
                break;
            case R.id.cb_awareness_time:
                permissionHasGranted = checkPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                List<BarrierParamEntity> timeBarrierList = MockData.getTimeBarrierList(this);
                for (BarrierParamEntity entity : timeBarrierList) {
                    mRequestBuilder.addBarrier(entity.getBarrierLabel(), entity.getBarrier(),
                            entity.getPendingIntent());
                }
                break;
            case R.id.cb_awareness_behavior:
                String[] permissions;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissions = new String[]{Manifest.permission.ACTIVITY_RECOGNITION};
                } else {
                    permissions = new String[]{"com.huawei.hms.permission.ACTIVITY_RECOGNITION"};
                }
                permissionHasGranted = checkPermissions(permissions);
                List<BarrierParamEntity> behaviorBarrierList = MockData.getBehaviorBarrierList(this);
                for (BarrierParamEntity entity : behaviorBarrierList) {
                    mRequestBuilder.addBarrier(entity.getBarrierLabel(), entity.getBarrier(),
                            entity.getPendingIntent());
                }
                break;
            default:
                break;
        }
        if (permissionHasGranted) {
            Awareness.getBarrierClient(this).updateBarriers(mRequestBuilder.build())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "setup success", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "setup failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "add barrier failed");
                        e.printStackTrace();
                        buttonView.setChecked(false);
                    });
        }
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


    private void deleteBarrier(CompoundButton buttonView) {
        BarrierUpdateRequest.Builder requestBuilder = new BarrierUpdateRequest.Builder();
        switch (buttonView.getId()) {
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
                    buttonView.setChecked(true);
                });
    }

    private boolean checkPermissions(String[] permissions) {
        boolean result = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionDenied = false;
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    permissionDenied = true;
                    break;
                }
            }
            if (permissionDenied) {
                Toast.makeText(this, "grant permission failed", Toast.LENGTH_SHORT).show();
            } else {
                Awareness.getBarrierClient(this).updateBarriers(mRequestBuilder.build())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Setup success", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Setup Failed", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            }
        }
    }
}
