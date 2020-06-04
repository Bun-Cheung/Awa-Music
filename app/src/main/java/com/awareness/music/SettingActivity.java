package com.awareness.music;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.awareness.music.entity.BarrierParamEntity;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.BluetoothBarrier;
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier;
import com.huawei.hms.kit.awareness.status.BluetoothStatus;
import com.huawei.hms.kit.awareness.status.HeadsetStatus;

import java.util.List;

public class SettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String IS_CHECKED_KEY = "key_scenarioCheckId";
    private static final String RECOMMENDATION_CHECK_ID_KEY = "key_recommendationCheckId";
    private final String TAG = getClass().getSimpleName();
    private SharedPreferences mSP;
    private boolean mCheckBoxIsChecked;
    private int mRecommendationCheckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mSP = getSharedPreferences(Constant.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        mCheckBoxIsChecked = mSP.getBoolean(IS_CHECKED_KEY, false);
        mRecommendationCheckId = mSP.getInt(RECOMMENDATION_CHECK_ID_KEY, -1);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = mSP.edit();
        editor.putBoolean(IS_CHECKED_KEY, mCheckBoxIsChecked);
        editor.putInt(RECOMMENDATION_CHECK_ID_KEY, mRecommendationCheckId);
        editor.apply();
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
        CheckBox checkBox = findViewById(R.id.cb_isNotifyUser);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            handleUserCheck(isChecked);
        });
        checkBox.setChecked(mCheckBoxIsChecked);
        RadioGroup rgRecommendation = findViewById(R.id.rg_recommendation);
        rgRecommendation.setOnCheckedChangeListener(this);
        rgRecommendation.check(mRecommendationCheckId);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton view = group.findViewById(checkedId);
        if (view != null && !view.isPressed()) {
            return;
        }
        switch (checkedId) {

        }
    }

    private void handleUserCheck(boolean isCheck) {
        if (isCheck) {
            AwarenessBarrier headsetBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED);
            int deviceType = 0;
            AwarenessBarrier bluetoothBarrier = BluetoothBarrier.keep(deviceType, BluetoothStatus.CONNECTED);
            AwarenessBarrier barrier = AwarenessBarrier.or(headsetBarrier, bluetoothBarrier);
            Intent intent = new Intent(this, BarrierReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            addBarrier(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL, barrier, pendingIntent);
            mCheckBoxIsChecked = true;
        } else {
            //delete barrier
            BarrierUpdateRequest request = new BarrierUpdateRequest.Builder().
                    deleteBarrier(Constant.HEADSET_BLUETOOTH_BARRIER_LABEL).build();
            Awareness.getBarrierClient(this).updateBarriers(request)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "delete headset barrier success"))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "delete headset barrier failed", e);
                        e.printStackTrace();
                    });
            mCheckBoxIsChecked = false;
        }
    }

    private void addBarrier(String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest.Builder requestBuilder = new BarrierUpdateRequest.Builder();
        requestBuilder.addBarrier(label, barrier, pendingIntent);
        Awareness.getBarrierClient(this).updateBarriers(requestBuilder.build())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "add barrier success"))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "setup failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "add barrier failed", e);
                    e.printStackTrace();
                });
    }

    private void addBatchBarrier(List<BarrierParamEntity> barrierList) {
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        for (BarrierParamEntity entity : barrierList) {
            builder.addBarrier(entity.getBarrierLabel(), entity.getBarrier(), entity.getPendingIntent());
        }
        Awareness.getBarrierClient(this).updateBarriers(builder.build())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "add barrier success"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "add barrier failed", e);
                    e.printStackTrace();
                });
    }
}
