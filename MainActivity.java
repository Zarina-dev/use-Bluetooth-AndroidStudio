//package com.example.connectraspi;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}


package com.example.connectraspi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;




/* 노트
* 프로젝트 만들기 참조: https://www.youtube.com/watch?v=iFtjox9_zAI&list=LL&index=1&t=32s
*
* checkSelfPermission: 실행중 사용자에게 권한허용받기
*      종류(https://ddangeun.tistory.com/158):
*           BLUETOOTH_SCAN : 주변 블루투스 기기를 검색하는 경우
*           BLUETOOTH_ADVERTISE : 현재 기기를 다른 블루투스 기기에서 검색할 수 있도록 하는 경우
*           BLUETOOT_CONNECT : 이미 피어링된 기기와 통신해야 할 경우
*/

public class MainActivity extends AppCompatActivity {

    CheckBox enable_bt, visible_bt;
    ImageView search_bt;
    TextView name_bt;
    ListView listView;

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;



    @SuppressWarnings({"deprecation", "unused"})    // startActivityForResult depricated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enable_bt = findViewById(R.id.enable_bt);
        visible_bt = findViewById(R.id.visible_bt);
        search_bt = findViewById(R.id.search_bt);
        name_bt = findViewById(R.id.name_bt);
        listView = findViewById(R.id.list_view);

        name_bt.setText(getLocalBluetoothName());


        BA = BluetoothAdapter.getDefaultAdapter();      /* local device Bluetooth adapter */
        if (BA == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (BA.isEnabled()) {                           /* local device Bluetooth adapter 사용 가능*/
            enable_bt.setChecked(true);
        }


        /* local device의 Bluetooth 기능 enable 여부 */
        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {  // b == isChecked
                if (!b) {

                    // BLUETOOTH_ADVERTISE permission 있는지 확인, 없을 시 사용자로부터터 permission 요청

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        // 참조: https://codinghero.tistory.com/111
                        // BLUETOOTH_ADVERTISE permission 없음
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                        builder.setMessage("애플리케이션이 블루투스를 연결(Connect) 할 수 있도록 위치 정보 액세스 권한을 부여하십시오");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[] {android.Manifest.permission.BLUETOOTH_CONNECT}, 3);
                            }
                        });
                        builder.show();
                        // 허용 받았음
                    }
                    BA.disable();
                    Toast.makeText(MainActivity.this, "Turned off", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn, 0);
                    Toast.makeText(MainActivity.this, "Turned on", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /* local device 를 다른 기기들이 검색할 수 있게끔 허용 : visible for 2 minitues*/
        visible_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

                    // BLUETOOTH_ADVERTISE permission 있는지 확인, 없을 시 사용자로부터터 permission 요청
                   if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        // BLUETOOTH_ADVERTISE permission 없음
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                        builder.setMessage("애플리케이션이 블루투스를 다른 기기에 알리도록 (Advertise) 위치 정보 액세스 권한을 부여하십시오");
                        builder.setPositiveButton(android.R.string.ok, null);               // OK 누르면 허용
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[] {android.Manifest.permission.BLUETOOTH_ADVERTISE}, 3);        // 사용자로부터 허용 요청
                            }
                        });
                        builder.show();
                       // advertise 허용 받았음

                    }else {
                        // BLUETOOTH_ADVERTISE permission 있음
                        Toast.makeText(MainActivity.this, "BLUETOOTH_ADVERTISE permission 확인!", Toast.LENGTH_SHORT).show();
                    }

                    startActivityForResult(getVisible, 0);
                    Toast.makeText(MainActivity.this, "Visible for 2 min", Toast.LENGTH_SHORT).show();
                }
            }
        });



        /* pairing된 다른 디바이스들의 목록을 표시*/
        search_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                list();
            }
        });
    }


    private void list() {
        // BLUETOOTH_ADVERTISE permission 있는지 확인, 없을 시 사용자로부터터 permission 요청
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // BLUETOOTH_ADVERTISE permission 없음
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("블루투스에 대한 액세스가 필요합니다");
            builder.setMessage("애플리케이션이 블루투스를 연결(Connect) 할 수 있도록 위치 정보 액세스 권한을 부여하십시오");
            builder.setPositiveButton(android.R.string.ok, null);               // OK 누르면 허용
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    requestPermissions(new String[] {android.Manifest.permission.BLUETOOTH_CONNECT}, 3);
                }
            });
            builder.show();
            // 요청 받았음
        }else{
            // BLUETOOTH_ADVERTISE permission 있음
            Toast.makeText(MainActivity.this, "BLUETOOTH_ADVERTISE permission 확인!", Toast.LENGTH_SHORT).show();
        }
        pairedDevices = BA.getBondedDevices();     // get bonded(paired) device

          // paired된 블루투스 이름 리스트를 listView 목록록에 추가
       ArrayList list = new ArrayList();
        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
        }
        Toast.makeText(this, "Showing Devices", Toast.LENGTH_SHORT).show();             // Showing paired Devices
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list);
        listView.setAdapter(adapter);
    }

    public String getLocalBluetoothName() {     // 현재 기기의 bluetooth 이름
        if (BA == null) {
            BA = BluetoothAdapter.getDefaultAdapter();
        }


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // BLUETOOTH_CONNECT permission 없음
            // 사용자로부터 허용 요청
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("블루투스에 대한 액세스가 필요합니다");
            builder.setMessage("애플리케이션이 블루투스를 연결(Connect) 할 수 있도록 위치 정보 액세스 권한을 부여하십시오");
            builder.setPositiveButton(android.R.string.ok, null);               // OK 누르면 허용
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    requestPermissions(new String[] {android.Manifest.permission.BLUETOOTH_CONNECT}, 3);
                }
            });
            builder.show();
        }else{
            // BLUETOOTH_CONNECT permission 있음
            Toast.makeText(MainActivity.this, "BLUETOOTH_CONNECT permission 확인!", Toast.LENGTH_SHORT).show();
        }


        String baName = BA.getName(); // sdk_gphone64_x86_64
        //Toast.makeText(MainActivity.this, "BA.getName = "+baName, Toast.LENGTH_SHORT).show();    // baName이 null이 아님
        if(baName == null){
            baName = BA.getAddress();
            Toast.makeText(MainActivity.this, "BA.getAddress = "+baName, Toast.LENGTH_SHORT).show();
        }

        return baName;
    }


}
