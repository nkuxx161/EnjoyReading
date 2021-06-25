package com.awslab.bookuitemplate;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.awslab.bookuitemplate.model.Book;
import com.awslab.bookuitemplate.recyclerview.BookAdapter;
import com.awslab.bookuitemplate.recyclerview.BookCallback;
import com.awslab.bookuitemplate.recyclerview.CustomItemAnimator;

import java.util.ArrayList;
import java.util.List;

// make sure to import this exact same class
import androidx.core.util.Pair;


public class AdminFavlistActivity extends AppCompatActivity implements BookCallback {
    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private List<Book> mdata;
    private Button goMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_fav_list);
        initViews();

        initmdataBooks();

        setupBookAdapter();

    }

    private void setupBookAdapter() {

        bookAdapter = new BookAdapter(mdata, this);
        rvBooks.setAdapter(bookAdapter);

    }

    private void initmdataBooks() {

        // for testing purpos I'm creating a random set of books
        // you may get your data from web service or firebase database.

        mdata = new ArrayList<>();
        mdata.add(new Book("http://books.google.com/books/content?id=e1NgBK4k2cwC&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api"));



    }

    private void initViews() {
        goMain = findViewById(R.id.btn_return);
        rvBooks = findViewById(R.id.rv_book);

        goMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminFavlistActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        rvBooks.setHasFixedSize(true);

        // we need first to setupe the custom item animator that we just create
        rvBooks.setItemAnimator(new CustomItemAnimator());

        // to test the animation we need to simulate the add book operation
    }

    @Override
    public void onBookItemClick(int pos,
                                ImageView imgContainer,
                                ImageView imgBook,
                                TextView title,
                                TextView authorName,
                                TextView nbpages,
                                TextView score,
                                RatingBar ratingBar) {


        // create intent and send book object to Details activity

        Intent intent = new Intent(this,BookDetailsActivity.class);
        intent.putExtra("bookObject",mdata.get(pos));

        // shared Animation setup
        // let's import the Pair class
        Pair<View,String> p1 = Pair.create((View)imgContainer,"containerTN"); // second arg is the tansition string Name
        Pair<View,String> p2 = Pair.create((View)imgBook,"bookTN"); // second arg is the tansition string Name
        Pair<View,String> p3 = Pair.create((View)title,"booktitleTN"); // second arg is the tansition string Name
        Pair<View,String> p4 = Pair.create((View)authorName,"authorTN"); // second arg is the tansition string Name
        Pair<View,String> p5 = Pair.create((View)nbpages,"bookpagesTN"); // second arg is the tansition string Name
        Pair<View,String> p6 = Pair.create((View)score,"scoreTN"); // second arg is the tansition string Name
        Pair<View,String> p7 = Pair.create((View)ratingBar,"rateTN"); // second arg is the tansition string Name


        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1,p2,p3,p4,p5,p6,p7);


        // start the activity with scene transition

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent,optionsCompat.toBundle());
        }
        else
            startActivity(intent);


    }

    public void goMainActivity(View view) {

    }

}
