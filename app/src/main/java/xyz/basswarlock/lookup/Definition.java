package xyz.basswarlock.lookup;

/**
 * Created by user on 2016.8.13.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class Definition extends Fragment {

    TextView textView;
    ScrollView scrollView;

    public Definition() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.definition, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textView = (TextView) getView().findViewById(R.id.definition_textView);
        scrollView = (ScrollView) getView().findViewById(R.id.definition_scrollView);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    public boolean searchHandler(View view) {
        String message = (String) ((AppCompatTextView) view).getText();
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        String url = "https://vocabulary.com/dictionary/definition.ajax?search="
                + message + "&lang=en";
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
