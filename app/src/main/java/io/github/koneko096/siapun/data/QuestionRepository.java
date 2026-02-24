package io.github.koneko096.siapun.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.koneko096.siapun.Soal;
import io.github.koneko096.siapun.sync.SyncWorker;

public class QuestionRepository {
    private static final String TAG = "QuestionRepository";
    private SoalDao soalDao;
    private WorkManager workManager;
    private Context context;
    private final ExecutorService diskIoExecutor = Executors.newFixedThreadPool(2); // For disk operations

    public QuestionRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(this.context);
        soalDao = db.soalDao();
        workManager = WorkManager.getInstance(this.context);
    }

    public LiveData<List<Soal>> getQuestions(int paket, String mapel) {
        // Load from assets if database is empty for initial run
        diskIoExecutor.execute(() -> {
            if (soalDao.getQuestions(paket, mapel).getValue() == null || soalDao.getQuestions(paket, mapel).getValue().isEmpty()) {
                List<Soal> assetSoals = loadQuestionsFromAssets(paket, mapel);
                if (!assetSoals.isEmpty()) {
                    soalDao.insertAll(assetSoals);
                }
            }
        });

        return soalDao.getQuestions(paket, mapel);
    }

    public void scheduleSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniquePeriodicWork("QuestionSync", androidx.work.ExistingPeriodicWorkPolicy.KEEP, syncRequest);
        Log.d(TAG, "Question sync scheduled.");
    }

    private List<Soal> loadQuestionsFromAssets(int paket, String mapel) {
        List<Soal> list = new ArrayList<>();
        String fileDir = "questions/v" + paket + "/" + mapel + ".json";
        AssetManager assetManager = context.getAssets();
        try (InputStream is = assetManager.open(fileDir);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                Soal soal = new Soal(array.getJSONObject(i));
                soal.setPaket(paket);
                soal.setMapel(mapel);
                list.add(soal);
            }
            Log.d(TAG, "Loaded " + list.size() + " questions from assets for paket " + paket + ", mapel " + mapel);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading questions from assets: " + fileDir, e);
        }
        return list;
    }
}
