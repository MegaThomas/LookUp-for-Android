package xyz.basswarlock.lookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2016.8.16.
 */
public class UsageExample extends Fragment {

    ListView listView;

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
        listView = (ListView) getView().findViewById(R.id.example_listView);
    }

    public boolean searchHandler(View view) {
        String message = (String) ((AppCompatTextView) view).getText();
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
                String[] listText = new String[sentences.length()];
                for (int i = 0; i < sentences.length(); i++) {
                    sentence = sentences.getJSONObject(i);
                    listText[i] = sentence.get("sentence").toString();
                }
                ArrayAdapter adapter =
                        new ArrayAdapter(getActivity(),
                                android.R.layout.simple_expandable_list_item_1, listText);
                listView.setAdapter(adapter);
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

//    class UsageExample {
//        private String mSentence,
//                       mCorpus,
//                       mDate,
//                       mLink;
//
//        public void setmSentence(String s) {
//            mSentence = s;
//        }
//
//        public void setmCorpus(String s) {
//            mCorpus = s;
//        }
//
//        public void setmDate(String s) {
//            mDate = s;
//        }
//
//        public void setmLink(String s) {
//            mLink = s;
//        }
//    }
}


