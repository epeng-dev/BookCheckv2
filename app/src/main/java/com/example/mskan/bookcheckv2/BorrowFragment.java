package com.example.mskan.bookcheckv2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;


public class BorrowFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<CardViewItem> items;
    public RequestManager requestManager;
    private String UserToken;
    public BorrowFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            UserToken = getArguments().getString("UserToken");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrow, container, false);
        requestManager = Glide.with(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        items = new ArrayList<CardViewItem>();
        items.add(new CardViewItem("바코드 대출", R.mipmap.ic_launcher, "http://52.79.134.200:3004/borrow", "borrow", BarcodeActivity.class));
        items.add(new CardViewItem("RFID 대출", R.mipmap.ic_launcher, "http://52.79.134.200:3004/borrow", "borrow", RFIDActivity.class));
        adapter = new CardViewAdapter(getContext(), items, requestManager, UserToken);
		recyclerView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
