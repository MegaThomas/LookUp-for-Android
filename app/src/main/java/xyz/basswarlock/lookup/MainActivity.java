package xyz.basswarlock.lookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void searchHandler(View view) {
        String message = editText.getText().toString().trim();
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        Log.v("searchHandler", message);
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
                            Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY), "\n\n");
                }
            } else {
                for (String str : result) {
                    spanned = (Spanned) TextUtils.concat(spanned,
                            Html.fromHtml(str), "\n");
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
            return new String[] {""};
        }
    }

    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[256];
        String result = "";
        while (-1 != reader.read(buffer)) {
            result += new String(buffer);
        }
        result += new String(buffer);
        return result;
    }
}
