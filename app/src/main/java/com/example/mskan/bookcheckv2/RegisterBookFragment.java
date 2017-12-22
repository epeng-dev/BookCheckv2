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

public class RegisterBookFragment extends Fragment {
	private RecyclerView recyclerView;
	private RecyclerView.Adapter adapter;
	private RecyclerView.LayoutManager layoutManager;
	private ArrayList<CardViewItem> items;
	public RequestManager requestManager;
	private String UserID, UserToken;

	public RegisterBookFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			UserID = getArguments().getString("UserID");
			UserToken = getArguments().getString("UserToken");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_return_book, container, false);
		requestManager = Glide.with(getActivity());
		recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
		recyclerView.setHasFixedSize(true);
		layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		String URL = "http://esplay.xyz:21221/";
		items = new ArrayList<CardViewItem>();
		items.add(new CardViewItem("바코드 책 등록", R.mipmap.ic_launcher, "http://52.79.134.200:3004/book", "register", BarcodeActivity.class));
		items.add(new CardViewItem("RFID 책 등록", R.mipmap.ic_launcher, "http://52.79.134.200:3004/book", "register", RFIDActivity.class));
		adapter = new CardViewAdapter(getContext(), items, requestManager, UserToken);
		recyclerView.setAdapter(adapter);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
