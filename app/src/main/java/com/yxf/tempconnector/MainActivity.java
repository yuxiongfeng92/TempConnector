package com.yxf.tempconnector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.proton.temp.connector.TempConnectorManager;
import com.proton.temp.connector.bean.ConnectionType;
import com.proton.temp.connector.bean.DeviceBean;
import com.proton.temp.connector.bean.TempDataBean;
import com.proton.temp.connector.bluetooth.BleConnector;
import com.proton.temp.connector.bluetooth.callback.OnScanListener;
import com.proton.temp.connector.interfaces.ConnectStatusListener;
import com.proton.temp.connector.interfaces.DataListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wms.ble.utils.BluetoothUtils;
import com.wms.logger.Logger;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    final RxPermissions rxPermissions = new RxPermissions(this);
    ScanAdapter adapter;
    private List<DeviceBean> datum = new ArrayList<>();
    private TextView txtTemp;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.w("success");
        //需要申请定位权限，否则高版本手机调用蓝牙扫描方法不起作用
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        // All requested permissions are granted
                        findViewById(R.id.id_scan).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                scan();
                            }
                        });
                    } else {
                        // At least one permission is denied
                        finish();
                    }
                });

        RecyclerView scanRecyclerView = findViewById(R.id.id_recycler);
        scanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScanAdapter(datum, this);
        scanRecyclerView.setAdapter(adapter);
        txtTemp = findViewById(R.id.id_temp);


        adapter.setListener(new ScanAdapter.ConnectListener() {
            @Override
            public void connectListener(DeviceBean deviceBean) {
                //连接体温贴
                connect(deviceBean);
            }

            @Override
            public void disconnectListener(DeviceBean deviceBean) {
                TempConnectorManager.getInstance(deviceBean).disConnect();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (TempConnectorManager.getInstance(device).isConnected()) {
            TempConnectorManager.getInstance(device).disConnect();
        }
    }

    private void scan() {
        Logger.w("start scan...");
        if (!BluetoothUtils.isBluetoothOpened()) {
            BluetoothUtils.openBluetooth();
            return;
        }

        datum.clear();
        adapter.notifyDataSetChanged();

        BleConnector.scanDevice( new OnScanListener() {
            @Override
            public void onDeviceFound(DeviceBean device) {
                Logger.w("扫描到的设备信息: ", device.toString());
                //看看当前设备是否已经添加
                if (datum != null && datum.size() > 0) {
                    for (DeviceBean tempDevice : datum) {
                        if (tempDevice.getMacaddress().equalsIgnoreCase(device.getMacaddress())) {
                            return;
                        }
                    }
                }
                datum.add(device);
                adapter.notifyItemInserted(datum.size());
            }
        });
    }
    DeviceBean device;
    private void connect(DeviceBean device) {
        //停止搜索
        BleConnector.stopScan();
        this.device=device;
        TempConnectorManager.getInstance(device)
//                .setConnectionType(ConnectionType.BROADCAST)//广播方式连接
                .setConnectionType(ConnectionType.BLUETOOTH)//蓝牙方式连接
                .connect(connectStatusListener, dataLisener, true);

    }

    private ConnectStatusListener connectStatusListener = new ConnectStatusListener() {
        @Override
        public void onConnectSuccess() {
            super.onConnectSuccess();
            Logger.w("onConnectSuccess");
            Toast.makeText(MainActivity.this, "onConnectSuccess", Toast.LENGTH_SHORT).show();
//            if (TempConnectorManager.getInstance(device).getConnectionType()==ConnectionType.BLUETOOTH) {
//                TempConnectorManager.getInstance(device).switchConnectionType(ConnectionType.BROADCAST);
//            }
        }

        @Override
        public void onConnectFaild() {
            super.onConnectFaild();
            Logger.w("onConnectFaild");
            Toast.makeText(MainActivity.this, "onConnectFaild", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnect(boolean isManual) {
            super.onDisconnect(isManual);
            Logger.w("onDisconnect");
            Toast.makeText(MainActivity.this, "onDisconnect", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void receiveReconnectTimes(int retryCount, int leftCount, long totalTime) {
            super.receiveReconnectTimes(retryCount, leftCount, totalTime);
            Logger.w("receiveReconnectTimes");
            Toast.makeText(MainActivity.this, "receiveReconnectTimes", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void receiveNotSampleDevice(String oldMac, String newMac) {
            super.receiveNotSampleDevice(oldMac, newMac);
            Logger.w("receiveNotSampleDevice");
            Toast.makeText(MainActivity.this, "receiveNotSampleDevice", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void receiveDockerOffline(boolean isOffline) {
            super.receiveDockerOffline(isOffline);
            Logger.w("receiveDockerOffline");
            Toast.makeText(MainActivity.this, "receiveDockerOffline", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void showBeforeMeasureDisconnect() {
            super.showBeforeMeasureDisconnect();
            Logger.w("showBeforeMeasureDisconnect");
            Toast.makeText(MainActivity.this, "showBeforeMeasureDisconnect", Toast.LENGTH_SHORT).show();

        }
    };

    private DataListener dataLisener = new DataListener() {
        @Override
        public void receiveCurrentTemp(float currentTemp, float algorithmTemp) {
            super.receiveCurrentTemp(currentTemp, algorithmTemp);
            Logger.w("currentTemp: ", currentTemp, ", algorithmTemp : ", algorithmTemp);
            txtTemp.setText(String.format("currentTemp:%s , algorithmTemp : %s ", currentTemp, algorithmTemp));
        }

        @Override
        public void receiveCurrentTemp(List<TempDataBean> temps) {
            super.receiveCurrentTemp(temps);
        }

        @Override
        public void receiveCacheTemp(List<TempDataBean> cacheTemps) {//仅在蓝牙连接模式下起作用
            super.receiveCacheTemp(cacheTemps);
            for (int i = 0; i < cacheTemps.size(); i++) {
                Log.d("cache temp : ",cacheTemps.get(i).getAlgorithmTemp()+"");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TempConnectorManager.close();
    }

}
