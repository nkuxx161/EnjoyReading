package com.awslab.bookuitemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awslab.bookuitemplate.NetworkUtils.BookLoader;
import com.awslab.bookuitemplate.model.Book;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.awslab.bookuitemplate.AdminFavlistActivity.LOG_TAG;
import static com.awslab.bookuitemplate.R.drawable.ic_favorite_black_24dp;
import static com.awslab.bookuitemplate.R.drawable.ic_favorite_red_24dp;
import static java.lang.Integer.parseInt;

public class BookDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    public static final String LOG_TAG = "BookDetailsActivity";

    Book item;
    private String phone;
    private String serverUrl = "https://hello-cloudbase-3gcqukj9c27892d6-1305327820.ap-shanghai.service.tcloudbase.com/api/v1.0/book-collection"; //服务器收藏集合地址
    public JSONObject requestBody; //消息体

    ImageView imgbook;
    TextView title;
    TextView author;
    RatingBar ratingBar;
    TextView score;
    TextView pages;
    ImageView fav;
    TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
//        getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        phone = preferences.getString("userPhone",null);

        //ini view
        imgbook = findViewById(R.id.item_book_img);
        title = findViewById(R.id.item_book_title);
        author = findViewById(R.id.item_book_author);
        ratingBar = findViewById(R.id.item_book_ratingbar);
        score = findViewById(R.id.item_book_score);
        pages = findViewById(R.id.item_book_pagesrev);
        fav = findViewById(R.id.imageView3);
        description = findViewById(R.id.details_desc);


        // we need to get book item object
        item = (Book) getIntent().getExtras().getSerializable("bookObject");
        loadBookData(item);

        if (LoaderManager.getInstance(this).getLoader(0) != null) {
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }
    }

    //从豆瓣服务器得到书籍的具体描述
    public void getPagesAndDesc(String ulrDest, Book item) {
        // Set up variables for the try block that need to be closed in the
        // finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            // Build the full query URI, limiting results to 10 items and
            // printed books.
            // Convert the URI to a URL.
            URL requestURL = new URL(ulrDest);

            // Open the network connection.
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(10000);
//            urlConnection.connect();

            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

            String line;
            if (urlConnection.getResponseCode() != 200) {
                Toast.makeText(this, "请求书籍详细信息失败", Toast.LENGTH_SHORT).show();
                return;
            }
            while ((line = reader.readLine()) != null) {
                // Add the current line to the string.
                builder.append(line);

                // Since this is JSON, adding a newline isn't necessary (it won't
                // affect parsing) but it does make debugging a *lot* easier
                // if you print out the completed buffer for debugging.
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty.  Exit without parsing.
                Toast.makeText(this, "请求书籍详细信息为空", Toast.LENGTH_SHORT).show();
                return;
            }

            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the connection and the buffered reader.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Write the final JSON response to the log
        Log.d("书籍详细信息", bookJSONString);
        //解析对象
        int pages = 0;
        String desc = "";
        try {
            JSONObject alldata = new JSONObject(bookJSONString);
            JSONObject bookData = alldata.getJSONObject("data");
            String pageInfo = bookData.getJSONArray("info").get(3).toString();
            try {
                pages = parseInt(pageInfo.substring(3));
            } catch (Exception e) {
                e.printStackTrace();
                pages = 0;
            }

            String publisher = bookData.getJSONArray("info").get(1).toString() + "\n";
            String publishYear = bookData.getJSONArray("info").get(2).toString() + "\n";
            String price = bookData.getJSONArray("info").get(4).toString() + "\n";
            String ISBN = bookData.getJSONArray("info").get(6).toString() + "\n";
            JSONArray contents = bookData.getJSONArray("content");
            String content = "";
            for (int i = 0; i < contents.length(); i ++) {
                content += "    " + contents.get(i).toString() + "\n";
            }
            desc = publisher + publishYear + price + ISBN + "简介:\n" + content;

        } catch (JSONException e) {
            Toast.makeText(this, "解析书籍详细信息失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        item.setPages(pages);
        item.setDescription(desc);
        return;
    }

    //渲染数据到页面
    private void loadBookData(Book item) {
        //当是收藏数据时，直接加载
        if (item.getIsFav().equals("yes")) {
            Glide.with(this).load(item.getImgUrl()).into(imgbook);
            title.setText(item.getTitle());
            author.setText(item.getAuthor());
            ratingBar.setRating(item.getRating());
            if (item.getPages() == 0) {
                pages.setText("No pages info");
            } else {
                pages.setText(item.getPages()+" pages");
            }
            score.setText(item.getRating()+"");
            description.setText("简介：\n  " + item.getDescription());
            fav.setImageResource(ic_favorite_red_24dp); //收藏的话是红色的心
            return;
        }

        String id = item.getReview();
        try {
            parseInt(id);
        } catch(NumberFormatException e) {
            //如果是google搜索的api
            Glide.with(this).load(item.getImgUrl()).into(imgbook);
            title.setText(item.getTitle());
            author.setText(item.getAuthor());
            ratingBar.setRating(item.getRating());
            if (item.getPages() == 0) {
                pages.setText("No pages info");
            } else {
                pages.setText(item.getPages()+" pages");
            }
            score.setText(item.getRating()+"");
            description.setText("简介：\n  " + item.getDescription());
            return;
        }
        //如果是豆瓣qpi的数据
        getPagesAndDesc("http://39.105.38.10:8081/book/info?id="+id, item);
        Glide.with(this).load(item.getImgUrl()).into(imgbook);
        title.setText(item.getTitle());
        author.setText(item.getAuthor());
        ratingBar.setRating(item.getRating());
        if (item.getPages() == 0) {
            pages.setText("No pages info");
        } else {
            pages.setText(item.getPages()+" pages");
        }
        score.setText(item.getRating()+"");
        description.setText(item.getDescription());
    }

    //点击爱心图标
    public void addCollection(View view) {
//        Toast.makeText(this, "点击了爱心", Toast.LENGTH_SHORT).show();
        if (item.getIsFav().equals("no")) {
            item.setIsFav("yes");
//            Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
        } else if (item.getIsFav().equals("yes")) {
            item.setIsFav("no");
//            Toast.makeText(this, "取消收藏成功", Toast.LENGTH_SHORT).show();
        }
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
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        if (item.getIsFav() == "no") { //取消收藏
            requestBody = new JSONObject();
            JSONObject query = new JSONObject();
            try {
                query.put("phone", phone);
                query.put("bookId", item.getReview());//要取消收藏的书的id
                requestBody.put("query", query);
                Log.d(LOG_TAG, String.valueOf(requestBody));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "构建请求体失败");
            }
            return new BookLoader(this, "DELETE", serverUrl+"/", requestBody);
        } else { //收藏
            JSONArray data = new JSONArray();
            JSONObject bookItem = new JSONObject();
            try {
                bookItem.put("phone", phone);
                bookItem.put("bookId", item.getReview());//要收藏的书的id
                bookItem.put("bookImg", item.getImgUrl());
                bookItem.put("bookName", item.getTitle());
                bookItem.put("bookAuthor", item.getAuthor());
                bookItem.put("description", item.getDescription());
                bookItem.put("bookReview", item.getRating()+"");
                bookItem.put("bookPages", item.getPages()+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data.put(bookItem);
            try {
                requestBody = new JSONObject();
                requestBody.put("data", data);
                Log.d(LOG_TAG, String.valueOf(requestBody));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "构建请求体失败");
            }
            return new BookLoader(this, "POST", serverUrl, requestBody);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (data != null) {
            Log.d(LOG_TAG, data);
        }
        if (item.getIsFav() == "no") {
            Toast.makeText(this, "取消收藏成功", Toast.LENGTH_SHORT).show();
            fav.setImageResource(ic_favorite_black_24dp);
        } else if (item.getIsFav() == "yes") {
            Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
            fav.setImageResource(ic_favorite_red_24dp);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}