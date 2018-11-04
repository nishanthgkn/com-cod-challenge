package xfinity.com.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

public class CharactersViewModel extends AndroidViewModel {
    private static MutableLiveData<CharacterDetails> mCurrentSelectedCharacter;
    private CharacterRepository mRepository;
    private LiveData<List<CharacterDetails>> mAllCharacters;
    private MutableLiveData<List<CharacterDetails>> mCurrentCharacters;

    public CharactersViewModel(@NonNull Application application) {
        super(application);
        mRepository = CharacterRepository.getInstance(application);
        mAllCharacters = mRepository.getAllCharacters();
        mAllCharacters.observeForever(new Observer<List<CharacterDetails>>() {
            @Override
            public void onChanged(@Nullable List<CharacterDetails> details) {
                getMutableCurrentCharacters().setValue(details);
            }
        });
    }

    public LiveData<List<CharacterDetails>> getCurrentCharacters() {
        return getMutableCurrentCharacters();
    }

    private MutableLiveData<List<CharacterDetails>> getMutableCurrentCharacters() {
        if (mCurrentCharacters == null) {
            mCurrentCharacters = new MutableLiveData<>();
        }
        return mCurrentCharacters;
    }

    public LiveData<CharacterDetails> getCurrentSelectedCharacter() {
        return getMutableCurrentSelectedCharacter();
    }

    public void setCurrentSelectedCharacter(long characterId) {
        getMutableCurrentSelectedCharacter().setValue(mRepository.getCharacterById(characterId));
    }

    private MutableLiveData<CharacterDetails> getMutableCurrentSelectedCharacter() {
        if (mCurrentSelectedCharacter == null) {
            mCurrentSelectedCharacter = new MutableLiveData<CharacterDetails>();
        }
        return mCurrentSelectedCharacter;
    }

    public void getCharactersForQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            getMutableCurrentCharacters().setValue(mAllCharacters.getValue());
        } else {
            getMutableCurrentCharacters().setValue(mRepository.getCharacterForQuery(query));
        }
    }
}
