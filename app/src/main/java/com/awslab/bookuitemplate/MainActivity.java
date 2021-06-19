package com.awslab.bookuitemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awslab.bookuitemplate.model.Book;
import com.awslab.bookuitemplate.recyclerview.BookAdapter;
import com.awslab.bookuitemplate.recyclerview.BookCallback;
import com.awslab.bookuitemplate.recyclerview.CustomItemAnimator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// make sure to import this exact same class
import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements BookCallback {

    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private List<Book> mdata ;
    private Button btnAddBook,btnRemove;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initViews();
        getURLResource("http://39.105.38.10:8081/book/top250");
        //主线程等待子线程1s获取资源
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setupBookAdapter();
    }

    private void setupBookAdapter() {

        bookAdapter = new BookAdapter(mdata,this);
        rvBooks.setAdapter(bookAdapter);

    }

    public void getURLResource(final String ulrDest) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
//                    urlConnection.connect();

                    // Get the InputStream.
                    InputStream inputStream = urlConnection.getInputStream();

                    // Create a buffered reader from that input stream.
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    // Use a StringBuilder to hold the incoming response.
                    StringBuilder builder = new StringBuilder();

                    if (urlConnection.getResponseCode() != 200) {
                        initmdataBooks("");//处理得到的数据
                        return;
                    }
                    String line;
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
                        initmdataBooks("");//处理得到的数据
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
                Log.d("请求得到的json数据", bookJSONString);

                initmdataBooks(bookJSONString);//处理得到的数据
            }
        }).start();
    }

    private void initmdataBooks(String jsonString) {

        // for testing purpos I'm creating a random set of books
        // you may get your data from web service or firebase database.

        mdata = new ArrayList<>();
//        mdata.add(new Book("我的心中每天开出一朵花", "暂无描述", "幾米",
//                "https://img9.doubanio.com/view/subject/l/public/s1150266.jpg",
//                0, 80, (float)4.0));
        //对返回的json数据解析，构建book对象
        try {
            String id = "", title = "", description= "", author = "", imgUrl = "";
            int pages = 0;
            float rating = 0;
            JSONObject jsonData = new JSONObject(jsonString);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray bookArray = data.getJSONArray("subject");
            for (int i = 0; i < bookArray.length(); i++) {
                JSONObject book = bookArray.getJSONObject(i);
                title = book.getString("title");
                description = "暂无描述";
                try {
                    author = book.getJSONArray("abstract").get(0).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    author = book.getString("gray");
                }
                imgUrl = book.getString("img");
                pages = 0;//暂时获取不到
                id = book.getString("id");
                try {
                    rating = parseFloat(book.getString("score"));
                } catch (Exception e) {
                    rating = 8;
                }
                mdata.add(new Book(title, description, author, imgUrl, pages, id, rating/2));
            }
        } catch (JSONException e) {
            Looper.prepare();
            Toast.makeText(MainActivity.this, "获取豆瓣图书失败", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    private void initViews() {

        btnAddBook = findViewById(R.id.btn_add);
        btnRemove = findViewById(R.id.btn_remove);
        rvBooks = findViewById(R.id.rv_book);
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        rvBooks.setHasFixedSize(true);

        // we need first to setupe the custom item animator that we just create
        rvBooks.setItemAnimator(new CustomItemAnimator());

        // to test the animation we need to simulate the add book operation

        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topBook();
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newestBook();
            }
        });



    }

    private void topBook() {
        getURLResource("http://39.105.38.10:8081/book/newBook");
        //主线程等待子线程1s获取资源
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setupBookAdapter();

    }

    private void newestBook() {
        getURLResource("http://39.105.38.10:8081/book/top250");
        //主线程等待子线程1s获取资源
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setupBookAdapter();
    }

    //跳转详情界面
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
}
