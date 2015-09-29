Infinite looper View
=======

I don't like viewpager.

But you are familiar with ViewPager's Adapter

so..

![demo](http://ww2.sinaimg.cn/large/5dd54131gw1ewjff8k95wg20f00qoduv.gif)

## How to Use?

in xml

```xml
<cn.geminiwen.circlepager.view.CirclePager
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/circle_pager" />
```

As you wish, extends BaseAdapter like this.

```java
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
```

then, invoke setAdapter of CirclePager

```java
mCirclePager = (CirclePager)findViewById(R.id.circle_pager);
mCirclePager.setAdapter(new Adapter());
```

if you want to start auto-play mode
just
```java
mCirclePager.setAutoPlay(true);
```

## More
just fork.