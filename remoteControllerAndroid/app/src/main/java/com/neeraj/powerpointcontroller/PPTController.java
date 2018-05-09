package com.neeraj.powerpointcontroller;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RemoteController;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class PPTController extends Activity implements Controller{
    BluetoothDevice device;
    ConnectThread connectThread;
    AcceptThread acceptThread;
    BluetoothAdapter mBluetoothAdapter;
    public static final byte KEY_PRESS = 0x3A;
    public static final byte KEY_RELEASE = 0x3B;
    public static final String UUID_CLIENT = "af83dc63-c212-49c4-99d5-c91782900315";
    private static final String EXTRA_SYNC = "sync";
    private static final String EXTRA_NOTE = "note";
    private static final String EXTRA_BITMAP = "bitmap";
    private static final String EXTRA_LEN = "length";
    private static final String EXTRA_CUR_SLIDE = "cur_slide";
    PresentationController presentationController;
    boolean sync = false;
    int currentSlide;
    Bitmap[] bitmaps;
    String[] notes;
    int numSlides;

    public static final String EXTRA_RADIO_ID = "radioid";

    private final View.OnTouchListener touchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            byte[] msg  = new byte[2];
            msg[1] = (byte) Integer.parseInt(v.getTag().toString());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                msg[0] = KEY_PRESS;
                write(msg);
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                msg[0] = KEY_RELEASE;
                write(msg);
                return true;
            }
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pptcontroller);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frag_container, (Fragment) new MouseController());
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.commit();
        RadioButton radioMouse = (RadioButton) findViewById(R.id.radio_mouse);
        radioMouse.setChecked(true);
        if(savedInstanceState==null){
            Intent intent = getIntent();
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }
        else {
            device = savedInstanceState.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            int id = savedInstanceState.getInt(EXTRA_RADIO_ID);
            selectRadio(id);
            sync = savedInstanceState.getBoolean(EXTRA_SYNC);
            if(sync){
                int len = savedInstanceState.getInt(EXTRA_LEN);
                notes = new String[len];
                bitmaps = new Bitmap[len];
                numSlides = len - 1;
                currentSlide = savedInstanceState.getInt(EXTRA_CUR_SLIDE);
                for(int i=0;i<len;i++){
                    notes[i] = savedInstanceState.getString(EXTRA_NOTE + i);
                    bitmaps[i] = savedInstanceState.getParcelable(EXTRA_BITMAP + i);
                }
            }
            replaceFragment(id);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectThread = new ConnectThread(device,this);
        connectThread.start();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private OutputStream mmOutStream;
        private final Activity activity;
        public volatile boolean interrupt = false;
        private final String CONNECTING = "Connecting" ;
        private final String CONNECTED = "Connected" ;
        private String state = CONNECTING;

        public ConnectThread(BluetoothDevice device,Activity act) {
            activity = act;
            connecting();
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(Device.getUUID(BluetoothDeviceFinder.PPT_UUID));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                connected();
                state = CONNECTED;
            } catch (IOException connectException) {}

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        synchronized public void cancel() {
            interrupt = true;
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

        void manageConnectedSocket(BluetoothSocket socket){
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpOut = socket.getOutputStream();
            } catch (Exception e) { }

            mmOutStream = tmpOut;
            boolean newConnection = false;
            while(interrupt==false){
                byte[] msg = new byte[1];
                msg[0] = 0;
                try{
                    mmOutStream.write(msg);
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){}
                    newConnection = false;
                    if(!state.equals(CONNECTED)) {
                        connected();
                        state = CONNECTED;
                    }
                }catch (Exception e){
                    newConnection = true;
                    if(!state.equals(CONNECTING)) {
                        connecting();
                        state = CONNECTING;
                    }
                }
                if(newConnection){
                    try{
                        mmSocket = device.createRfcommSocketToServiceRecord(Device.getUUID(BluetoothDeviceFinder.PPT_UUID));
                        mmSocket.connect();
                        mmOutStream = mmSocket.getOutputStream();
                    }
                    catch (Exception e){}
                }
            }
        }

        synchronized public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {}
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private Handler handler = new Handler();
        public static final String NAME = "remote client";
        public static final int NOTE = 0x01;
        public static final int NUM_SLIDES = 0x02;
        public static final int CURRENT_SLIDE_IMG = 0x03;
        public static final int SLIDE_IMG = 0x04;
        public static final int CURRENT_SLIDE = 0x05;
        public static final int SYNC_FINISH = 0x06;
        public static final int SYNC_ACK = 0x07;
        public static final int SYNC_FAIL = 0x08;

        public AcceptThread() {
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, Device.getUUID(UUID_CLIENT));
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    try {
                        Log.d("acceptor", "connection accepted");
                        mmServerSocket.close();
                        InputStream inp = socket.getInputStream();
                        int msgType;
                        int slideNum;
                        while((msgType = inp.read())>0) {
                            switch (msgType) {
                                case SLIDE_IMG:
                                    Log.d("acceptor","SLIDE_IMG");
                                    slideNum = Utility.readInt(inp, 2);
                                    byte[] imageBytes = Utility.readData(inp);
                                    bitmaps[slideNum] = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    /*if(slideNum==currentSlide && presentationController!=null){
                                        presentationController.setCurrentSlideImg(bitmaps[slideNum]);
                                    }*/
                                    if(presentationController != null){
                                        presentationController.addImage(bitmaps[slideNum]);
                                    }
                                    final int progress = (int) ((((float) slideNum)/numSlides)*100);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setProgressBarStatus(progress);
                                        }
                                    });
                                    break;
                                case NOTE:
                                    Log.d("acceptor","NOTE");
                                    slideNum = Utility.readInt(inp,2);
                                    byte[] noteArray = Utility.readData(inp);
                                    String note = new String(noteArray, Charset.forName("UTF-8"));
                                    notes[slideNum] = note;
                                    break;
                                case NUM_SLIDES:
                                    Log.d("acceptor","NUM_SLIDES");
                                    numSlides = Utility.readInt(inp,2);
                                    notes = new String[numSlides+1];
                                    bitmaps = new Bitmap[numSlides+1];
                                    sync = false;
                                    break;
                                case CURRENT_SLIDE:
                                    Log.d("acceptor","CURRENT_SLIDE");
                                    currentSlide = Utility.readInt(inp,2);
                                    if(sync){
                                        if(presentationController!=null){
                                            presentationController.displayNote(notes[currentSlide]);
                                            presentationController.setCurrentSlide(currentSlide);
                                        }
                                    }
                                    break;
                                case SYNC_FINISH:
                                    Log.d("acceptor","SYNC_FINISH");
                                    sync = true;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setProgressVisibility(false);
                                            presentationController.setCurrentSlide(currentSlide);
                                            presentationController.displayNote(notes[currentSlide]);
                                        }
                                    });
                                    break;
                                case SYNC_ACK:
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setProgressVisibility(true);
                                        }
                                    });
                                    break;
                                case SYNC_FAIL:
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setProgressVisibility(false);
                                        }
                                    });
                                    break;
                                default:
                                    Log.d("acceptor", "unrecognized msg type");
                            }
                        }
                        //String note = getNote(inpStream);
                    }catch (IOException e){}

                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int id = radioGroup.getCheckedRadioButtonId();
        savedInstanceState.putInt(EXTRA_RADIO_ID, id);
        savedInstanceState.putBoolean(EXTRA_SYNC,sync);
        if(sync){
            savedInstanceState.putInt(EXTRA_LEN,notes.length);
            savedInstanceState.putInt(EXTRA_CUR_SLIDE,currentSlide);
            for(int i=0;i<notes.length;i++){
                savedInstanceState.putString(EXTRA_NOTE + i,notes[i]);
                savedInstanceState.putParcelable(EXTRA_BITMAP + i,bitmaps[i]);
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        connectThread.cancel();
        acceptThread.cancel();
    }

    void connecting(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.connectCircle).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.connectText)).setText("Connecting");
            }
        });
    }
    void connected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.connectCircle).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.connectText)).setText("Connected");
            }
        });
    }

    @Override
    public View.OnTouchListener getTouchListener(){
        return touchListener;
    }

    @Override
    public void write(byte[] bytes){
        try {
            connectThread.write(bytes);
        }catch (Exception ex){}
    }

    public void radioClicked(View view){
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int id = radioGroup.getCheckedRadioButtonId();
        replaceFragment(id);
    }

    void replaceFragment(int id){
        Fragment newFrag = null;
        switch(id) {
            case R.id.radio_mouse:
                newFrag =  new MouseController();
                presentationController = null;
                break;
            case R.id.radio_presentation:
                newFrag = new PresentationController();
                presentationController = (PresentationController) newFrag;
                break;
            case R.id.radio_keyboard:
                newFrag = new KeyboardController();
                presentationController = null;
                break;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frag_container, newFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    void selectRadio(int id){
        if(id==R.id.radio_mouse){
            ((RadioButton) findViewById(R.id.radio_mouse)).setChecked(true);
        }
        else {
            ((RadioButton) findViewById(R.id.radio_mouse)).setChecked(false);
        }
        if(id==R.id.radio_presentation){
            ((RadioButton) findViewById(R.id.radio_presentation)).setChecked(true);
        }
        else{
            ((RadioButton) findViewById(R.id.radio_presentation)).setChecked(false);
        }
        if(id==R.id.radio_keyboard){
            ((RadioButton) findViewById(R.id.radio_keyboard)).setChecked(true);
        }
        else{
            ((RadioButton) findViewById(R.id.radio_keyboard)).setChecked(false);
        }
    }

    public void setProgressVisibility(boolean visibility){
        ProgressBar bar = (ProgressBar) findViewById(R.id.sync_bar);
        if(visibility){
            bar.setProgress(0);
            bar.setVisibility(View.VISIBLE);
        }
        else{
            bar.setVisibility(View.GONE);
        }
    }

    public void setProgressBarStatus(int progress){
        ProgressBar bar = (ProgressBar) findViewById(R.id.sync_bar);
        bar.setProgress(progress);
    }

    public Bitmap[] getBitmaps(){
        Bitmap[] bitmaps1 = new Bitmap[bitmaps.length - 1];
        for (int i = 1;i<bitmaps.length;i++){
            bitmaps1[i-1] = bitmaps[i];
        }
        return bitmaps1;
    }

    public String getCurrentNote(){
        return notes[currentSlide];
    }

    public boolean isSync(){
        return sync;
    }

    public int getCurrentSlide(){
        return currentSlide;
    }

    public int getNumSlides() { return numSlides; }
}
