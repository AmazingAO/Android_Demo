package com.example.photowalldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {
    //记录正在下载或者等待下载的任务
    private Set<BitmapWorkerTask> taskCollection;

    //图片缓存，用于缓存下载好的图片，并按最近最少使用删除图片
    private LruCache<String,Bitmap>mMemoryCache;

    //照片墙实体类
    private GridView mPhotoWall;

    //第一张可见图片的下标
    private int mFirstVisibleItem;

    //一屏有多少张可见图片
    private int mVisibleCount;

    //记录是否刚刚打开程序
    private boolean isFirstEnter = true;



    public PhotoWallAdapter(Context context, int textViewResourceId, String[] objects,
                            GridView photoWall) {
        super(context,textViewResourceId,objects);
        taskCollection = new HashSet<BitmapWorkerTask>();
        mPhotoWall = photoWall;

        //获得应用的最大运行内存
        int maxMemory = (int)Runtime.getRuntime().maxMemory();

        //设置图片缓存大小为应用程序最大运行内存的1/8
        int cacheSize = maxMemory/8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                return value.getByteCount();
            }
        };
        mPhotoWall.setOnScrollListener(this);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String url = getItem(position);
        View view ;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.phone_layout,null);
        }else {
            view = convertView;
        }
        final ImageView photo = (ImageView)view.findViewById(R.id.phone);

        //给ImageView 设置一个Tag；保证异步加载时不会乱序
        photo.setTag(url);
        setImageView(url,photo);
        return view;
    }



    private void setImageView(String imageUrl,ImageView imageView){
        Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else {
            imageView.setImageResource(R.drawable.ic_launcher);
        }
    }

    /**
     * 将一张图片加入到LruCache中
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    /**
     * 在LruCache中取出一张图片
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);//LruCache使用的数据结构为HashMap
    }



    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //仅当GridView静止时才去下载图片，GridView滑动时取消下载
        if (scrollState == SCROLL_STATE_IDLE){
            loadBitmaps(mFirstVisibleItem,mVisibleCount);
        }else {
            cancleAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleCount = visibleItemCount;
        //下载任务应该在onScrollStateChanged中调用但是第一次加载onScrollStateChanged并不会调用，因为没有滑动
        //所以在这里首次启动下载任务
        if (isFirstEnter && visibleItemCount > 0){
            loadBitmaps(firstVisibleItem,visibleItemCount);
            isFirstEnter =false;
        }
    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个 ImageView 的Bitmap对象不在缓存中，就会开启异步线程去下载图片
     */
    private void loadBitmaps(int firstVisibleItem,int visibleCount){
        try{
            for(int i = firstVisibleItem;i<firstVisibleItem+visibleCount;i++){
                String imageUrl = Images.imageThumbUrls[i];
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap == null){
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(imageUrl);//开启异步线程获取资源
                }else {
                    ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                    if (imageView!= null && bitmap !=null){
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }





    //关闭下载任务
    public void cancleAllTasks(){
        if (taskCollection !=null){
            for (BitmapWorkerTask task : taskCollection){
                task.cancel(false);
            }
        }
    }


        class BitmapWorkerTask extends AsyncTask<String,Void, Bitmap>{
        /**
         * 图片的Url地址
         */
        private String imageUrl;


        @Override
        protected Bitmap doInBackground(String... strings) {
            imageUrl = strings[0];
//            Log.d("Bitmap",imageUrl);
            //在后台开始下载图片
            Bitmap bitmap = downloadBitMap(imageUrl);
//            if (bitmap!=null)
//            Log.d("Bitmap",bitmap.toString());
            if (bitmap != null){
                //将其下载好的Bitmap缓存在LrcCache中
                addBitmapToMemoryCache(imageUrl,bitmap);
            }
            return bitmap;
        }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                ImageView imageView = (ImageView)mPhotoWall.findViewWithTag(imageUrl);
                if (imageView!=null && bitmap !=null){
                    imageView.setImageBitmap(bitmap);
                }
                taskCollection.remove(this);
            }

            /**
         * 建立connect 获取 Bitmap对象
         * @param imageUrl
         * @return 获取的BitMap对象
         */
        private Bitmap downloadBitMap(String imageUrl){
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try{
                URL url = new URL(imageUrl);
                con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(5*1000);
                con.setReadTimeout(10*1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (con !=null){
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}
