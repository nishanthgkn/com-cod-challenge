package xfinity.com.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {CharacterDetails.class}, version = 1)
public abstract class CharacterDatabase extends RoomDatabase {
    private static volatile CharacterDatabase mInstance;
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new GenerateCharacterDetailsTask(mInstance).execute();
        }
    };

    static CharacterDatabase getDatabase(final Context context) {
        if (mInstance == null) {
            synchronized (CharacterDatabase.class) {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(context.getApplicationContext(),
                            CharacterDatabase.class, "character_database")
                            .allowMainThreadQueries()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return mInstance;
    }

    public abstract CharacterDao characterDao();

    private static class GenerateCharacterDetailsTask extends AsyncTask<CharacterDetails, Void, Void> {
        private final CharacterDao mDao;

        GenerateCharacterDetailsTask(CharacterDatabase mDatabase) {
            mDao = mDatabase.characterDao();
        }

        @Override
        protected Void doInBackground(CharacterDetails... characterDetails) {
            mDao.deleteAll();
            mDao.insertAll(characterDetails);
            return null;
        }
    }
}
