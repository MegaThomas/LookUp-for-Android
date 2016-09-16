package xyz.basswarlock.lookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016.8.16.
 */
public class UsageExample extends Fragment {

    private RecyclerView recyclerView;
    private UsageExampleAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String query;

    public UsageExample() {
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
        return inflater.inflate(R.layout.usage_example, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    public boolean searchHandler(View view) {
        String message = (String) ((AppCompatTextView) view).getText();
        query = message;
        String url = "https://corpus.vocabulary.com/api/1.0/examples.json?query=" + message +
                "&maxResults=24&startOffset=0&filter=0&_=" + System.currentTimeMillis();
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new downloadTask().execute(url);
            return true;
        } else {
//            textView.setText(R.string.no_network);
            return false;
        }
    }

    public boolean searchHandler(String message) {
        query = message;
        String url = "https://corpus.vocabulary.com/api/1.0/examples.json?query=" + message +
                "&maxResults=24&startOffset=0&filter=0&_=" + System.currentTimeMillis();
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new downloadTask().execute(url);
            return true;
        } else {
//            textView.setText(R.string.no_network);
            return false;
        }
    }
    private class downloadTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                JSONArray sentences =  result.getJSONObject("result").getJSONArray("sentences");
                JSONObject sentence;
                List<UsageExampleItem> examples = new ArrayList<>();
                for (int i = 0; i < sentences.length(); i++) {
                    sentence = sentences.getJSONObject(i);
                    // https://cdn.vocab.com/js/vcom/package-1lzk5qf.js:209
                    if (sentence.getJSONObject("volume").has("locator"))
                        examples.add(new UsageExampleItem(
                            sentence.get("sentence").toString(),
                            sentence.getJSONObject("volume").getJSONObject("corpus").get("name").toString(),
                            sentence.getJSONObject("volume").has("datePublished") ?
                                sentence.getJSONObject("volume").get("datePublished").toString() :
                                sentence.getJSONObject("volume").has("dateAdded") ?
                                        sentence.getJSONObject("volume").get("dateAdded").toString() :
                                            "",
                            sentence.getJSONObject("volume").get("locator").toString()));
                    else
                        examples.add(new UsageExampleItem(
                                sentence.get("sentence").toString(),
                                sentence.getJSONObject("volume").getJSONObject("corpus").get("name").toString(),
                                sentence.getJSONObject("volume").get("datePublished").toString()));
                }
                adapter = new UsageExampleAdapter(examples);
                recyclerView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            textView.setText(text, TextView.BufferType.SPANNABLE);
            // scroll to top
//            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }
    }

    private JSONObject downloadUrl(String myurl) throws IOException {
        // return JSON, don't check MIME, otherwise throw 'UnsupportedMimeTypeException'
        String response = Jsoup.connect(myurl).ignoreContentType(true).execute().body();
        JSONObject json;
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            json = new JSONObject();
        }
        return json;
    }

//    class UsageExampleArrayAdapter extends SimpleAdapter {
//
//        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
//
//        public UsageExampleArrayAdapter(Context context,
//                                        String[] data,
//                                        int resource,
//                                        String[] from,
//                                        int[] to) {
//            super(context, data, resource, from, to);
//            List<Map<String, Object>> list = new ArrayList<>();
//            Map<String, Object> map;
//            for (String sentence : data) {
//                map = new HashMap<String, Object>();
//                map.put("list_item_content", sentence);
//                list.add(map);
//            }
//        }
//
//
//        @Override
//        public boolean hasStableIds() {
//            return true;
//        }
//    }


    class UsageExampleItem {
        private String mSentence,
                       mCorpus,
                       mDate,
                       mLink;

        public UsageExampleItem(String sentence, String corpus, String date, String link) {
            mSentence = sentence;
            mCorpus = corpus;
            mDate = date;
            mLink = link;
        }

        public UsageExampleItem(String sentence, String corpus, String date) {
            mSentence = sentence;
            mCorpus = corpus;
            mDate = date;
            mLink = "";
        }

        public void setmSentence(String s) {
            mSentence = s;
        }

        public void setmCorpus(String s) {
            mCorpus = s;
        }

        public void setmDate(String s) {
            mDate = s;
        }

        public void setmLink(String s) {
            mLink = s;
        }

        public String getmSentence() {
            return mSentence;
        }

        public String getmCorpus() {
            return mCorpus;
        }

        public String getmDate() {
            return mDate;
        }

        public String getmLink() {
            return mLink;
        }
    }

    class UsageExampleAdapter extends RecyclerView.Adapter<UsageExampleAdapter.MyViewHolder> {

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView sentence,
                    source;
            CardView cardView;

            MyViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.card_view);
                sentence = (TextView) itemView.findViewById(R.id.sentence);
                source   = (TextView) itemView.findViewById(R.id.source);
            }
        }

        List<UsageExampleItem> examples;

        public UsageExampleAdapter(List<UsageExampleItem> examples) {
            this.examples = examples;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(v);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
            String sentence = examples.get(position).getmSentence();
            Spanned spanned = new SpannableStringBuilder();
            spanned = (Spanned) TextUtils.concat(spanned,
                    Html.fromHtml(sentence.replaceAll(query, "<strong>"+query+"</strong>")));
            SpannableString text = new SpannableString(spanned);
            myViewHolder.sentence.setText(text);

            String date = examples.get(position).getmDate().substring(0,10);
            date = date.replace('-', '.');
            spanned = new SpannableStringBuilder();
            spanned = (Spanned) TextUtils.concat(spanned,
                    Html.fromHtml("<i>" + examples.get(position).getmCorpus() + "</i>   "), date);
            text = new SpannableString(spanned);
            myViewHolder.source.setText(text, TextView.BufferType.SPANNABLE);
        }

        @Override
        public int getItemCount() {
            return examples.size();
        }
    }
}


