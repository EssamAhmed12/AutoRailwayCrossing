package com.example.railway;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class GateMan extends AppCompatActivity {
    final String ON = "0";
    final String OFF = "1";

    BluetoothSPP bluetooth;
    Button call, getLocation,connect,off,on,aboutUs,contactUs;
    TextView messageFromBlutooth;
    private static final String TAG = "GateMan";
    ActionBar actionBar;
    private FirebaseAuth mAuth;
    FusedLocationProviderClient client;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ((requestCode)) {
            case 1000:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    }
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_man);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#783887")));
        mAuth = FirebaseAuth.getInstance();

        bluetooth = new BluetoothSPP(this);

        connect = findViewById(R.id.connect);
        on = findViewById(R.id.on);
        off = findViewById(R.id.off);
        call = findViewById(R.id.call);
        getLocation = findViewById(R.id.getLocation);
        messageFromBlutooth = findViewById(R.id.message);
        aboutUs = findViewById(R.id.about_us);
        contactUs = findViewById(R.id.contact_us);
        bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                connect.setText(getResources().getText(R.string.connected_now) + " " + name);
            }

            public void onDeviceDisconnected() {
                connect.setText(R.string.connection_lost);
            }

            public void onDeviceConnectionFailed() {
                connect.setText(R.string.unable_connection);
            }

        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bluetooth.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.send(ON, true);
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.send(OFF, true);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });

        bluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.e(TAG, "Data" + data.toString() + " ,Message : " + message);
                if (message.equals("1")) {
                    messageFromBlutooth.setText(R.string.gate_close);
                    sendNotification();
                }
                if (message.equals("0")) {
                    messageFromBlutooth.setText(R.string.gate_open);
                }
            }
        });
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(GateMan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(GateMan.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        });
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutUsDialog();
            }
        });
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUsDialog();
            }
        });
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            buildLocationRequest();
            buildLocationCallback();
            client = LocationServices.getFusedLocationProviderClient(this);
            getLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(GateMan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(GateMan.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            });
        }
    }

    public void onStart() {
        super.onStart();
        if (!bluetooth.isBluetoothEnabled()) {
            bluetooth.enable();
        } else {
            if (!bluetooth.isServiceAvailable()) {
                bluetooth.setupService();
                bluetooth.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }


    public void onDestroy() {
        super.onDestroy();
        bluetooth.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_state, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ar:
                setLocale("ar");
                Toast.makeText(this, R.string.toast_arabic, Toast.LENGTH_LONG).show();
                return true;
            case R.id.en:
                setLocale("en");
                Toast.makeText(this, R.string.toast_english, Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_favorite:
                Intent intent = new Intent(this, Chat.class);
                this.startActivity(intent);
                return true;
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                Toast.makeText(getApplicationContext(), getString(R.string.sign_out_successfuly), Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(this, SignIn.class);
                startActivity(intent1);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetooth.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetooth.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , R.string.unable_connection
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, GateMan.class);
        startActivity(refresh);
        finish();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GateMan.this);
        builder.setTitle(getString(R.string.choose_number));

// add a list
        String[] num = {getString(R.string.fire_department), getString(R.string.ambulance_number)
                , getString(R.string.emergency_number)};
        builder.setItems(num, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent callIntent1 = new Intent(Intent.ACTION_CALL);
                        callIntent1.setData(Uri.parse("tel:180"));
                        startActivity(callIntent1);
                    case 1:
                        Intent callIntent2 = new Intent(Intent.ACTION_CALL);
                        callIntent2.setData(Uri.parse("tel:123"));
                        startActivity(callIntent2);
                    case 2:
                        Intent callIntent3 = new Intent(Intent.ACTION_CALL);
                        callIntent3.setData(Uri.parse("tel:122"));
                        startActivity(callIntent3);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", notificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(R.color.colorPrimary);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.railway1)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("GATE MAN")
                .setContentText("WARNING : THE TRAIN IS COMING")
                .setSound(uri, 5)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

    public void contactUsDialog() {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(GateMan.this);
        LayoutInflater factory = LayoutInflater.from(GateMan.this);
        final View view = factory.inflate(R.layout.contact_us, null);
        alertadd.setView(view);
        alertadd.setNeutralButton(R.string.contact_us_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();
    }

    public void aboutUsDialog(){
        AlertDialog.Builder alertadd = new AlertDialog.Builder(GateMan.this);
        LayoutInflater factory = LayoutInflater.from(GateMan.this);
        final View view = factory.inflate(R.layout.about_us, null);
        alertadd.setView(view);
        alertadd.setNeutralButton(R.string.about_us_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }

        });
        alertadd.show();
    }
        public void buildLocationCallback() {
            locationCallback=new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    for(Location location:locationResult.getLocations()){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude() + "?q=" + location.getLatitude() + "," + location.getLongitude()));
                 startActivity(intent);

                    }
                }

            };

        }

        private void buildLocationRequest() {
            locationRequest=new LocationRequest();
            locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setSmallestDisplacement(10);
            locationRequest.setNumUpdates(3000);
        }
}

