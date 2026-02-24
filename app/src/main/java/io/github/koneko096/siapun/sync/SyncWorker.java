package io.github.koneko096.siapun.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.koneko096.siapun.AppConstants;
import io.github.koneko096.siapun.Soal;
import io.github.koneko096.siapun.api.GSheetService;
import io.github.koneko096.siapun.data.AppDatabase;
import io.github.koneko096.siapun.data.SoalDao;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting sync work...");

        GSheetService service = new Retrofit.Builder()
                .baseUrl("https://docs.google.com/") // Base URL for Google Sheets
                .build()
                .create(GSheetService.class);

        try {
            Response<ResponseBody> response = service.downloadFileWithDynamicUrl(AppConstants.GSHEET_CSV_URL).execute();

            if (response.isSuccessful() && response.body() != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                String line;
                List<Soal> newSoals = new ArrayList<>();

                // Skip header row
                reader.readLine(); 

                while ((line = reader.readLine()) != null) {
                    List<String> parsedLine = parseCsvLine(line);
                    if (parsedLine.size() >= 7) { // paket, mapel, question, choice1, choice2, choice3, choice4, answer
                        try {
                            int paket = Integer.parseInt(parsedLine.get(0));
                            String mapel = parsedLine.get(1);
                            String question = parsedLine.get(2);
                            List<String> choices = Arrays.asList(
                                    parsedLine.get(3),
                                    parsedLine.get(4),
                                    parsedLine.get(5),
                                    parsedLine.get(6)
                            );
                            int answer = Integer.parseInt(parsedLine.get(7));

                            Soal soal = new Soal();
                            soal.setPaket(paket);
                            soal.setMapel(mapel);
                            soal.setQuestion(question);
                            soal.setChoices(choices);
                            soal.setAnswer(answer);
                            newSoals.add(soal);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing number in CSV line: " + line, e);
                        }
                    }
                }

                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                SoalDao soalDao = db.soalDao();

                // Clear existing questions for the synced pakets/mapels and insert new ones
                // This simplistic approach assumes the CSV is the single source of truth for all questions.
                // For more granular updates, a more complex diffing logic would be required.
                soalDao.deleteQuestions(newSoals.get(0).getPaket(), newSoals.get(0).getMapel()); // Assuming all questions in CSV are for the same paket/mapel for now
                soalDao.insertAll(newSoals);

                Log.d(TAG, "Sync work finished successfully. Inserted " + newSoals.size() + " questions.");
                return Result.success();

            } else {
                Log.e(TAG, "Failed to download CSV: " + response.code() + " " + response.message());
                return Result.retry();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during sync work", e);
            return Result.failure();
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder currentField = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                result.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString().trim());
        return result;
    }
}
