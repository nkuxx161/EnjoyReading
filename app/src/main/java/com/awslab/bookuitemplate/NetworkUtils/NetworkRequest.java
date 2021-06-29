package com.awslab.bookuitemplate.NetworkUtils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkRequest {
    private static final String LOG_TAG = "NetworkRequest";

    static String getBookInfo(String requestMethod, String requestUrl, JSONObject requestBody) {

        // Set up variables for the try block that need to be closed in the
        // finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            // Build the full query URI, limiting results to 10 items and
            // printed books.

            // Convert the URI to a URL.
            URL requestURL = new URL(requestUrl);

            // Open the network connection.
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            if (requestMethod.equals("POST")) {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                OutputStreamWriter os = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                String content = String.valueOf(requestBody);
                os.write(content);
                os.flush();
                os.close();
            } else if (requestMethod.equals("DELETE")) {
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                String content = String.valueOf(requestBody);
                os.writeBytes(content);
                os.flush();
                os.close();
            } else if (requestMethod.equals("GET")) {
                urlConnection.setRequestMethod("GET");
            }
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(10000);
//            urlConnection.connect();

            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

//            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() != 201) {//处理得到的数据
//                Log.d(LOG_TAG, "请求书籍信息失败");
//                return null;
//            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Add the current line to the string.
                builder.append(line);
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty.  Exit without parsing.
                return null;
            }

            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "请求收藏的图书信息失败");
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
        if (bookJSONString != null) {
            Log.d(LOG_TAG, bookJSONString);
        }

        return bookJSONString;
    }
}
