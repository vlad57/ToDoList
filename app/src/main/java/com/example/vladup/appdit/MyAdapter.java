package com.example.vladup.appdit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    public List<Model> mDataset;
    public Boolean isChekable;
    private static ClickListener clickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView mTextTitle;
        public TextView mTextContent;
        public TextView mTextDate;
        public CheckBox mCheckboxFavoris;
        public int maPosition;
        public String IdDBElement;
        public RelativeLayout itemLayout;
        public String colorBackground;
        View root;


        public MyViewHolder(View v) {
            super(v);
            root = v;
            //isChekable = true;
            mTextTitle = (TextView)itemView.findViewById(R.id.titleItem);
            mTextContent = (TextView)itemView.findViewById(R.id.contentItem);
            mTextDate = (TextView)itemView.findViewById(R.id.dateItem);
            mCheckboxFavoris = (CheckBox)itemView.findViewById(R.id.favorisItem);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.itemLayout);

//            buttonDelete = (Button) v.findViewById(R.id.delete);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 1, 10, "Delete");
        }

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        MyAdapter.clickListener = clickListener;
    }

    public MyAdapter(Context context, List<Model> myDataset) {
        this.mDataset = myDataset;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.mTextTitle.setText(mDataset.get(position).getTitle());
        holder.mTextContent.setText(mDataset.get(position).getContent());
        holder.mTextDate.setText(mDataset.get(position).getDate());
        holder.itemLayout.setBackgroundColor(Color.parseColor(mDataset.get(position).getColor()));
        holder.colorBackground = mDataset.get(position).getColor();
        holder.mTextDate.setText(mDataset.get(position).getDate());
        holder.IdDBElement = mDataset.get(position).getId();
        holder.maPosition = mDataset.get(position).getPosition();
        //mDataset.get(position).setPosition(position);
//        holder.buttonDelete = (Button) holder.root.findViewById(R.id.delete);


        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onClick(holder, position);
            }
        });


            holder.mCheckboxFavoris.setOnCheckedChangeListener(null);
            holder.mCheckboxFavoris.setChecked(mDataset.get(position).isSelected());
            holder.mCheckboxFavoris.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mDataset.get(holder.getAdapterPosition()).setSelected(isChecked);
                    MyApolloClient.getMyApolloClient().mutate(
                            UpdatePostTMutation.builder()
                                    .id(mDataset.get(holder.getAdapterPosition()).getId())
                                    .position(mDataset.get(position).getPosition())
                                    .favoris(isChecked).build())
                            .enqueue(new ApolloCall.Callback<UpdatePostTMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<UpdatePostTMutation.Data> response) {
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {
                                }
                            });
                }
            });

  /*      holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.clickDelete(position, holder);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public String getIDitemRemove(int position){
        return mDataset.get(position).getId();
    }

    public void remove (int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void update(int position, Model List){
        mDataset.set(position, List);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    public void updateList(List<Model> newList){
        mDataset = newList;
        notifyDataSetChanged();
    }

    public void add(int position, Model List){
        mDataset.add(position, List);
        notifyItemInserted(position);
    }

    public interface ClickListener {
        void onClick(MyViewHolder holder, int position);
//        void clickDelete(int position, MyViewHolder holder);
    }

}

