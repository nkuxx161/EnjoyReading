package com.awslab.bookuitemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import static java.lang.Integer.parseInt;

public class BookDetailsActivity extends AppCompatActivity {

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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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

        Book item = (Book) getIntent().getExtras().getSerializable("bookObject");

        loadBookData(item);


    }
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

    private void loadBookData(Book item) {

        // bind book data here for now i will only load the book cover image
        String id = item.getReview();
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
}