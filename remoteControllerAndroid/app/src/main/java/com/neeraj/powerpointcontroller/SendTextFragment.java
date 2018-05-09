package com.neeraj.powerpointcontroller;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendTextFragment extends Fragment {

    public static final byte MSG_START = 0x0F;

    private Controller controller;

    private EditText editText;

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendClicked(v);
        }
    };

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d("beforeTextChanged", "start=" + start + " count=" + count + " after=" + after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("onTextChanged", "start=" + start + " before=" + before + " count=" + count);
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("afterTextChanged:",editText.getText().toString());
        }
    };

    public SendTextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_send_text, container, false);
        editText = (EditText) root.findViewById(R.id.msg_field);
        editText.addTextChangedListener(textWatcher);
        ((CustomEditText) editText).setController(controller);
        root.findViewById(R.id.send_button).setOnClickListener(clickListener);
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

    public void sendClicked(View view){
        byte[] encodedMsg;
        EditText msgField = (EditText) getView().findViewById(R.id.msg_field);
        String msgText = msgField.getText().toString();
        msgField.setText("");
        if(!msgText.equals("")){
            encodedMsg = msgText.getBytes(Charset.forName("UTF-8"));
            byte[] msg = new byte[3 + encodedMsg.length];
            msg[0] = MSG_START;
            Utility.getShort(msg,1,encodedMsg.length);
            for(int i=0;i<encodedMsg.length;i++){
                msg[i+3] = encodedMsg[i];
            }
            controller.write(msg);
        }
    }

}
