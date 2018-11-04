package xfinity.com.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.lang.annotation.Retention;
import java.util.List;

import xfinity.com.R;
import xfinity.com.data.CharacterDetails;
import xfinity.com.data.CharactersViewModel;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class CharactersListFragment extends Fragment {
    public static final String TAG = "CharactersListFragment";
    private static final String KEY_LAYOUT_MANAGER = "layout_manager";
    private static final int SPAN_COUNT = 2;

    private static final int XFIN_LIST_LAYOUT_MANAGER = 0;
    private static final int XFIN_GRID_LAYOUT_MANAGER = 1;
    protected CharactersListAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private CharactersViewModel mCharactersViewModel;

    private CharacterSelectionListener mCharacterSelectionListener = new CharacterSelectionListener() {
        @Override
        public void onCharacterSelected(long characterId) {
            mCharactersViewModel.setCurrentSelectedCharacter(characterId);
            ((MainActivity) getActivity()).showCharacterDetails();
        }
    };
    private @LayoutManagerType
    int mCurrentLayoutManagerType = XFIN_LIST_LAYOUT_MANAGER;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharactersViewModel = ViewModelProviders.of(this).get(CharactersViewModel.class);
        mCharactersViewModel.getCurrentCharacters().observe(this, new Observer<List<CharacterDetails>>() {
            @Override
            public void onChanged(@Nullable List<CharacterDetails> details) {
                mAdapter.setCharacters(details);
                if (details != null && !details.isEmpty()) {
                    mCharactersViewModel.setCurrentSelectedCharacter(details.get(0).getId());
                }
            }
        });

        ((MainActivity) getActivity()).setQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCharactersViewModel.getCharactersForQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCharactersViewModel.getCharactersForQuery(newText);
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_characters, container, false);
        rootView.setTag(TAG);

        mRecyclerView = rootView.findViewById(R.id.characters_list);
        mAdapter = new CharactersListAdapter(rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(100);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mLayoutManager = new LinearLayoutManager(getActivity());
        if (savedInstanceState != null) {
            mCurrentLayoutManagerType = savedInstanceState.getInt(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
        mAdapter.setCharacterSelectionListener(mCharacterSelectionListener);

        ((MainActivity) getActivity()).setViewTypeChangedListener(new ViewTypeChangedListener() {
            @Override
            public void onViewTypeChanged(boolean isInGridViewMode) {
                if (isInGridViewMode) {
                    setRecyclerViewLayoutManager(XFIN_GRID_LAYOUT_MANAGER);
                    return;
                }
                setRecyclerViewLayoutManager(XFIN_LIST_LAYOUT_MANAGER);
            }
        });

        return rootView;
    }

    private void setRecyclerViewLayoutManager(@LayoutManagerType int layoutManagerType) {
        int scrollPosition = 0;

        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case XFIN_GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = XFIN_GRID_LAYOUT_MANAGER;
                break;
            case XFIN_LIST_LAYOUT_MANAGER:
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = XFIN_LIST_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter.setLayoutViewType(layoutManagerType);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) activity.setPageTitleAndOptions(null);
    }

    @Retention(SOURCE)
    @IntDef({XFIN_LIST_LAYOUT_MANAGER, XFIN_GRID_LAYOUT_MANAGER})
    @interface LayoutManagerType {
    }
}
