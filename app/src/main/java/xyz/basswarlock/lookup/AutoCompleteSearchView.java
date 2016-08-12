package xyz.basswarlock.lookup;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.support.v4.widget.CursorAdapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Created by user on 2016.8.10.
 */
public class AutoCompleteSearchView extends SearchView {

    private SearchView.SearchAutoComplete mData;

    public AutoCompleteSearchView(Context context) {
        super(context);
        init();
    }

    public AutoCompleteSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoCompleteSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        // prohibited
    }

    private void init() {
        mData = (SearchAutoComplete)
                findViewById(android.support.v7.appcompat.R.id.search_src_text);
        this.setAdapter(null);
        this.setOnItemClickListener(null);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mData.setOnItemClickListener(listener);
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        mData.setAdapter(adapter);
    }

    public void setText(String text) {
        mData.setText(text);
    }
}
