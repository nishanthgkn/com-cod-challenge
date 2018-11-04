package xfinity.com.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import xfinity.com.R;
import xfinity.com.data.CharacterDetails;

public class CharactersListAdapter extends RecyclerView.Adapter<CharactersListAdapter.CharactersViewHolder> {
    private static final int XFIN_LIST_LAYOUT_MANAGER = 0;
    private static final int XFIN_GRID_LAYOUT_MANAGER = 1;
    private final LayoutInflater mInflater;
    private int mLayoutViewType = 0;
    private List<CharacterDetails> mCharacterDetails;
    private CharacterSelectionListener mCharacterSelectionListener;

    CharactersListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CharactersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (mLayoutViewType) {
            case XFIN_GRID_LAYOUT_MANAGER:
                itemView = mInflater.inflate(R.layout.characters_grid_item, parent, false);
                break;
            case XFIN_LIST_LAYOUT_MANAGER:
            default:
                itemView = mInflater.inflate(R.layout.characters_list_item, parent, false);
                break;
        }

        return new CharactersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CharactersViewHolder holder, int position) {
        if (mCharacterDetails != null) {
            CharacterDetails current = mCharacterDetails.get(position);
            holder.getTitleView().setText(current.getName());
            ImageView imageView = holder.getIconView();
            if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(current.getImageUrl())
                        .placeholder(R.drawable.placeholder_logo)
                        .into(imageView);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.mTitle.setText("No Characters Found");
        }
    }

    @Override
    public int getItemCount() {
        return (mCharacterDetails == null) ? 0 : mCharacterDetails.size();
    }

    void setCharacters(List<CharacterDetails> mDataSet) {
        mCharacterDetails = mDataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mLayoutViewType;
    }

    void setLayoutViewType(int layoutViewType) {
        this.mLayoutViewType = layoutViewType;
    }

    public void setCharacterSelectionListener(CharacterSelectionListener characterSelectionListener) {
        this.mCharacterSelectionListener = characterSelectionListener;
    }

    class CharactersViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final ImageView mIcon;

        public CharactersViewHolder(View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharacterDetails current = mCharacterDetails.get(getAdapterPosition());
                    if (mCharacterSelectionListener != null) {
                        mCharacterSelectionListener.onCharacterSelected(current.getId());
                    }
                }
            });
            mTitle = itemView.findViewById(R.id.title);
            mIcon = itemView.findViewById(R.id.character_logo);
        }

        public TextView getTitleView() {
            return mTitle;
        }

        public ImageView getIconView() {
            return mIcon;
        }
    }
}
