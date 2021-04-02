package com.pstiwari.android;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pstiwari.android.calls.Global;
import com.pstiwari.android.calls.calls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private View PrivateCallView;
    ArrayList<calls> callLogList;
    RecyclerView rv_callLog;
    LogcallAdaptor callLog_adapter;
    String uid;
    DatabaseReference mlogs;

    public CallsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateCallView= inflater.inflate(R.layout.fragment_calls, container, false);
        mlogs = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        callLogList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        rv_callLog=PrivateCallView.findViewById( R.id.recycler_view112 );
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(false);
        manager.setStackFromEnd(false);
        rv_callLog.setLayoutManager(manager);
        return PrivateCallView;
    }
    public int halbine(ArrayList<calls> ml, String id) {
        int j = 0, i = 0;

        try {
            for (i = 0; i < ml.size(); i++) {
                if (ml.get(i).getId().equals(id)) {
                    j = 1;
                    break;
                }
            }
            if (j == 1)
                return i;
            else
                return -1;
        }
        catch (NullPointerException e)
        {
            return -1;

        }

    }

    private void arrange() {

        calls temp;
        for (int i = 0; i < callLogList.size(); i++) {
            if (i != callLogList.size() - 1) {
                if (callLogList.get(i).getTime() < callLogList.get(i + 1).getTime()) {
                    temp = callLogList.get(i);
                    callLogList.set(i, callLogList.get(i + 1));
                    callLogList.set(i + 1, temp);
                    arrange();
                    break;
                }
            }

        }

        callLog_adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        uid = mAuth.getCurrentUser().getUid();
        try {
            mlogs.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (mAuth.getCurrentUser() != null) {

                        if (!dataSnapshot.exists()) {
                            callLogList.clear();
                            try {
                                rv_callLog.setVisibility(View.GONE);
                                callLog_adapter.notifyItemRangeRemoved(0, callLogList.size());
                                callLog_adapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        } else {

                            for (DataSnapshot data1 : dataSnapshot.getChildren()) {
                                mlogs.child(mAuth.getCurrentUser().getUid()).child(data1.getKey())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot data2 : dataSnapshot.getChildren()) {
                                                    calls call2 = data2.getValue(calls.class);
                                                    System.out.println("dddddd"+call2.getName()+ "  "+call2.getDur());
                                                    if (halbine(callLogList, call2.getId()) == -1) {

                                                        callLogList.add(call2);
                                                        arrange();
                                                        callLog_adapter.notifyDataSetChanged();

                                                    } else {
                                                        callLogList.set(halbine(callLogList, call2.getId()), call2);
                                                        arrange();
                                                        callLog_adapter.notifyDataSetChanged();

                                                    }


                                                }
                                                try {

                                                    if (callLogList.size() == 0) {
                                                        rv_callLog.setVisibility(View.GONE);
                                                    } else {
                                                        rv_callLog.setVisibility(View.VISIBLE);

                                                    }
                                                } catch (NullPointerException e) {

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                            }

                            rv_callLog.setVisibility(View.VISIBLE);
                            callLog_adapter = new LogcallAdaptor(getActivity(),callLogList);
                            rv_callLog.setAdapter(callLog_adapter);
                            callLog_adapter.notifyDataSetChanged();

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
