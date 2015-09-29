package cn.geminiwen.circlepager.adapter;

import android.database.DataSetObservable;
import android.view.View;

/**
 * Created by geminiwen on 15/9/29.
 */
public abstract class BaseAdapter extends DataSetObservable {
    public abstract int getCount();
    public abstract View getView(int position);
}
