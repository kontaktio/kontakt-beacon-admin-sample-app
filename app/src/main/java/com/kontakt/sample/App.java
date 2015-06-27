package com.kontakt.sample;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.kontakt.sample.util.LruBitmapCache;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.util.Logger;

import butterknife.ButterKnife;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setDebugLoggingEnabled(BuildConfig.DEBUG);
        Logger.setCrashlyticsLoggingEnabled(true);
        KontaktApiClient.init(this);
        ButterKnife.setDebug(BuildConfig.DEBUG);
    }

    protected RequestQueue mRequestQueue = null;
    protected ImageLoader mImageLoader = null;

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public ImageLoader  getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
        }

        return this.mImageLoader;
    }


    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
