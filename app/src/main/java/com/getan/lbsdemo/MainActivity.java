package com.getan.lbsdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isFirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化要在setcontentview前调用
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mLocationClient = new LocationClient(getApplicationContext());
        positionText = (TextView)findViewById(R.id.position_text_view);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient.registerLocationListener((BDAbstractLocationListener) new MyLocationListener());
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()){
            /*这是一个string数组，数组的定义方式为 String [] s = new String[10];
            string数组的定义有三种：
            String arr[] = new String[10]; //创建一个长度为10的String 类型数组。
            String arr[] = {"张三","李四"};   数组:string[] s = { "a", "b", "c", "d", "e" };    String[] desc = new String[]{};
            String[] arr = new String[10];*/

         /*   String数组的常见遍历方法
            String[] str=new String[3];
            for (int i = 0; i < str.length; i++) {
                str[i]="i=;"+i;
            }
            方法一：for循环
            for (int i = 0; i < str.length; i++) {
                System.out.println(str[i]);
            }
           方法二:foreach
            for (String string : str) {
                System.out.println(string);
            }
            方法三：迭代器遍历
            List list= Arrays.asList(str);
            System.out.println(str);*/

            /*String[] desc = new String[list.size()];
            list.toArray(desc);
            output(desc);*/
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mMapView.onDestroy();
        mBaiduMap.setBuildingsEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result :grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
                default:

        }
    }

    private void navigateTo(BDLocation location){
        if (isFirstLocate){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData = builder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    private class MyLocationListener extends BDAbstractLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
         /*   StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS模式");
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络基站模式");
            }
            positionText.setText(currentPosition);*/

         if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType()
                 == BDLocation.TypeNetWorkLocation){
             navigateTo(bdLocation);
         }
        }
    }
}
