package com.walnutin.hardsdktest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.walnutin.hardsdk.ProductList.sdk.HardSdk;
import com.walnutin.hardsdk.ProductNeed.Jinterface.IHardScanCallback;
import com.walnutin.hardsdk.ProductNeed.Jinterface.IHardSdkCallback;
import com.walnutin.hardsdk.ProductNeed.Jinterface.SimpleDeviceCallback;
import com.walnutin.hardsdk.ProductNeed.entity.Device;
import com.walnutin.hardsdk.ProductList.sdk.GlobalValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by chenliu on 2017/4/14.
 */

public class SearchDeviceActivity extends Activity implements IHardScanCallback {

    private RecyclerView mRecyclerView;
    private MyAdapter mMyAdapter;
    private final String TAG = SearchDeviceActivity.class.getSimpleName();
    private ProgressBar mProgressBar;
    private RelativeLayout rlProgress;
    private EditText edtGuolv;

    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private void initBLE() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not support", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "BLE  support", Toast.LENGTH_SHORT).show();
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initBLE();
        if(BluetoothAdapter.getDefaultAdapter() != null){
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
            }
        }else{
        }
        initView();
        initEvent();

    }



    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.search_result);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        rlProgress =  findViewById(R.id.rlProgress);
        edtGuolv =  findViewById(R.id.edtGuolv);
        TextView txtRetry = findViewById(R.id.txtRetry);

        txtRetry.setOnClickListener(v->{
            mMyAdapter.clearData();
            HardSdk.getInstance().stopScan();
            HardSdk.getInstance().startScan();
        });

    }

    private void initEvent() {
        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        edtGuolv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(s)){
                    isEditYanzheng = true;
                    reGeneratorList();
                }else {
                    isEditYanzheng =false;
                    reGeneratorList();
                }
                Log.d(TAG,"onTextChanged: "+s+" isEidtYan: "+isEditYanzheng);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        HardSdk.getInstance().setHardScanCallback(this);
        HardSdk.getInstance().setHardSdkCallback(simpleDeviceCallback);
        if (!HardSdk.getInstance().isSupportBle4_0()) {
            Toast.makeText(HardSdk.getInstance().getContext(), "The current device does not support ble 4.0", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(HardSdk.getInstance().getContext(), "Please turn on Bluetooth before searching", Toast.LENGTH_LONG).show();
            finish();
        }else {
            Toast.makeText(HardSdk.getInstance().getContext(), "Start scan", Toast.LENGTH_LONG).show();
            HardSdk.getInstance().startScan();
        }
    }

    SimpleDeviceCallback simpleDeviceCallback =new SimpleDeviceCallback() {
        @Override
        public void onCallbackResult(int flag, boolean state, Object obj) {
            super.onCallbackResult(flag, state, obj);
            Log.d(TAG, "GET RESULT___");
            if (flag == GlobalValue.CONNECTED_MSG) {
                Log.d(TAG, "onCallbackResult: 连接成功");
                if(isVisibleViewed) {
                    Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_LONG).show();
                }
                mProgressBar.setVisibility(View.GONE);
                rlProgress.setVisibility(View.GONE);
                MyApplication.deviceName = deviceName;
                MyApplication.deviceAddr = deviceAddr;
                HardSdk.getInstance().stopScan();
                edtGuolv.setText("");
//                MySharedPf.getInstance(getApplicationContext()).setDeviceName(deviceName);
//                MySharedPf.getInstance(getApplicationContext()).setDeviceMacAddress(deviceAddr);
                finish();
            } else if (flag == GlobalValue.DISCONNECT_MSG) {
                Log.d(TAG, "onCallbackResult: 连接失败");
                if(isVisibleViewed)
                    Toast.makeText(getApplicationContext(), "连接断开", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                rlProgress.setVisibility(View.GONE);
                HardSdk.getInstance().startScan();
            } else if (flag == GlobalValue.CONNECT_TIME_OUT_MSG) {
                Log.d(TAG, "onCallbackResult: 连接超时");
                Toast.makeText(getApplicationContext(), "连接超时", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                rlProgress.setVisibility(View.GONE);
                HardSdk.getInstance().startScan();
            }
        }
    };

    boolean isEditYanzheng =false;
    List<Device>backList =new ArrayList<>();
    void reGeneratorList(){
        if(isEditYanzheng){
            List<Device>tmpDev =new ArrayList<>();
            for (Device device: deviceInfoList){
                if(device.deviceName.contains(edtGuolv.getText().toString().trim().toUpperCase())){
                    tmpDev.add(device);
                }
            }
            backList = deviceInfoList;
            deviceInfoList =  tmpDev;
            mMyAdapter.notifyDataSetChanged();
        }else {
            if(backList.size()> deviceInfoList.size()) {
                deviceInfoList = backList;
            }
            mMyAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onFindDevice(BluetoothDevice device, int rssi, String factoryNameByUUID, byte[] scanRecord) {
        String value  = byteArrHexToString(scanRecord); //byte转成字符串
        int wan = byteArrToShort(hex2byte(reverseHexHighTwoLow(value.substring(22, 26))));
        String connState  =  value.substring(26, 28) ;  // 设备未连接 "00"代表未连接，01代表连接
        float tiwen= wan / 100f;
        Log.d(TAG, "onFindDevice: device:" + device.getName() + " " + device.getAddress()+" 体温: "+tiwen  );
        if(!TextUtils.isEmpty(device.getName())  && deviceInfoList.size()<200 ) {
            mMyAdapter.addDevice(new Device(factoryNameByUUID, device.getName(), device.getAddress(),rssi));
        }
    }


    public static short byteArrToShort(byte[] b){
        return byteArrToShort(b,0);
    }
    public static short byteArrToShort(byte[] b, int index) {
        return (short) (((b[index + 0] << 8) | b[index + 1] & 0xff));
    }



    @Override
    protected void onResume() {
        super.onResume();
        isVisibleViewed =true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisibleViewed =false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HardSdk.getInstance().stopScan();
        HardSdk.getInstance().removeHardScanCallback(this);
        HardSdk.getInstance().removeHardSdkCallback(simpleDeviceCallback);
    }
    boolean isVisibleViewed;


    String deviceName;
    String deviceAddr;
    private List<Device> deviceInfoList = new ArrayList<>();

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements View.OnClickListener {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(SearchDeviceActivity.this).inflate(R.layout.item_recyclerview, parent, false);
            layout.setOnClickListener(this);
            MyViewHolder myViewHolder = new MyViewHolder(layout);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            if (holder.deviceText.getTag(R.id.tag_listener) == null) {
                holder.deviceText.setOnClickListener(this);
                holder.deviceText.setTag(R.id.tag_listener, true);
            }
            holder.deviceText.setTag(R.id.tag_position, position);
            Log.d(TAG, "onBindViewHolder: deviceInfoList:" + deviceInfoList.size() + "  positon:" + position + "  deviceInfoList.get(position)" + deviceInfoList.get(position));

            boolean isFind = false;
            boolean isSuccess =false;
            if (isFind) {
                holder.deviceText.setTextColor(0xffffffff);
                holder.deviceText.setBackgroundColor(0xffee0000);
                holder.deviceText.setText(deviceInfoList.get(position).getDeviceName()+"   "+deviceInfoList.get(position).rssi + " (未通过)");
            } else if(isSuccess) {
                holder.deviceText.setTextColor(0xffffffff);
                holder.deviceText.setBackgroundColor(0xff00cc00);
                holder.deviceText.setText(deviceInfoList.get(position).getDeviceName()+"   "+deviceInfoList.get(position).rssi  +  " (通过)");
            }else {
                holder.deviceText.setBackgroundColor(0xffffffff);
                holder.deviceText.setTextColor(0xff434343);
//                holder.deviceText.setText(deviceInfoList.get(position).getDeviceName() + " " + deviceInfoList.get(position).getDeviceAddr());
                holder.deviceText.setText(deviceInfoList.get(position).getDeviceName()+"   "+deviceInfoList.get(position).rssi  );

            }
        }

        @Override
        public int getItemCount() {
            return deviceInfoList.size();
        }

        @Override
        public void onClick(View view) {
            if (view != null) {
                Object tag = view.getTag(R.id.tag_position);
                if (tag != null) {
                    int positon = (int) tag;
                    Device device = deviceInfoList.get(positon);
                    HardSdk.getInstance().stopScan();
                    deviceName = device.getDeviceName();
                    deviceAddr = device.getDeviceAddr();
                    HardSdk.getInstance().refreshBleServiceUUID(device.getFactoryName(), device.getDeviceName(), device.getDeviceAddr(),getApplicationContext(),true);
                    mMyAdapter.clearData();
                    mProgressBar.setVisibility(View.VISIBLE);
                    rlProgress.setVisibility(View.VISIBLE);
                }
            }
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView deviceText;

            public MyViewHolder(View itemView) {
                super(itemView);
                initHolderView(itemView);
            }

            private void initHolderView(View itemView) {
                deviceText = (TextView) itemView.findViewById(R.id.item_recyclerview);
            }
        }

        public void addDevice(com.walnutin.hardsdk.ProductNeed.entity.Device deviceInfo) {

            for (Device info : deviceInfoList) {
                if (info.getDeviceAddr().equals(deviceInfo.getDeviceAddr())) {
                    return;
                }
            }

            if((isEditYanzheng && deviceInfo.getDeviceName().contains(edtGuolv.getText().toString())) || !isEditYanzheng) {
                deviceInfoList.add(deviceInfo);
                notifyDataSetChanged();
            }
        }


        public void clearData() {
            deviceInfoList.clear();
            notifyDataSetChanged();
        }
    }

    private static String byteArrHexToString(byte[] b) {
        String ret = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xff);
            if (hex.length() % 2 == 1) {
                hex = '0' + hex;
            }
            ret += hex;
        }

        return ret.toUpperCase();
    }

    // 高位转地位 比如 b4d2u589 -> 89u5d2b4
    private static String reverseHexHighTwoLow(String value) {
        StringBuffer sbf = new StringBuffer();
        int j = 0;
        for (int i = 0; i < value.length() / 2; i++) {
            sbf.insert(0, value.substring(j, j + 2));
            j = j + 2;
        }
        return sbf.toString();
    }
    public static final byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }
}
