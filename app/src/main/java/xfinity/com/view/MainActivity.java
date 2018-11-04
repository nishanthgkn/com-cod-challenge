package xfinity.com.view;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import xfinity.com.R;

public class MainActivity extends AppCompatActivity {
    private static final String XFIN_GRID_MODE_KEY = "XFIN_GRID_MODE_KEY";
    private static final String XFIN_QUERY_STRING_KEY = "XFIN_QUERY_STRING_KEY";
    private boolean isInGridMode = false;
    private ViewTypeChangedListener mViewTypeChangedListener;
    private SearchView mSearchView;
    private SearchView.OnQueryTextListener mQueryTextListener;
    private String mCurrentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isRunningOnTab()) {
            if (savedInstanceState != null) {
                isInGridMode = savedInstanceState.getBoolean(XFIN_GRID_MODE_KEY);
                mCurrentQuery = savedInstanceState.getString(XFIN_QUERY_STRING_KEY);
                return;
            }

            CharactersListFragment fragment = new CharactersListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.characters_fragment_container, fragment, CharactersListFragment.TAG);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_options, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionFavorite = menu.findItem(R.id.action_favorite);
        MenuItem actionSearch = menu.findItem(R.id.search);

        boolean showSearchOptions = !isShowingDetailsFragment();

        if (!isRunningOnTab() && !isShowingDetailsFragment()) {
            // We show the view options only when the app is currently running on a phone,
            // and we are in the character list page
            actionFavorite.setVisible(true);
            if (isInGridMode) {
                isInGridMode = !isInGridMode; // Invert here, will be switched back in onOptionsItemSelected()

                if (actionFavorite != null) {
                    onOptionsItemSelected(actionFavorite);
                }
            }
        } else {
            actionFavorite.setVisible(false);
        }

        if (!isRunningOnTab() && isShowingDetailsFragment()) {
            // We hide the search option only when the app is running on a phone,
            // and the user is currently in character details page
            actionSearch.setVisible(false);
        } else {
            actionSearch.setVisible(true);
            if (mQueryTextListener != null) {
                SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
                mSearchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
                mSearchView.setOnQueryTextListener(mQueryTextListener);
                if (!TextUtils.isEmpty(mCurrentQuery)) {
                    mSearchView.setQuery(mCurrentQuery, true);
                }
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorite) {
            if (isInGridMode) {
                // Switch to List View Mode
                item.setIcon(R.drawable.baseline_list_white_18dp);

            } else {
                // Switch to Grid View Mode
                item.setIcon(R.drawable.baseline_grid_on_white_18dp);
            }

            isInGridMode = !isInGridMode;
            if (mViewTypeChangedListener != null)
                mViewTypeChangedListener.onViewTypeChanged(isInGridMode);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setViewTypeChangedListener(ViewTypeChangedListener viewTypeChangedListener) {
        this.mViewTypeChangedListener = viewTypeChangedListener;
    }

    void showCharacterDetails() {
        if (isRunningOnTab()) return;
        CharacterDetailsFragment fragment = new CharacterDetailsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.characters_fragment_container, fragment, CharacterDetailsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(XFIN_GRID_MODE_KEY, isInGridMode);
        if (mSearchView != null) {
            outState.putString(XFIN_QUERY_STRING_KEY, mSearchView.getQuery().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private boolean isRunningOnTab() {
        return (findViewById(R.id.characters_fragment_container) == null);
    }

    void setPageTitleAndOptions(String title) {
        if (isRunningOnTab()) return;
        if (TextUtils.isEmpty(title)) {
            title = getResources().getString(R.string.app_name);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        invalidateOptionsMenu();
    }

    private boolean isShowingDetailsFragment() {
        CharacterDetailsFragment characterDetailsFragment = (CharacterDetailsFragment) getSupportFragmentManager().findFragmentByTag(CharacterDetailsFragment.TAG);
        return (characterDetailsFragment != null && characterDetailsFragment.isVisible());
    }

    public void setQueryTextListener(SearchView.OnQueryTextListener queryTextListener) {
        this.mQueryTextListener = queryTextListener;
    }
}
