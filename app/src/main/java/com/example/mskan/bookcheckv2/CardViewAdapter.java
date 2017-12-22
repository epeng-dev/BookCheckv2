package com.example.mskan.bookcheckv2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

/**
 * Created by mskan on 2017-05-29.
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {
	final ArrayList<CardViewItem> Itemset;
	private RequestManager glide;
	private Context Listcontext;
	private String libraries;
	private String UserToken;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ImageView Icon;
		public TextView Title;
		public CardView Cardview;

		public ViewHolder(View itemView) {
			super(itemView);
			Icon = (ImageView) itemView.findViewById(R.id.IconImage);
			Title = (TextView) itemView.findViewById(R.id.name);
			Cardview = (CardView) itemView.findViewById(R.id.CardView);
		}
	}

	public CardViewAdapter(Context context, ArrayList<CardViewItem> itemset, RequestManager requestManager, String Token) {
		this.Listcontext = context;
		this.Itemset = itemset;
		this.glide = requestManager;
		this.UserToken = Token;
	}

	@Override
	public CardViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.Title.setText(Itemset.get(position).Title);
		holder.Cardview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Listcontext, Itemset.get(position).Nextclass);
				intent.putExtra("mode", Itemset.get(position).mode);
				intent.putExtra("UserToken", UserToken);
				intent.putExtra("URL", Itemset.get(position).URL);
				intent.putExtra("Title", Itemset.get(position).Title);
				Listcontext.startActivity(intent);
			}
		});
	}

	@Override
	public int getItemCount() {
		return Itemset.size();
	}
}
