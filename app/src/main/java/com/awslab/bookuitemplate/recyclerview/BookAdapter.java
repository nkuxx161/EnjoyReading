package com.awslab.bookuitemplate.recyclerview;

import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.awslab.bookuitemplate.R;
import com.awslab.bookuitemplate.model.Book;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.bookviewholder> {

    List<Book> mdata;
    BookCallback callback;

    public BookAdapter(List<Book> mdata , BookCallback callback) {
        this.mdata = mdata;
        this.callback = callback ;
    }


    @NonNull
    @Override
    public bookviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book,parent,false);

        return new bookviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bookviewholder holder, int position) {

        // bind book item data here
        // I'm just going to bint the book image..

        //设置封面 using Glide
        Glide.with(holder.itemView.getContext())
                .load(mdata.get(position).getImgUrl()) // set the img book Url
                .transforms(new CenterCrop(),new RoundedCorners(16)) // i know it's deprecated i will fix it later
                .into(holder.imgBook); // destination path
        Log.d("Glide加载网络图片", "yes");

        holder.ratingBar.setRating(mdata.get(position).getRating()); //设置评分星星
        holder.rate.setText(mdata.get(position).getRating() + "");//设置评分数字
        holder.title.setText(mdata.get(position).getTitle()); //设置标题
        holder.author.setText(mdata.get(position).getAuthor()); //设置作者
        int pages = mdata.get(position).getPages();
        if (pages != 0) {
            holder.pages.setText(pages +" Pages");
        } else {
            holder.pages.setText("More info");
        }


    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    public class bookviewholder extends RecyclerView.ViewHolder {


        ImageView imgBook,imgFav,imgContainer;
        TextView title,author,pages,rate;
        RatingBar ratingBar;

        public bookviewholder(@NonNull View itemView) {
            super(itemView);


            imgBook = itemView.findViewById(R.id.item_book_img);
            imgContainer = itemView.findViewById(R.id.container);
            title = itemView.findViewById(R.id.item_book_title);
            author = itemView.findViewById(R.id.item_book_author);
            pages = itemView.findViewById(R.id.item_book_pagesrev);
            rate = itemView.findViewById(R.id.item_book_score);
            ratingBar = itemView.findViewById(R.id.item_book_ratingbar);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onBookItemClick(getAdapterPosition(),
                            imgContainer,
                            imgBook,
                            title,
                            author,
                            pages,
                            rate,
                            ratingBar);
                }
            });


        }
    }
}
