package com.neeraj.powerpointcontroller;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MouseController extends Fragment {

    private static byte COORD_DELIMITER = 0x07;
    private static byte COORD_START_DELIMITER = 0x0E;
    private static byte VK_LBUTTON = 0x01;
    private Controller controller;
    private long time;
    private static final long MAX_TAP_TIME = 180;
    private static final double MAX_TAP_DIST = 10;
    private int xCoord;
    private int yCoord;

    private final View.OnTouchListener touchPadListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            byte[] msg;
            int x,y;
            final int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:{
                    time = SystemClock.uptimeMillis();
                    xCoord = (int) event.getX();
                    yCoord = (int) event.getY();
                    msg = new byte[5];
                    msg[0] = COORD_START_DELIMITER;
                    Utility.getShort(msg, 1, xCoord);
                    Utility.getShort(msg, 3, yCoord);
                    controller.write(msg);
                    break;
                }
                case MotionEvent.ACTION_MOVE:{
                    x = (int) event.getX();
                    y = (int) event.getY();
                    if(x>=0 && y>=0) {
                        msg = new byte[5];
                        msg[0] = COORD_DELIMITER;
                        Utility.getShort(msg, 1, x);
                        Utility.getShort(msg, 3, y);
                        controller.write(msg);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    long newTime = SystemClock.uptimeMillis();
                    if((newTime - time)<MAX_TAP_TIME && getDist(xCoord,yCoord,(int) event.getX(),(int) event.getY())<MAX_TAP_DIST){
                        msg = new byte[4];
                        msg[0] = PPTController.KEY_PRESS;
                        msg[1] = VK_LBUTTON;
                        msg[2] = PPTController.KEY_RELEASE;
                        msg[3] = VK_LBUTTON;
                        controller.write(msg);
                    }
                }
            }
            return true;
        }
    };

    double getDist(int x,int y,int newx,int newy){
        return Math.sqrt((newx-x)*(newx-x)+(newy-y)*(newy-y));
    }

    public MouseController() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_mouse_controller, container, false);
        ArrayList<View> buttons = new ArrayList<>();
        buttons.add(root.findViewById(R.id.leftMouse));
        buttons.add(root.findViewById(R.id.rightMouse));
        for(View v:buttons){
            v.setOnTouchListener(controller.getTouchListener());
        }
        root.findViewById(R.id.touchPad).setOnTouchListener(touchPadListener);
        return root;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        controller = (Controller) activity;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        controller = (Controller) context;
    }

}
