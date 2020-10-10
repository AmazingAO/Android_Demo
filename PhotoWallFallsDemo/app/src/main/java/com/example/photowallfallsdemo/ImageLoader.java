package com.example.photowallfallsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class ImageLoader {
    /**
     * 图片的核心缓存类，用于缓存下载好的图片，其内部使用HashMap进行存储键值对，并且使用最近最少最少访问算法进行对缓存的删除
     */
     private static LruCache<String, Bitmap> mMemoryCache;

    /**
     * ImageLoader实例
     */
    private static ImageLoader imageLoader;
    private ImageLoader(){
        //获取应用运行的最大缓存
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        //设置图片缓存为应用程序最大缓存的1/8
        int cacheSize = maxMemory/8;
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                return value.getByteCount();
            }
        };
    }
    /**
     * 获取ImageLoader实例 使用懒汉式的单例模式
     */
    public static ImageLoader getInstance(){
        if (imageLoader == null){
            imageLoader = new ImageLoader();
        }
        return imageLoader;
    }

    /**
     * 将缓存图片存入LruCache中
     */
    public void addBitmapToMemoryCache(String key , Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    /**
     * 从LruCache中获取一张图片
     */
    public Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    /**
     * 计算图片的宽度
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth){
        //获取原图片的长度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width>reqWidth){
                final int widthRatio = Math.round((float)width/(float)reqWidth);
                inSampleSize = widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 获取到源图片的宽度并计算
     */
    public static Bitmap decodeSampleBitmapFromResource(String pathName,int ReqWidth){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//禁用BitmapFactory为其分配内存，目的只为获取图片信息
        BitmapFactory.decodeFile(pathName,options);
        options.inSampleSize = calculateInSampleSize(options,ReqWidth);
        options.inJustDecodeBounds =false;
        return BitmapFactory.decodeFile(pathName,options);
    }
}
