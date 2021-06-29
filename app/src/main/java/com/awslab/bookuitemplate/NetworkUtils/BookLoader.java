package com.awslab.bookuitemplate.NetworkUtils;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONObject;

public class BookLoader extends AsyncTaskLoader<String> {

    private String mRequestUrl;
    private String mRequestMethod;
    private JSONObject mRequestBody;

    public BookLoader(Context context, String requestMethod, String requestUrl, JSONObject resuestBody) {
        super(context);
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mRequestBody = resuestBody;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkRequest.getBookInfo(mRequestMethod, mRequestUrl, mRequestBody);
    }
}
