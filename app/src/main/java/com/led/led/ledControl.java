package com.led.led;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;


public class ledControl extends AppCompatActivity {

    Button btnRight, btnLeft, btnForward, btnBackward, btnStop, btnDis;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgets
        btnRight = findViewById(R.id.button2);
        btnLeft = findViewById(R.id.button3);
        btnForward = findViewById(R.id.button5);
        btnBackward = findViewById(R.id.button6);
        btnStop = findViewById(R.id.button7);
        btnDis = findViewById(R.id.button4);
        brightness = findViewById(R.id.seekBar);
        lumn = findViewById(R.id.lumn);
        brightness.setProgress(255);

        new ConnectBT().execute(); //Call the class to connect


//        commands to be sent to bluetooth
        btnForward.setOnClickListener(v -> {
            turnForward();      //method to turn on

        });
//
//        btnBackward.setOnTouchListener((v, event) -> {
//            switch(event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    // PRESSED
//                    turnBackward();
//                    return true; // if you want to handle the touch event
//                case MotionEvent.ACTION_UP:
//                    // RELEASED
//                    turnStop();
//                    return true; // if you want to handle the touch event
//            }
//            return false;
//        });  btnForward.setOnTouchListener((v, event) -> {
//            switch(event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    // PRESSED
//                    turnForward();
//                    return true; // if you want to handle the touch event
//                case MotionEvent.ACTION_UP:
//                    // RELEASED
//                    turnStop();
//                    return true; // if you want to handle the touch event
//            }
//            return false;
//        });
//        btnLeft.setOnTouchListener((v, event) -> {
//            switch(event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    // PRESSED
//                    turnLeft();
//                    return true; // if you want to handle the touch event
//                case MotionEvent.ACTION_UP:
//                    // RELEASED
//                    turnStop();
//                    return true; // if you want to handle the touch event
//            }
//            return false;
//        });
//        btnRight.setOnTouchListener((v, event) -> {
//            switch(event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    // PRESSED
//                    turnRight();
//                    return true; // if you want to handle the touch event
//                case MotionEvent.ACTION_UP:
//                    // RELEASED
//                    turnStop();
//                    return true; // if you want to handle the touch event
//            }
//            return false;
//        });
        btnBackward.setOnClickListener(v -> {
            turnBackward();      //method to turn on
        });

        btnRight.setOnClickListener(v -> {
            turnRight();      //method to turn on
        });

        btnLeft.setOnClickListener(v -> {
            turnLeft();   //method to turn off
        });

        btnStop.setOnClickListener(v -> {
            turnStop();      //method to turn on
        });

        btnDis.setOnClickListener(v -> {
            Disconnect(); //close connection
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lumn.setText(String.valueOf(progress));
                    try {
                        btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    } catch (IOException e) {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void Disconnect() {
        AlertDialog.Builder AlertDialog = new AlertDialog.Builder(this);
        AlertDialog.setTitle("Disconnect?");
        AlertDialog.setMessage("Are you sure you want to disconnect from SPP:" + "\n" + address);
        AlertDialog.setPositiveButton("Disconnect", (dialog, which) -> {
            if (btSocket != null) //If the btSocket is busy
            {
                try {
                    msg("Disconnected Successfully");
                    turnStop();
                    btSocket.close(); //close connection
                } catch (IOException e) {
                    msg("Error");
                }
            }
            finish();
        });

        AlertDialog.setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(getApplicationContext(), "Continue playing", Toast.LENGTH_SHORT).show());

        AlertDialog.show();
    }

    private void turnLeft() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write('L');
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnRight() {
        if (btSocket != null) {
            try {

                btSocket.getOutputStream().write('R');
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnForward() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write('F');
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnBackward() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write('B');
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnStop() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write('S');
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait while God fixes my mistakes");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Be sure the device is turned on and that it is a Serial Port Profile (SPP)");
                finish();
            } else {
                msg("Connected! God does Miracles!");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}