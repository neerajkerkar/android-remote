package com.neeraj.powerpointcontroller;


import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class KeyboardController extends Fragment {

    private Controller controller;

    public KeyboardController() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_keyboard_controller, container, false);
        ArrayList<View> buttons = new ArrayList<>();
        buttons.add(root.findViewById(R.id.key11));
        buttons.add(root.findViewById(R.id.key12));
        buttons.add(root.findViewById(R.id.key13));
        buttons.add(root.findViewById(R.id.key14));
        buttons.add(root.findViewById(R.id.key15));
        buttons.add(root.findViewById(R.id.key16));
        buttons.add(root.findViewById(R.id.key17));
        buttons.add(root.findViewById(R.id.key18));
        buttons.add(root.findViewById(R.id.key19));
        buttons.add(root.findViewById(R.id.key110));
        buttons.add(root.findViewById(R.id.key111));
        buttons.add(root.findViewById(R.id.key112));
        buttons.add(root.findViewById(R.id.key113));
        buttons.add(root.findViewById(R.id.key114));
        buttons.add(root.findViewById(R.id.key115));
        buttons.add(root.findViewById(R.id.key116));
        buttons.add(root.findViewById(R.id.key21));
        buttons.add(root.findViewById(R.id.key22));
        buttons.add(root.findViewById(R.id.key23));
        buttons.add(root.findViewById(R.id.key24));
        buttons.add(root.findViewById(R.id.key25));
        buttons.add(root.findViewById(R.id.key26));
        buttons.add(root.findViewById(R.id.key27));
        buttons.add(root.findViewById(R.id.key28));
        buttons.add(root.findViewById(R.id.key29));
        buttons.add(root.findViewById(R.id.key210));
        buttons.add(root.findViewById(R.id.key211));
        buttons.add(root.findViewById(R.id.key212));
        buttons.add(root.findViewById(R.id.key213));
        buttons.add(root.findViewById(R.id.key214));
        buttons.add(root.findViewById(R.id.key215));
        buttons.add(root.findViewById(R.id.key216));
        buttons.add(root.findViewById(R.id.key217));
        buttons.add(root.findViewById(R.id.key218));
        buttons.add(root.findViewById(R.id.key219));
        buttons.add(root.findViewById(R.id.key220));
        buttons.add(root.findViewById(R.id.key221));
        buttons.add(root.findViewById(R.id.key31));
        buttons.add(root.findViewById(R.id.key32));
        buttons.add(root.findViewById(R.id.key33));
        buttons.add(root.findViewById(R.id.key34));
        buttons.add(root.findViewById(R.id.key35));
        buttons.add(root.findViewById(R.id.key36));
        buttons.add(root.findViewById(R.id.key37));
        buttons.add(root.findViewById(R.id.key38));
        buttons.add(root.findViewById(R.id.key39));
        buttons.add(root.findViewById(R.id.key310));
        buttons.add(root.findViewById(R.id.key311));
        buttons.add(root.findViewById(R.id.key312));
        buttons.add(root.findViewById(R.id.key313));
        buttons.add(root.findViewById(R.id.key314));
        buttons.add(root.findViewById(R.id.key315));
        buttons.add(root.findViewById(R.id.key316));
        buttons.add(root.findViewById(R.id.key317));
        buttons.add(root.findViewById(R.id.key318));
        buttons.add(root.findViewById(R.id.key319));
        buttons.add(root.findViewById(R.id.key320));
        buttons.add(root.findViewById(R.id.key41));
        buttons.add(root.findViewById(R.id.key42));
        buttons.add(root.findViewById(R.id.key43));
        buttons.add(root.findViewById(R.id.key44));
        buttons.add(root.findViewById(R.id.key45));
        buttons.add(root.findViewById(R.id.key46));
        buttons.add(root.findViewById(R.id.key47));
        buttons.add(root.findViewById(R.id.key48));
        buttons.add(root.findViewById(R.id.key49));
        buttons.add(root.findViewById(R.id.key410));
        buttons.add(root.findViewById(R.id.key411));
        buttons.add(root.findViewById(R.id.key412));
        buttons.add(root.findViewById(R.id.key413));
        buttons.add(root.findViewById(R.id.key414));
        buttons.add(root.findViewById(R.id.key415));
        buttons.add(root.findViewById(R.id.key416));
        buttons.add(root.findViewById(R.id.key51));
        buttons.add(root.findViewById(R.id.key52));
        buttons.add(root.findViewById(R.id.key53));
        buttons.add(root.findViewById(R.id.key54));
        buttons.add(root.findViewById(R.id.key55));
        buttons.add(root.findViewById(R.id.key56));
        buttons.add(root.findViewById(R.id.key57));
        buttons.add(root.findViewById(R.id.key58));
        buttons.add(root.findViewById(R.id.key59));
        buttons.add(root.findViewById(R.id.key510));
        buttons.add(root.findViewById(R.id.key511));
        buttons.add(root.findViewById(R.id.key512));
        buttons.add(root.findViewById(R.id.key513));
        buttons.add(root.findViewById(R.id.key514));
        buttons.add(root.findViewById(R.id.key515));
        buttons.add(root.findViewById(R.id.key516));
        buttons.add(root.findViewById(R.id.key61));
        buttons.add(root.findViewById(R.id.key62));
        buttons.add(root.findViewById(R.id.key63));
        buttons.add(root.findViewById(R.id.key64));
        buttons.add(root.findViewById(R.id.key65));
        buttons.add(root.findViewById(R.id.key66));
        buttons.add(root.findViewById(R.id.key67));
        buttons.add(root.findViewById(R.id.key68));
        buttons.add(root.findViewById(R.id.key69));
        buttons.add(root.findViewById(R.id.key610));
        buttons.add(root.findViewById(R.id.key611));
        buttons.add(root.findViewById(R.id.key612));
        buttons.add(root.findViewById(R.id.key613));
        buttons.add(root.findViewById(R.id.plus));
        buttons.add(root.findViewById(R.id.num_enter));
        for(View v:buttons){
            v.setOnTouchListener(controller.getTouchListener());
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

}
