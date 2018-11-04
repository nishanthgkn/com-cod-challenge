package xfinity.com.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import xfinity.com.R;
import xfinity.com.data.CharacterDetails;
import xfinity.com.data.CharactersViewModel;

public class CharacterDetailsFragment extends Fragment {
    public static final String TAG = "CharacterDetailsFragment";
    private View mRootView;
    private CharactersViewModel mCharactersViewModel;
    private CharacterDetails mCharacterDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharactersViewModel = ViewModelProviders.of(this).get(CharactersViewModel.class);
        mCharactersViewModel.getCurrentSelectedCharacter().observe(this, new Observer<CharacterDetails>() {
            @Override
            public void onChanged(@Nullable CharacterDetails characterDetails) {
                mCharacterDetails = characterDetails;
                updateView();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_character_details, container, false);
        mRootView.setTag(TAG);

        updateView();
        return mRootView;
    }

    private void updateView() {
        ImageView imageView = mRootView.findViewById(R.id.character_logo);
        TextView title = mRootView.findViewById(R.id.character_title);
        TextView desc = mRootView.findViewById(R.id.character_description);
        desc.setMovementMethod(new ScrollingMovementMethod());

        if (mCharacterDetails != null) {
            String imageUrl = mCharacterDetails.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_logo)
                        .into(imageView);
            }

            String characterTitle = mCharacterDetails.getName();
            if (characterTitle != null && !characterTitle.isEmpty()) {
                title.setText(characterTitle.trim());
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.setPageTitleAndOptions(characterTitle.trim());
            }

            String characterDescription = mCharacterDetails.getDescription();
            if (characterDescription != null && !characterDescription.isEmpty()) {
                desc.setText(characterDescription.trim());
            }
        }
    }
}
