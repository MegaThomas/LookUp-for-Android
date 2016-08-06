package xyz.basswarlock.lookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView autoCompleteTextView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        autoCompleteTextView.setAdapter(new AutoCompleteAdapter(this,
                android.R.layout.simple_dropdown_item_1line));
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchHandler(view);
            }
        });
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void searchHandler(View view) {
        String message = autoCompleteTextView.getText().toString().trim();
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new downloadTask().execute(url);
        } else {
            textView.setText(R.string.no_network);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (String str : result) {
                    spanned = (Spanned) TextUtils.concat(spanned,
                            Html.fromHtml(str+"<br/><br/>", Html.FROM_HTML_MODE_LEGACY));
                }
            } else {
                for (String str : result) {
                    spanned = (Spanned) TextUtils.concat(spanned,
                            Html.fromHtml(str+"<br/><br/>"));
                }
            }
            SpannableString text = new SpannableString(spanned);
            textView.setText(text, TextView.BufferType.SPANNABLE);
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
