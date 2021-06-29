package com.awslab.bookuitemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awslab.bookuitemplate.NetworkUtils.BookLoader;
import com.awslab.bookuitemplate.NetworkUtils.NetworkRequest;
import com.awslab.bookuitemplate.model.Book;
import com.awslab.bookuitemplate.recyclerview.BookAdapter;
import com.awslab.bookuitemplate.recyclerview.BookCallback;
import com.awslab.bookuitemplate.recyclerview.CustomItemAnimator;

import java.util.ArrayList;
import java.util.List;

// make sure to import this exact same class
import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;


public class AdminFavlistActivity extends AppCompatActivity implements BookCallback, LoaderManager.LoaderCallbacks<String> {
    public static final String LOG_TAG = "AdminFavlistActivity";

    private String phone;
    private String serverUrl = "https://hello-cloudbase-3gcqukj9c27892d6-1305327820.ap-shanghai.service.tcloudbase.com/api/v1.0/book-collection"; //服务器收藏集合地址
    public JSONObject requestBody; //消息体
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

//        setupBookAdapter();

    }

    private void setupBookAdapter() {

        bookAdapter = new BookAdapter(mdata, this);
        rvBooks.setAdapter(bookAdapter);

    }

    private void initmdataBooks() {
        //采用异步加载，从数据库得到收藏的书籍信息

        //检查网络状态
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        //如果有网络连接执行异步任务
        if (networkInfo != null) {
            LoaderManager.getInstance(this).restartLoader(0, null, this);
            goMain.setText("数据加载中...");
        }

    }

    //完成页面的一些静态设置
    private void initViews() {
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        phone = preferences.getString("userPhone",null);
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

        if (LoaderManager.getInstance(this).getLoader(0) != null) {
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }
    }

    //点击卡片进入书籍详情信息界面
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

    //异步任务的加载处理
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        JSONObject query = new JSONObject();
        try {
            requestBody = new JSONObject();
            query.put("phone", phone);
            requestBody.put("query", query);
            Log.d(LOG_TAG, String.valueOf(requestBody));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "构建请求体失败");
        }
        return new BookLoader(this, "POST", serverUrl+"/find", requestBody);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        mdata = new ArrayList<>();;
        goMain.setText("返回主页");
        try {
            Log.d(LOG_TAG+"异步加载的数据", data);
        } catch (Exception e) {
            Log.d(LOG_TAG+"异步加载的数据", "获取的收藏信息为空");
        }
        //对返回的json数据解析，构建book对象
        try {
            String id = "", title = "", description = "", author = "", imgUrl = "";
            int pages = 0;
            float rating = 0;
            JSONObject jsonData = new JSONObject(data);
            JSONArray bookArray = jsonData.getJSONArray("data");
            if (bookArray.length() == 0) {
                Toast.makeText(this, "您还没有收藏，快去主页看看吧~", Toast.LENGTH_SHORT).show();
            }
            for (int i = 0; i < bookArray.length(); i++) {
                JSONObject book = bookArray.getJSONObject(i);
                title = book.getString("bookName");
                description = book.getString("description");
                author = book.getString("bookAuthor");
                imgUrl = book.getString("bookImg");
                pages = parseInt(book.getString("bookPages"));;//暂时获取不到
                id = book.getString("bookId");
                rating = parseFloat(book.getString("bookReview"));
                mdata.add(new Book(title, description, author, imgUrl, pages, id, rating, "yes"));
            }
            setupBookAdapter();
        }  catch (JSONException e) {
            Toast.makeText(this, "获取个人收藏图书失败", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
