package com.neeraj.powerpointcontroller;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.Key;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PresentationController extends Fragment {

    private static final byte NEXT = 0x11;
    private static final byte PREV = 0x10;
    private static final byte SLIDE_SHOW = 0x12;
    private static final byte GO_TO_SLIDE = 0x13;
    private static final byte GET_NOTE = 0x14;
    private static final byte SYNCHRONIZE = 0x15;

    private Controller controller;
    private View root;
    ArrayList<ImageView> images = new ArrayList<>();
    GradientDrawable gd;

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            byte[] msg;
            switch (id){
                case R.id.prev:
                    Log.d("onClick","prev");
                    msg = new byte[1];
                    msg[0] = PREV;
                    controller.write(msg);
                    break;
                case R.id.next:
                    Log.d("onClick", "next");
                    msg = new byte[1];
                    msg[0] = NEXT;
                    controller.write(msg);
                    break;
                case R.id.slideshow:
                    msg = new byte[1];
                    msg[0] = SLIDE_SHOW;
                    controller.write(msg);
                    break;
                case R.id.exit_slideshow:
                    msg = new byte[4];
                    msg[0] = PPTController.KEY_PRESS;
                    msg[1] = Keys.ESC;
                    msg[2] = PPTController.KEY_RELEASE;
                    msg[3] = Keys.ESC;
                    controller.write(msg);
                    break;
                case R.id.go_to_slide:
                    goToSlide();
                    break;
                case R.id.sync:
                    images = new ArrayList<>();
                    displayNote("No notes");
                    ((LinearLayout) root.findViewById(R.id.image_list)).removeAllViews();
                    ((TextView) root.findViewById(R.id.slideNumberText)).setText("");
                    msg = new byte[1];
                    msg[0] = SYNCHRONIZE;
                    controller.write(msg);
                    break;
            }
        }
    };


    public PresentationController() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_presentation_controller, container, false);
        ArrayList<View> buttons = new ArrayList<>();
        buttons.add(root.findViewById(R.id.prev));
        buttons.add(root.findViewById(R.id.next));
        buttons.add(root.findViewById(R.id.slideshow));
        buttons.add(root.findViewById(R.id.exit_slideshow));
        buttons.add(root.findViewById(R.id.go_to_slide));
        buttons.add(root.findViewById(R.id.sync));
        for(View v:buttons){
            v.setOnClickListener(clickListener);
        }
        gd = new GradientDrawable();
        gd.setColor(0xFF000000); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(5);
        gd.setStroke(5, 0xFF000000);
        PPTController pptc = (PPTController) getActivity();
        if(pptc.isSync()){
            for(Bitmap bitmap:pptc.getBitmaps()){
                addImage(bitmap);
            }
            displayNote(pptc.getCurrentNote());
            setCurrentSlide(pptc.getCurrentSlide());
        }
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

    public void displayNote(final String note){
        Activity activity = (Activity) controller;
        if(controller!=null){
            ((Activity) controller).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView noteView = (TextView) root.findViewById(R.id.note);
                    noteView.setText(note);
                }
            });
        }

    }


    public void setCurrentSlide(final int slideNum){
        final ImageView curSlide = images.get(slideNum - 1);
        final HorizontalScrollView hsl = (HorizontalScrollView) root.findViewById(R.id.hsl);
        ((Activity) controller).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView slideNumText = (TextView) root.findViewById(R.id.slideNumberText);
                PPTController pptc = (PPTController) getActivity();
                slideNumText.setText("Slide: " + slideNum + "/" + pptc.getNumSlides());
            }
        });
        hsl.post(new Runnable() {
            @Override
            public void run() {
                hsl.smoothScrollTo(curSlide.getLeft(),0);
            }
        });
    }

    public void goToSlide(){
        int index;
        byte[] msg = new byte[3];
        msg[0] = GO_TO_SLIDE;
        EditText slideNumText = (EditText) getView().findViewById(R.id.slide_num);
        try {
            index = Integer.valueOf(slideNumText.getText().toString());
            Utility.getShort(msg,1,index);
            controller.write(msg);
        }
        catch (Exception e) {}
    }

    public void goToSlide(int slideNum){
        int index;
        byte[] msg = new byte[3];
        msg[0] = GO_TO_SLIDE;
        EditText slideNumText = (EditText) getView().findViewById(R.id.slide_num);
        try {
            index = slideNum;
            Utility.getShort(msg,1,index);
            controller.write(msg);
        }
        catch (Exception e) {}
    }

    public void addImage(Bitmap bitmap){
        final LinearLayout imageList = (LinearLayout) root.findViewById(R.id.image_list);
        final ImageView imageView = new ImageView(getActivity());
        //imageView.setLayoutParams(new ViewGroup.LayoutParams(70, 70));
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(5,2,0,0);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setBackgroundDrawable(gd);
        } else {
            imageView.setBackground(gd);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSlide(images.indexOf(view) + 1);
            }
        });
        images.add(imageView);
        ((Activity) controller).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageList.addView(imageView);
            }
        });

    }

}
