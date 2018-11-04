package xfinity.com.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CharacterDao {
    @Query("SELECT * FROM CharacterDetails")
    LiveData<List<CharacterDetails>> getAllCharacters();

    @Query("SELECT * FROM CharacterDetails WHERE id LIKE :id LIMIT 1")
    CharacterDetails findById(long id);

    @Query("SELECT * FROM CharacterDetails WHERE name LIKE :sQuery OR description LIKE :sQuery")
    List<CharacterDetails> findCharacterForQuery(String sQuery);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CharacterDetails... characters);

    @Delete
    void delete(CharacterDetails characterDetails);

    @Query("DELETE FROM CharacterDetails")
    void deleteAll();
}
