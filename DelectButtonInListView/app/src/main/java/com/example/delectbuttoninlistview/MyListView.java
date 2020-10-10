package com.example.delectbuttoninlistview;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class MyListView extends ListView implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private GestureDetector gestureDetector;//手势监听器

    private OnDeleteListener listener;

    private View deleteButton;

    private ViewGroup itemLayout;

    private int selectedItem;

    private boolean isDeleteShown; //删除按钮是否已经显示

    public MyListView(Context context, AttributeSet attrs){
        super(context,attrs);
        gestureDetector = new GestureDetector(getContext(),this);
        setOnTouchListener(this);
    }

    public void setOnDeleteListener(OnDeleteListener l){
        listener = l;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("ondown","yes_Touch");
        if (isDeleteShown){
            itemLayout.removeView(deleteButton);
            deleteButton = null;
            isDeleteShown = false;
            return false;
        }else {
            return gestureDetector.onTouchEvent(event);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!isDeleteShown){
            selectedItem= pointToPosition((int)e.getX(),(int)e.getY());
            Log.d("ondown","yes_ondown");
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!isDeleteShown && Math.abs(velocityX)>Math.abs(velocityY)){
            deleteButton = LayoutInflater.from(getContext()).inflate(R.layout.delete_button,null);
            deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    itemLayout.removeView(deleteButton);
                    deleteButton = null;
                    isDeleteShown = false;
                    listener.onDelete(selectedItem);
                }
            });
            itemLayout = (ViewGroup)getChildAt(selectedItem - getFirstVisiblePosition()); //获取当前的子View是第几个
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            itemLayout.addView(deleteButton,params);
            isDeleteShown = true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    public interface OnDeleteListener {

        void onDelete(int index);

    }

}
