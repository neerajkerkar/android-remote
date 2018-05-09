package com.neeraj.powerpointcontroller;

import android.view.View;

/**
 * Created by neeraj on 28-08-2016.
 */
public interface Controller {
    void write(byte[] bytes);
    View.OnTouchListener getTouchListener();
}
