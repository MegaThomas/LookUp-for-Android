package xyz.basswarlock.lookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView textView,
             toolbarTitle;
    ScrollView scrollView;
    Toolbar toolbar;
    MenuItem searchItem;
    AutoCompleteSearchView searchView;
//    String theWord;
    // dummy theme for API 23+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        textView = (TextView) findViewById(R.id.textView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        initTextView();
        initToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // show menu buttons
        getMenuInflater().inflate(R.menu.menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (AutoCompleteSearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setImeOptions(EditorInfo.IME_FLAG_FORCE_ASCII|EditorInfo.IME_ACTION_SEARCH);
        searchView.setAdapter(new AutoCompleteAdapter(this,
                android.R.layout.simple_dropdown_item_1line));
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchHandler(view);
                searchItem.collapseActionView();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                boolean isFound = searchHandler(query);
                searchItem.collapseActionView();
                return isFound;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ((EditText) searchView.findViewById(
                android.support.v7.appcompat.R.id.search_src_text)).setTextColor(
                    ContextCompat.getColor(this, R.color.white));
        ((EditText) searchView.findViewById(
                android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(
                ContextCompat.getColor(this, R.color.white));
//        searchView.setOnQueryTextListener();
        MenuItemCompat.OnActionExpandListener expandListener =
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when action item collapses
                        return true;  // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        return true;  // Return true to expand action view
                    }
                };

        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolbar() {
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        // no title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initTextView() {
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    public boolean searchHandler(View view) {
        String message = (String) ((AppCompatTextView) view).getText();
        toolbarTitle.setText(message);
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new downloadTask().execute(url);
            return true;
        } else {
            textView.setText(R.string.no_network);
            return false;
        }
    }

    public boolean searchHandler(String message) {
        toolbarTitle.setText(message);
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new downloadTask().execute(url);
            return true;
        } else {
            textView.setText(R.string.no_network);
            return false;
        }
    }

    private class downloadTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return new String[] {getResources().getString(R.string.retrieve_failed)};
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            Spanned spanned = new SpannableStringBuilder();
            ArrayList<Integer> paragraph = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (String str : result) {
                    spanned = (Spanned) TextUtils.concat(spanned,
                            Html.fromHtml(str+"<br/><br/>", Html.FROM_HTML_MODE_LEGACY));
                    paragraph.add(spanned.length());
                }
            } else {
                for (String str : result) {
                    spanned = (Spanned) TextUtils.concat(spanned,
                            Html.fromHtml(str+"<br/><br/>"));
                    paragraph.add(spanned.length());
                }
            }
            SpannableString text = new SpannableString(spanned);
            text.setSpan(new TypefaceSpan(""), 0, paragraph.get(0),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(text, TextView.BufferType.SPANNABLE);
            // scroll to top
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }
    }

    private String[] downloadUrl(String myurl) throws IOException {
        Document doc = Jsoup.connect(myurl).get();
        Element defShort = doc.select("p.short").first();
        Element defLong = doc.select("p.long").first();
        if (null != defShort && null != defLong) {
            return new String[] {defShort.html(), defLong.html()};
        } else {
            return new String[] {"Not found."};
        }
    }
}
