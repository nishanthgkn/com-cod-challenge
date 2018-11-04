package xfinity.com.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import xfinity.com.R;
import xfinity.com.network.XfinVolleyClient;

public class CharacterRepository {
    private static final String XFIN_LAST_SYNC_TIMESTAMP_KEY = "last_sync_date";
    private static final String XFIN_RELATED_ITEMS_KEY = "RelatedTopics";
    private static final String XFIN_TEXT_KEY = "Text";
    private static final String XFIN_ICON_KEY = "Icon";
    private static final String XFIN_URL_KEY = "URL";
    private static long sLastSyncTimeInMillis;
    private CharacterDao mCharacterDao;
    private LiveData<List<CharacterDetails>> mAllCharacters;

    public CharacterRepository(Application application) {
        CharacterDatabase db = CharacterDatabase.getDatabase(application);
        mCharacterDao = db.characterDao();
        mAllCharacters = mCharacterDao.getAllCharacters();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application);
        sLastSyncTimeInMillis = sp.getLong(XFIN_LAST_SYNC_TIMESTAMP_KEY, 0L);
        if (dataOutOfSync()) {
            // fetch and Refresh
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    application.getResources().getString(R.string.data_api),
                    null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            CharacterDetails[] characterDetails = parseCharacterResponse(response);
                            insert(characterDetails);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error

                }
            });
            XfinVolleyClient.getInstance(application).addToRequestQueue(jsonObjectRequest);
        }
    }

    public LiveData<List<CharacterDetails>> getAllCharacters() {
        return mAllCharacters;
    }

    public CharacterDetails getCharacterById(long id) {
        return mCharacterDao.findById(id);
    }

    public List<CharacterDetails> getCharacterForQuery(String query) {
        return mCharacterDao.findCharacterForQuery("%" + query + "%");
    }

    public void insert(CharacterDetails... characterDetails) {
        new InsertAsyncTask(mCharacterDao).execute(characterDetails);
    }

    private CharacterDetails[] parseCharacterResponse(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        try {
            JSONArray relatedTopics = jsonObject.getJSONArray(XFIN_RELATED_ITEMS_KEY);
            if (relatedTopics != null) {
                int numCharacters = relatedTopics.length();
                if (numCharacters < 1) return null;
                CharacterDetails[] characterDetails = new CharacterDetails[numCharacters];
                for (int i = 0; i < numCharacters; i++) {
                    JSONObject current = relatedTopics.getJSONObject(i);
                    String description = current.getString(XFIN_TEXT_KEY);
                    String name = "";
                    String desc = "";
                    if (description != null && !description.isEmpty()) {
                        String[] tuples = description.split("-", 2);
                        if (tuples.length > 1) {
                            name = tuples[0];
                            desc = tuples[1];
                        }
                    }
                    CharacterDetails characterDetail = new CharacterDetails();
                    characterDetail.setName(name);
                    characterDetail.setDescription(desc);
                    JSONObject object = current.getJSONObject(XFIN_ICON_KEY);
                    characterDetail.setImageUrl(object.getString(XFIN_URL_KEY));
                    characterDetails[i] = characterDetail;
                }

                return characterDetails;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean dataOutOfSync() {
        if (sLastSyncTimeInMillis < 1L) return true;
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        return (TimeUnit.MILLISECONDS.toDays(currentTimeInMillis - sLastSyncTimeInMillis) > 29L);
    }

    private static class InsertAsyncTask extends AsyncTask<CharacterDetails, Void, Void> {

        private CharacterDao mAsyncTaskDao;

        InsertAsyncTask(CharacterDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CharacterDetails... characterDetails) {
            mAsyncTaskDao.insertAll(characterDetails);
            return null;
        }
    }
}
