package com.walnutin.hardsdktest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.walnutin.HeartRateAdditional;
import com.walnutin.hardsdk.ProductList.sdk.GlobalValue;
import com.walnutin.hardsdk.ProductList.sdk.HardSdk;
import com.walnutin.hardsdk.ProductList.sdk.TimeUtil;
import com.walnutin.hardsdk.ProductNeed.Jinterface.SimpleDeviceCallback;
import com.walnutin.hardsdk.ProductNeed.entity.BloodOxygen;
import com.walnutin.hardsdk.ProductNeed.entity.BloodPressure;
import com.walnutin.hardsdk.ProductNeed.entity.Clock;
import com.walnutin.hardsdk.ProductNeed.entity.Drink;
import com.walnutin.hardsdk.ProductNeed.entity.ExerciseData;
import com.walnutin.hardsdk.ProductNeed.entity.HeartRateModel;
import com.walnutin.hardsdk.ProductNeed.entity.SleepModel;
import com.walnutin.hardsdk.ProductNeed.entity.StepInfos;
import com.walnutin.hardsdk.ProductNeed.entity.TempModel;
import com.walnutin.hardsdk.ProductNeed.entity.TempStatus;
import com.walnutin.hardsdk.ProductNeed.entity.Version;
import com.walnutin.hardsdk.ProductNeed.entity.Weather;
import com.walnutin.hardsdk.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    @BindView(R.id.query_bp_btn)
    Button queryBpBtn;
    @BindView(R.id.query_oxygen_btn)
    Button queryOxygenBtn;
    @BindView(R.id.btnBpMeasure)
    Button btnBpMeasure;
    @BindView(R.id.btnOxygenMeasure)
    Button btnOxygenMeasure;
    private String beforeDate;
    private Button searchBtn;
    private Button queryHeartBtn;
    private Button querySleepBtn;
    private Button queryStepBtn;
    private String TAG = MainActivity.class.getSimpleName();
    private EditText contentInfo;
    private final int EDIT_CHANGED = 1;
    private boolean isTestingHeart;
    private Button realHeartBtn;
    private Button sedentaryBtn;
    private Button alarmBtn;
    private Button callPushBtn;
    private Button resetBtn;
    private Button findBattery;
    private Button wristScreen;
    private Button btnCamera;
    private boolean isCalling;
    private Button updateBtn;
    private Button queryVersion;
    private Button resourceTransf;
    private Button btnTiwen;
    private Button btnSwitchTiwen;

    private Button btnQueryBodyTemp;
    private Button btnQueryWanWenTemp;

    private Button btnSyncWanWenTemp;
    private Button btnSyncBodyTemp;
    private Button btnMeasureYeWen;

    private boolean isTestingOxygen;
    private boolean isTestingBp;
    BloodPressure bloodPressure;
    int oxygen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initEvent();
        shenqing();
        //  requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
            )
                    .subscribe(permission -> {

                    });
        }
    }

    private void initView() {
        searchBtn = (Button) findViewById(R.id.search_device_btn);
        queryHeartBtn = (Button) findViewById(R.id.query_heart_btn);
        querySleepBtn = (Button) findViewById(R.id.query_sleep_btn);
        queryStepBtn = (Button) findViewById(R.id.query_step_btn);
        realHeartBtn = (Button) findViewById(R.id.realtime_heart_btn);
        contentInfo = (EditText) findViewById(R.id.content_info);
        sedentaryBtn = (Button) findViewById(R.id.set_sedentary_btn);
        alarmBtn = (Button) findViewById(R.id.alarm_btn);
        callPushBtn = (Button) findViewById(R.id.call_push_btn);
        updateBtn = (Button) findViewById(R.id.update_btn);
        resetBtn = (Button) findViewById(R.id.reset_btn);
        findBattery = (Button) findViewById(R.id.findBattery);
        wristScreen = (Button) findViewById(R.id.wristScreen);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        queryVersion = (Button) findViewById(R.id.queryVersion);
        resourceTransf = (Button) findViewById(R.id.resourceTransf);
        btnTiwen = (Button) findViewById(R.id.btnTiwen);
        btnSwitchTiwen = (Button) findViewById(R.id.btnSwitchTiwen);
        btnSyncBodyTemp = (Button) findViewById(R.id.btnSyncBodyTemp);
        btnSyncWanWenTemp = (Button) findViewById(R.id.btnSyncWanWenTemp);
        btnQueryBodyTemp = (Button) findViewById(R.id.btnQueryBodyTemp);
        btnQueryWanWenTemp = (Button) findViewById(R.id.btnQueryWanWenTemp);
        btnMeasureYeWen = (Button) findViewById(R.id.btnMeasureYeWen);

//        btnCall.setOnClickListener(v -> {
//
//            callPhone("17097218091");
//        });
    }

    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisibleViewed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisibleViewed = false;
    }

    private void initEvent() {
        HardSdk.getInstance().setHardSdkCallback(simpleDeviceCallback);

        searchBtn.setOnClickListener(this);
        queryHeartBtn.setOnClickListener(this);
        querySleepBtn.setOnClickListener(this);
        queryStepBtn.setOnClickListener(this);
        realHeartBtn.setOnClickListener(this);
        findBattery.setOnClickListener(this);
        wristScreen.setOnClickListener(this);
        queryVersion.setOnClickListener(this);
        sedentaryBtn.setOnClickListener(this);
        alarmBtn.setOnClickListener(this);
        callPushBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        resourceTransf.setOnClickListener(this);
        btnTiwen.setOnClickListener(this);
        btnSwitchTiwen.setOnClickListener(this);
        btnQueryWanWenTemp.setOnClickListener(this);
        btnQueryBodyTemp.setOnClickListener(this);
        btnSyncBodyTemp.setOnClickListener(this);
        btnSyncWanWenTemp.setOnClickListener(this);
        btnMeasureYeWen.setOnClickListener(this);


//        findViewById(R.id.readBanben).setOnClickListener(this);
        contentInfo.setOnLongClickListener(v -> {
            contentInfo.setText("");
            return true;
        });


        HardSdk.getInstance().setAccount("user1");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = simpleDateFormat.format(new Date());

        beforeDate = TimeUtil.getBeforeDay(TimeUtil.getCurrentDate(), 0);
    }

    String currentDate;

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.search_device_btn) {
            if (!HardSdk.getInstance().isDevConnected()) {
                contentInfo.append("Please connect the bracelet before performing this operation.\n");
                return;
            }
        }

        switch (view.getId()) {
            case R.id.search_device_btn:
                //跳转到新页面搜索和连接
                if (!HardSdk.getInstance().isDevConnected()) {
                    Intent intent = new Intent();
                    intent.setClass(this, SearchDeviceActivity.class);
                    startActivity(intent);
                } else {
                    HardSdk.getInstance().disconnect();
                }

                break;

            case R.id.realtime_heart_btn:
                //Start testing heart rate
                Log.d(TAG, "onClick: isTestingHeart:" + isTestingHeart);
                if (!isTestingHeart) {
                    HardSdk.getInstance().startRateTest();
                    isTestingHeart = true;
// contentInfo.append("Aidu bracelet does not support real-time heart rate measurement Start measuring heart rate:\n");
                    realHeartBtn.setText("Stop testing heart rate");
                } else {
                    HardSdk.getInstance().stopRateTest();
                    isTestingHeart = false;
                    realHeartBtn.setText("Start testing heart rate");//The countdown 30S APP will stop automatically after 30 seconds
                }

                break;
            case R.id.query_step_btn:
                StepInfos stepInfos = HardSdk.getInstance().queryOneDayStep(TimeUtil.getBeforeDay(TimeUtil.getCurrentDate(), 0));
                stepInfos.getStep();
                contentInfo.append("Step counting:" + new Gson().toJson(stepInfos) + "\n");
                break;
            case R.id.wristScreen:
                contentInfo.append(" Set the wrist to turn on the bright screen" + "\n");
                HardSdk.getInstance().setWristStatus(true, true);

                break;

            case R.id.query_heart_btn:
                contentInfo.append("Query heart rate:" + beforeDate + "\n");
                List<HeartRateModel> heartRateModels = HardSdk.getInstance().queryOneDayHeartRate(beforeDate);
                if (heartRateModels != null && heartRateModels.size()> 0) {
                    for (HeartRateModel heartRateModel: heartRateModels) {
                        contentInfo.append(beforeDate + "Heart rate test moment:" + heartRateModel.getTestMomentTime() + "Value:" + heartRateModel.getCurrentRate() + "\n");
                        //Other data is obtained from heartRateModel
                    }
                } else {
                    contentInfo.append("No" + beforeDate + "Heart Rate History" + "\n");
                }
                break;
            case R.id.query_sleep_btn:
                SleepModel sleepModel = HardSdk.getInstance().queryOneDaySleepInfo(beforeDate);
                contentInfo.append(" Sleep:" + new Gson().toJson(sleepModel) + "\n");
                break;
            case R.id.set_sedentary_btn:
                //Set sedentary
                HardSdk.getInstance().setSedentaryRemindCommand(1, 60, 1320, 600, 127); //Switch, time: minutes
                contentInfo.append("Set sedentary reminder interval to 60 minutes\n");
                break;

            case R.id.queryVersion:
                //Set sedentary
                HardSdk.getInstance().queryFirmVesion(); //Switch, time: minutes
                contentInfo.append("Query version information \n");
                break;

            case R.id.findBattery:
                //Find bracelet
                HardSdk.getInstance().findBattery();//Vibration times
                contentInfo.append("Query battery\n");
                break;

            case R.id.alarm_btn:
                //设置闹钟
                int i = TimeUtil.nowHour();
                byte weekper = 127;
                Log.d(TAG, "onClick: weekper" + weekper);
                long timeMillis = (System.currentTimeMillis() + 1000 * 60);
                int hour = TimeUtil.hourFromTimeMillis(timeMillis);
                int minitues = TimeUtil.minituesFromTimeMillis(timeMillis) + 1;
                Log.d(TAG, "onClick: hour:" + hour + " minitues:" + minitues);
                List<Clock> clockList = new ArrayList<>();
                Clock clock = new Clock();
                clock.setEnable(true);
                clock.setTime(String.valueOf(hour + ":" + minitues));
                clock.setRepeat(127);
                clock.setEnable(true);
                clock.setSerial(0);
                clockList.add(clock);
                Clock clock1 = new Clock();
                clock1.setEnable(true);
                clock1.setTime(String.valueOf(hour + ":" + (minitues + 1)));
                clock1.setRepeat(127);
                clock1.setEnable(true);
                clock1.setSerial(1);
                clockList.add(clock1);
                Clock clock2 = new Clock();
                clock2.setEnable(true);
                clock2.setTime(String.valueOf(hour + ":" + (minitues + 2)));
                clock2.setRepeat(127);
                clock2.setEnable(true);
                clock2.setSerial(2);
                clockList.add(clock2);

                HardSdk.getInstance().setAlarmList(clockList);
                contentInfo.append("设置闹钟：" + hour + " 时" + minitues + "分\n");
                break;

            case R.id.call_push_btn:
                //incoming call
                if (!isCalling) {
                    Log.d(TAG, "onClick: calling:" + 1);
                    HardSdk.getInstance().sendCallOrSmsInToBLE("14334343333", GlobalValue.TYPE_MESSAGE_PHONE, "Langyabang", "Langyabang");
                    callPushBtn.setText("Hang up the phone");
                    isCalling = true;
                    contentInfo.append("Push incoming calls\n");
                } else {
                    Log.d(TAG, "onClick: calling:" + 2);
                    // HardSdk.getInstance().sendOffHookCommand();
                    HardSdk.getInstance().sendOffHookCommand();
                    callPushBtn.setText("Call notification");
                    isCalling = false;
                    contentInfo.append("Stop pushing incoming calls\n");
                }
                break;
            case R.id.update_btn:
                //Firmware upgrade
                contentInfo.append("Perform firmware upgrade\n");

                HardSdk.getInstance().startUpdateBLE(); //Receive callback in callback
                break;
            case R.id.resourceTransf:
                //Firmware upgrade
                contentInfo.append("Transfer Resource UI File\n");
                HardSdk.getInstance().startUpgradePicture(); //
                break;
            case R.id.reset_btn:
                //reset
                HardSdk.getInstance().restoreFactoryMode();
                contentInfo.append("Perform factory reset\n");

                break;

            case R.id.btnCamera:
                isOpenCamera = !isOpenCamera;
                contentInfo.append("Whether the camera mode is enabled" + isOpenCamera + "\n");

                HardSdk.getInstance().openTakePhotoFunc(isOpenCamera);

                if (isOpenCamera) {
                    new Thread(() -> {
                        try {
                            while (isOpenCamera) {
                                Thread.sleep(3000);
                                HardSdk.getInstance().keepTakePhotoState();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                break;

            case R.id.btnSwitchTiwen: // Turn on the automatic temperature measurement switch

                HardSdk.getInstance().readTempValue();
                break;

            case R.id.btnSyncBodyTemp: // Turn on the automatic temperature measurement switch 0 represents today, 1 represents yesterday
                contentInfo.append("Start to synchronize body temperature \n");
                HardSdk.getInstance().syncLatestBodyTemperature(0);
                break;

            case R.id.btnSyncWanWenTemp: // Turn on the automatic temperature measurement switch
                contentInfo.append("Start synchronizing wrist temperature \n");
                HardSdk.getInstance().syncLatestWristTemperature(0);
                break;

            case R.id.btnQueryBodyTemp: //
                contentInfo.append("Start querying body temperature \n");
                List<TempModel> tempModelList = HardSdk.getInstance().getBodyTemperature("2020-04-06", "2020-04-08");
                contentInfo.append("Results of body temperature:\n" + new Gson().toJson(tempModelList) + "\n");
                break;

            case R.id.btnQueryWanWenTemp: //
                contentInfo.append("Start querying wrist temperature \n");
                List<TempModel> tempModelList2 = HardSdk.getInstance().getWristTemperature("2020-04-06", "2020-04-08");

                contentInfo.append("Wrist temperature result:\n" + new Gson().toJson(tempModelList2) + "\n");
                break;

            case R.id.btnTiwen: //Enter the temperature measurement page, then automatically read once every second, stop reading the body temperature according to the countdown
                HardSdk.getInstance().enterTempMeasure();
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                disposable = Flowable.interval(1, 1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(v -> {

                            HardSdk.getInstance().readTempValue(); // Read the current body temperature value
                        });
                break;

            case R.id.btnMeasureYeWen: // Axillary temperature measurement
                isMeasureArmpit = !isMeasureArmpit;
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                // measurement 5 minutes measurement start 0 seconds
                if (isMeasureArmpit == true) {
                    disposable = Flowable.intervalRange(0, 300, 0, 1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(v -> {
                                Log.d(TAG, "Countdown:" + (300-v));
                                HardSdk.getInstance().measureArmpittem(Math.toIntExact(v));
                            });
                    btnMeasureYeWen.setText("Stop measuring");
                } else {
                    btnMeasureYeWen.setText("Online measurement of axillary temperature");
                }

                break;


        }

    }

    boolean isMeasureArmpit;

    Disposable disposable;

    private void shenqing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE

            )
                    .subscribe(permission -> {
                    });
        }
    }


    boolean isOpenCamera = false;

    boolean isVisibleViewed;
    long lastUnLinkTime = 0;


    Version serverVersion = null;
    String version;

    SimpleDeviceCallback simpleDeviceCallback = new SimpleDeviceCallback() {
        @Override
        public void onCallbackResult(int flag, boolean state, Object obj) {
            super.onCallbackResult(flag, state, obj);
            if (flag == GlobalValue.BATTERY) {
                contentInfo.append("Electricity:" + obj + "\n");
            }
            if (flag == GlobalValue.CONNECTED_MSG) {
                Log.d(TAG, "onCallbackResult: successful connection");
                if (isVisibleViewed)
                    Toast.makeText(getApplicationContext(), "connection succeeded", Toast.LENGTH_LONG).show();
                searchBtn.setText("Disconnect");
                contentInfo.setText("");
            } else if (flag == GlobalValue.DISCONNECT_MSG) {
                Log.d(TAG, "onCallbackResult: Connection failed");
                if (isVisibleViewed) {
                    if (System.currentTimeMillis() / 1000-lastUnLinkTime / 1000> 3) {
                        lastUnLinkTime = System.currentTimeMillis();
                        Toast.makeText(getApplicationContext(), "disconnection", Toast.LENGTH_LONG).show();
                    }
                }
                searchBtn.setText("Search");
            } else if (flag == GlobalValue.CONNECT_TIME_OUT_MSG) {
                Log.d(TAG, "onCallbackResult: Connection timed out");
                Toast.makeText(getApplicationContext(), "Connection Timed Out", Toast.LENGTH_LONG).show();
                searchBtn.setText("Search");
            } else if (flag == GlobalValue.STEP_FINISH) {
                Log.d(TAG, "onCallbackResult: synchronous step counting completed");
                contentInfo.append("Synchronized step counting completed\n");
            } else if (flag == GlobalValue.OFFLINE_HEART_SYNC_OK) {
                Log.d(TAG, "onCallbackResult: Sync heart rate completed");
                contentInfo.append("Sync heart rate complete\n");
            } else if (flag == GlobalValue.SLEEP_SYNC_OK) {
                Log.d(TAG, "onCallbackResult: synchronous sleep complete");
                contentInfo.append("Synchronized sleep complete \n");
            } else if (flag == GlobalValue.OFFLINE_EXERCISE_SYNC_OK) {
                Log.d(TAG, "onCallbackResult: Synchronous workout completed");
                contentInfo.append("Synchronized workout completed\n");
            } else if (flag == GlobalValue.SYNC_FINISH) {
                Log.d(TAG, "onCallbackResult: synchronization completed");
                contentInfo.append("Sync completed\n");
                Toast.makeText(getApplicationContext(), "Sync completed", Toast.LENGTH_LONG).show();
            } else if (flag == GlobalValue.Firmware_Version) {
                contentInfo.append(" version information: "+ obj +" \n");
                version = (String) obj;
            } else if (flag == GlobalValue.Hardware_Version) {
                contentInfo.append(" Hardware version information: "+ obj +" \n");
            } else if (flag == GlobalValue.DISCOVERY_DEVICE_SHAKE) {
                contentInfo.append("The photo is ok,\n");

            } else if (flag == GlobalValue.Firmware_DownFile) {
                contentInfo.append("in download file: \n");

            } else if (flag == GlobalValue.Firmware_Start_Upgrade) {
                contentInfo.append("Start upgrade \n");

            } else if (flag == GlobalValue.Firmware_Info_Error) {
                contentInfo.append("Error accessing firmware version, please get firmware version \n");

            } else if (flag == GlobalValue.Firmware_Server_Status) {
                contentInfo.append("Is there a new version: "+ obj +" \n");
                if (obj != null) {
                    serverVersion = (Version) obj;
                    contentInfo.append("Server:" + new Gson().toJson(serverVersion));

                }
            } else if (flag == GlobalValue.Firmware_Upgrade_Progress) {
                contentInfo.append("Progress: "+ obj + "% \n");

            } else if (flag == GlobalValue.Firmware_Server_Failed) {
                contentInfo.append("The latest version information is not obtained, please check the new firmware \n");

            } else if (flag == GlobalValue.READ_TEMP_FINISH_2) {// -273.15 represents absolute 0 degrees as invalid value
                TempStatus tempStatus = (TempStatus) obj;
                contentInfo.append("The temperature is returning Body temperature: .." + tempStatus.bodyTemperature + "Wrist temperature:" + tempStatus.wristTemperature + "Countdown: "+ tempStatus.downTime +" \n");

                if (tempStatus.downTime == 0) {
                    if (disposable != null && !disposable.isDisposed())
                        disposable.dispose();
                }
            } else if (flag == GlobalValue.TEMP_HIGH) {//
                contentInfo.append("The temperature is too high...\n");
            } else if (flag == GlobalValue.SYNC_BODY_FINISH) {//
                contentInfo.append("Temperature temperature synchronization completed...\n");
            } else if (flag == GlobalValue.SYNC_WRIST_FINISH) {//
                contentInfo.append("Wrist temperature synchronization completed...\n");
            } else if (flag == GlobalValue.READ_ArmpitTemp) {// -273.15 represents absolute 0 degrees as invalid value
                Float yewen = (Float) obj;
                if (!Float.isNaN(yewen) && yewen> -273) {
                    contentInfo.append("Axillary temperature value..." + yewen + "\n");
                }
            } else if(flag ==GlobalValue.uiFileListName){

                if(obj ==null){
                    contentInfo.append("Not missing ui \n");
                }else {
                    List<String> uiList = (List<String>) obj; // If you want to transfer ui, transfer the corresponding ui collection file to the bracelet, the steps are basically the same language


                }

            }else if(flag ==GlobalValue.PIC_TRANSF_FINISH){
                contentInfo.append("File transfer completed \n");

            }else if(flag ==GlobalValue.PIC_TRANSF_START){
                contentInfo.append("File is starting to transfer \n");
            }else if(flag ==GlobalValue.PIC_TRANSF_ING){
                contentInfo.append("File transfer \n");
            }
        }

        @Override
        public void onStepChanged(int step, float distance, int calories, boolean finish_status) {
            Log.d(TAG, "onStepChanged: step:" + step);
            contentInfo.append("Step counting:" + step + "Distance:" + distance + "Calories:" + calories + "\n");
        }

        @Override
        public void onHeartRateChanged(int rate, int status) {
            super.onHeartRateChanged(rate, status);
            Log.d(TAG, "onHeartRateChanged: status:" + status);
            if (isTestingHeart == true) {
                isTestingHeart = true;
                realHeartBtn.setText("Stop testing heart rate");
                contentInfo.append("Real-time heart rate value: "+ rate + "\n");
                if (status == GlobalValue.RATE_TEST_FINISH) {
                    contentInfo.append("End of measuring heart rate" + "\n");
                    isTestingHeart = false;
                    realHeartBtn.setText("Start measuring heart rate");
                }
            } else if (isTestingOxygen == true) {
                HeartRateAdditional heartRateAdditional = new HeartRateAdditional(System.currentTimeMillis() / 1000, rate, height, weight, sex, yearOld);
                oxygen = heartRateAdditional.get_blood_oxygen();
                contentInfo.append("Real-time blood oxygen value: "+ oxygen + "\n");
                if (status == GlobalValue.RATE_TEST_FINISH) {
                    contentInfo.append("End of measuring blood oxygen" + "\n");
                    isTestingOxygen = false;
                }
            } else if (isTestingBp) {
                HeartRateAdditional heartRateAdditional = new HeartRateAdditional(System.currentTimeMillis() / 1000, rate, height, weight, sex, yearOld);
                bloodPressure = new BloodPressure();
                bloodPressure.systolicPressure = heartRateAdditional.get_systolic_blood_pressure();
                bloodPressure.diastolicPressure = heartRateAdditional.get_diastolic_blood_pressure();
                contentInfo.append("Real-time blood pressure value:" + bloodPressure.getDiastolicPressure() + "/" + bloodPressure.getSystolicPressure() + "\n");
                if (status == GlobalValue.RATE_TEST_FINISH) {
                    contentInfo.append("End of blood pressure measurement" + "\n");
                    isTestingBp = false;
                }
            }
        }
    };


    private int height = 170;
    private int weight = 60;
    private int sex = 0; // 0代表男
    private int yearOld = 30; //

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HardSdk.getInstance().removeHardSdkCallback(simpleDeviceCallback);
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.content_info) {
            contentInfo.clearComposingText();
        }
        return false;
    }

    @OnClick({R.id.syncStep, R.id.syncSleep, R.id.syncHeart, R.id.syncExercise, R.id.query_exercise_btn, R.id.queryHardVersion, R.id.checkFirmWare, R.id.reset
            , R.id.viewStatus, R.id.wechat})
    public void onViewClicked(View view) {

        if (view.getId() != R.id.search_device_btn) {
            if (!HardSdk.getInstance().isDevConnected()) {
                contentInfo.append("Please connect the bracelet before performing this operation.\n");
                return;
            }
        }

        switch (view.getId()) {
            case R.id.syncStep:
                HardSdk.getInstance().syncStepData(0);
                break;
            case R.id.syncSleep:
                HardSdk.getInstance().syncSleepData(0);
                break;
            case R.id.syncHeart:
                HardSdk.getInstance().syncHeartRateData(0);
                break;
            case R.id.syncExercise:
                HardSdk.getInstance().syncExerciseData(1574040045000l);//对应 时间2019-11-18 09:20:45，获取该时间后所有锻炼记录
                break;
            case R.id.queryHardVersion:
                HardSdk.getInstance().queryHardVesion();
                break;
            case R.id.checkFirmWare:
                version = "ITPOWER01_1.13.00_200420";
                HardSdk.getInstance().checkNewFirmware(version);
                break;
            case R.id.viewStatus:
                contentInfo.append("Current synchronization state:" + HardSdk.getInstance().getCurrentSyncState() + "\n");
                contentInfo.append("Current dfu state:" + HardSdk.getInstance().getCurrentDfuSyncState() + "\n");
                break;
            case R.id.reset:
                HardSdk.getInstance().reset();
                contentInfo.setText("The bracelet generally restarts after reset");

                break;
            case R.id.wechat:
                HardSdk.getInstance().sendQQWeChatTypeCommand(GlobalValue.TYPE_MESSAGE_WECHAT, "WeChat message is coming...");
                contentInfo.setText("Push WeChat Message");
                break;
            case R.id.query_exercise_btn:
                List<ExerciseData> exerciseDataList = HardSdk.getInstance().getExercise(TimeUtil.getYesterdayDate(), TimeUtil.getCurrentDate() + "23:59:59");
                contentInfo.append("exercise data \n" + new Gson().toJson(exerciseDataList));
                break;


        }
    }

    boolean isHuaShidu = true;

    @OnClick({R.id.setInfo, R.id.btnHeartSwitch, R.id.setWeatherUnit, R.id.setWeatherValue, R.id.setDrinkSit, R.id.setScreen, R.id.setMetronome, R.id.setWuRao, R.id.set_sedentary_btn})
    public void onViewClicked2(View view) {
        if (view.getId() != R.id.search_device_btn) {
            if (!HardSdk.getInstance().isDevConnected()) {
                contentInfo.append("Please connect the bracelet before performing this operation.\n");
                return;
            }
        }


        switch (view.getId()) {
            case R.id.setInfo:
                contentInfo.append("Set personal information \n");
                HardSdk.getInstance().setTimeUnitAndUserProfile(true, true, GlobalValue.SEX_BOY, 20, 60, 172, 140, 90, 180);
                break;
            case R.id.btnHeartSwitch:
                contentInfo.append("Turn on all-weather automatic heart rate measurement \n");
                HardSdk.getInstance().setAutoHealthTest(true);
                break;
            case R.id.setWeatherUnit:
                isHuaShidu = !isHuaShidu;
                if (isHuaShidu) {
                    contentInfo.append("Set the weather type to Fahrenheit \n");
                    HardSdk.getInstance().setWeatherType(true, GlobalValue.Unit_Fahrenheit);
                } else {
                    contentInfo.append("Set the weather type to Celsius \n");
                    HardSdk.getInstance().setWeatherType(true, GlobalValue.Unit_Celsius);
                }


                break;
            case R.id.setWeatherValue:
                contentInfo.append("Set weather forecast for the next 5 days \n");
                List<Weather> weatherList = new ArrayList<>();
                Random random = new Random();
                for (int i = 0; i <5; i++) {
                    Weather weather = new Weather();
                    weather.high = random.nextInt(20) + 10;
                    weather.low = random.nextInt(10) + 10;
                    weather.serial = i;
                    weather.humidity = 10;
                    weather.isDaisan = 0;
                    weather.time = TimeUtil.getBeforeDay(TimeUtil.getCurrentDate(), -i);
                    Log.d(TAG, "time: "+ new Date().toString());
                    weather.type = random.nextInt(5);
                    weatherList.add(weather);
                }

                HardSdk.getInstance().setWeatherList(weatherList);

                break;
            case R.id.setDrinkSit:
                contentInfo.append("Set Drinking Water Reminder \n");
                List<Drink> drinkList;
                drinkList = new ArrayList<>();
                drinkList.add(new Drink(0, "08:00", true, 127));
                drinkList.add(new Drink(1, "09:30", true, 127));
                drinkList.add(new Drink(2, "11:30", true, 1));
                drinkList.add(new Drink(3, "13:30", false, 64));
                drinkList.add(new Drink(4, "15:30", true, 16));
                drinkList.add(new Drink(5, "17:30", true, 127));
                drinkList.add(new Drink(6, "19:30", true, 127));
                drinkList.add(new Drink(7, "20:30", true, 127));
                HardSdk.getInstance().setDrinkWater(drinkList);
                break;
            case R.id.setScreen:
                contentInfo.append("Set the bright screen for 5 seconds \n");
                HardSdk.getInstance().setScreenOnTime(5);
                break;
            case R.id.setMetronome:
                contentInfo.append("Set metronome 100 \n");
                HardSdk.getInstance().setMetronome(100);
                break;
            case R.id.setWuRao: //
                contentInfo.append("Set Do Not Disturb Off Off Status 22:00-8PM \n");
                HardSdk.getInstance().setDistrub(false, 1320, 480);
                break;

        }
    }

    @OnClick({R.id.query_bp_btn, R.id.query_oxygen_btn, R.id.btnBpMeasure, R.id.btnOxygenMeasure})
    public void onViewClicked3(View view) {
        switch (view.getId()) {
            case R.id.query_bp_btn: //
                List<HeartRateModel> heartRateModelList = HardSdk.getInstance().queryOneDayOxygen(beforeDate);
                List<BloodPressure> bloodPressureList = new ArrayList<>();

                for (HeartRateModel heartRateModel : heartRateModelList) {
                    try {
                        HeartRateAdditional heartRateAdditional = new HeartRateAdditional(TimeUtil.detaiTimeToStamp(heartRateModel.testMomentTime) / 1000, heartRateModel.currentRate, height, weight, sex, yearOld);
                        BloodPressure bloodPressure = new BloodPressure();
                        bloodPressure.testMomentTime = heartRateModel.testMomentTime;
                        bloodPressure.setSystolicPressure(heartRateAdditional.get_systolic_blood_pressure());
                        bloodPressure.setDiastolicPressure(heartRateAdditional.get_diastolic_blood_pressure());
                        bloodPressureList.add(bloodPressure);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (bloodPressureList != null && bloodPressureList.size()> 0) {
                    contentInfo.append(beforeDate + "Blood pressure history:" + new Gson().toJson(bloodPressureList) + "\n");
                } else {
                    contentInfo.append("no" + beforeDate + "blood pressure history" + "\n");
                }

                break;
            case R.id.query_oxygen_btn:
                heartRateModelList = HardSdk.getInstance().queryOneDayOxygen(beforeDate);
                List<BloodOxygen> oxygenList = new ArrayList<>();
                for (HeartRateModel heartRateModel : heartRateModelList) {
                    try {
                        HeartRateAdditional heartRateAdditional = new HeartRateAdditional(TimeUtil.detaiTimeToStamp(heartRateModel.testMomentTime) / 1000, heartRateModel.currentRate, height, weight, sex, yearOld);
                        BloodOxygen bloodOxygen = new BloodOxygen();
                        bloodOxygen.testMomentTime = heartRateModel.testMomentTime;
                        bloodOxygen.oxygen = (heartRateAdditional.get_blood_oxygen());
                        oxygenList.add(bloodOxygen);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (oxygenList != null && oxygenList.size()> 0) {
                    contentInfo.append(beforeDate + "Oxygen test moment:" + new Gson().toJson(oxygenList) + "\n");
                    //Other data is obtained from heartRateModel
                } else {
                    contentInfo.append("No" + beforeDate + "Blood Oxygen History" + "\n");
                }
                break;
            case R.id.btnBpMeasure:
                if (isTestingBp == false) {
                    isTestingBp = true;
                    btnBpMeasure.setText("Stop measurement");
                    HardSdk.getInstance().startBpMeasure(); // Wait 30 seconds for the value
                } else {
                    isTestingBp = false;
                    btnBpMeasure.setText("Start measuring blood pressure");
                    HardSdk.getInstance().stopBpMeasure(bloodPressure);
                }
                break;
            case R.id.btnOxygenMeasure:
                if (isTestingOxygen == false) {
                    isTestingOxygen = true;
                    btnOxygenMeasure.setText("Stop measurement");
                    HardSdk.getInstance().startBpMeasure(); // Wait 30 seconds for the value
                } else {
                    isTestingOxygen = false;
                    btnOxygenMeasure.setText("Start measuring blood oxygen");
                    HardSdk.getInstance().stopOxygenMeasure(oxygen);
                }
                break;
        }
    }

    @OnClick({R.id.btnCheckUi, R.id.btnTransUi, R.id.btnChangeLanguage})
    public void onViewFile(View view) {
// if (view.getId() != R.id.search_device_btn) {
// if (!HardSdk.getInstance().isDevConnected()) {
// contentInfo.append("Please connect the bracelet before performing this operation.\n");
// return;
//}
//}

        // Transfer the file and check getCurrentDfuSyncState if it is GlobalValue.SYNC_IDLE state can be transmitted, otherwise the dfu channel will block
        contentInfo.append("Current Dfu state:"+HardSdk.getInstance().getCurrentSyncState()+"\n");
        //The PIC_TRANSF_ING file is being transferred, // PIC_TRANSF_START starts transferring the first file // PIC_TRANSF_FINISH file transfer is completed,
        switch (view.getId()) {
            case R.id.btnCheckUi:

                contentInfo.append("Check if UI file is missing.\n"); //GlobalValue.uiFileListName data is null means no need to pass ui, otherwise it will return List<String> data
                HardSdk.getInstance().checkUIFile();
                break;
            case R.id.btnTransUi: // Only need to pass the missing ui
                copyFilesFassets(getApplicationContext(),"ui",getExternalFilesDir("ui").getPath());
                List<File> uifileList = getFileList(getExternalFilesDir("ui").getPath()); // The example here is to get all ui, just get the list of files with missing ui
                HardSdk.getInstance().startTransUI(uifileList);

                break;
            case R.id.btnChangeLanguage:

                copyFilesFassets(getApplicationContext(),"language",getExternalFilesDir("language").getPath());//Copy assets language files to the project's internal storage

                List<File> fileList = getFileList(getExternalFilesDir("language/zh").getPath());
                Log.d(TAG,"fileList:"+fileList.size());
                HardSdk.getInstance().startTransLanguage(fileList,"zh"); // Change to German, if the de directory does not exist, pass the default English en directory to the bracelet
                break;
        }
    }
    List<File> getFileList(String dirPath){
        List<File> stringList = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if(files != null&& files.length != 0){
            for (File file : files) {
                if (file.isFile()) {
                    stringList.add(file);
                }
            }
        }
        return stringList;
    }



    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  context  Context 使用CopyFiles类的Activity
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
        }
    }
}
