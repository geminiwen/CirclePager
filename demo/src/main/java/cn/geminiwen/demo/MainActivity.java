package cn.geminiwen.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import cn.geminiwen.circlepager.adapter.BaseAdapter;
import cn.geminiwen.circlepager.view.CirclePager;


public class MainActivity extends AppCompatActivity {

    private CirclePager mCirclePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mCirclePager = (CirclePager)findViewById(R.id.circle_pager);
        mCirclePager.setAdapter(new Adapter());
    }

    private class Adapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater = LayoutInflater.from(MainActivity.this);
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public View getView(int position) {
            if (position == 0) {
                return mLayoutInflater.inflate(R.layout.item_0, null);
            } else {
                return mLayoutInflater.inflate(R.layout.item_1, null);
            }
        }
    }
}
