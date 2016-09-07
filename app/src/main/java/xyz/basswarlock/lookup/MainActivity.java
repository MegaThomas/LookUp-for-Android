package xyz.basswarlock.lookup;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    TextView toolbarTitle;
    MenuItem searchItem;
    TabLayout tabLayout;
    ViewPager viewPager;
    AutoCompleteSearchView searchView;
    Definition def;
    Synonym syn;
    UsageExample exa;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolbar();
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        initViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//      Doesn't work here
//        def = (Definition) getSupportFragmentManager().findFragmentByTag(
//                "android:switcher:" + viewPager.getId() + ":" + 0);
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
                toolbarTitle.setText(((AppCompatTextView) view).getText());
                try {
                    def.searchHandler(view);
                    syn.test();
                    exa.searchHandler(view);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                searchItem.collapseActionView();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                toolbarTitle.setText(query);
                boolean isFound = def.searchHandler(query) && exa.searchHandler(query);
                searchItem.collapseActionView();
                return isFound;
//                return true;
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
        def = (Definition) adapter.getItem(0);
        exa = (UsageExample) adapter.getItem(1);
        syn = (Synonym) adapter.getItem(2);
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

    private void initViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Definition(), getResources().getString(R.string.Definition));
        adapter.addFragment(new UsageExample(), getResources().getString(R.string.Example));
        adapter.addFragment(new Synonym(), getResources().getString(R.string.Synonym));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
