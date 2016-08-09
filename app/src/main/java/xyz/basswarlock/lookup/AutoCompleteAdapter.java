package xyz.basswarlock.lookup;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> mData;

    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mData = new ArrayList<>();
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public String getItem(int index){
        return mData.get(index);
    }

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (null != constraint) {
                    mData.clear();
                    String url = "https://www.vocabulary.com/dictionary/autocomplete?search="
                            + constraint;
                    try {
                        Document doc = Jsoup.connect(url).get();
                        Elements words = doc.select("li > div > span.word");
                        for (Element word : words)
                        {
                            mData.add(word.text());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                } else {
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                if (null != filterResults && filterResults.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }


}
