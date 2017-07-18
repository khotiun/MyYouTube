package com.khotiun.myyoutube.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by hotun on 17.07.2017.
 */

public class MySingleton {

    private static MySingleton sInstance;
    private RequestQueue mRequestQueue;//очередь запросов библиотеки volley, будет использоваться адаптером для загрузки видеороликов
    private final ImageLoader mImageLoader;//загрузчик картинок
    private static Context sContext;

    private MySingleton(Context context) {
        sContext = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {

            private final LruCache<String, Bitmap>//хранит постоянные ссылки на ограниченное число значений
            //когда значение запрашивается оно помещается в начало очереди
            mCache = new LruCache<>(20);
            //метод для извлечения изображения
            @Override
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
            //метод для сохранения изображения
            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
        });
    }
    //только один поток сможет использовать данный метод
    public static synchronized MySingleton getInstance(Context context){
        if(sInstance == null) {
            sInstance = new MySingleton(context);
        }
        return sInstance;
    }
    //метод получения очереди
    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(sContext.getApplicationContext());
        }
        return mRequestQueue;
    }
    //метод добавления запросов в очередь
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
    //метод для получения картинок
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
