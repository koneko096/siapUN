package io.github.koneko096.siapun;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.github.koneko096.siapun.data.QuestionRepository;

public class ExerciseViewModel extends AndroidViewModel {
    private QuestionRepository repository;
    private LiveData<List<Soal>> soals;
    private MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private MutableLiveData<Long> remainingTime = new MutableLiveData<>(7200000L); // 2 hours
    private MutableLiveData<Integer[]> userSelections = new MutableLiveData<>(new Integer[50]);
    private MutableLiveData<Boolean> isQuizStarted = new MutableLiveData<>(false);

    private CountDownTimer timer;

    public ExerciseViewModel(Application application, int paket, String mapel) {
        super(application);
        repository = new QuestionRepository(application);
        soals = repository.getQuestions(paket, mapel);
        repository.scheduleSync(); // Schedule sync when ViewModel is created
    }

    public LiveData<List<Soal>> getSoals() {
        return soals;
    }

    public void setSoals(List<Soal> list) {
        // This method is no longer needed as questions are loaded via repository
        // soals.setValue(list);
    }

    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int index) {
        currentQuestionIndex.setValue(index);
    }

    public LiveData<Long> getRemainingTime() {
        return remainingTime;
    }

    public LiveData<Integer[]> getUserSelections() {
        return userSelections;
    }

    public LiveData<Boolean> getIsQuizStarted() {
        return isQuizStarted;
    }

    public void startQuiz() {
        isQuizStarted.setValue(true);
        startTimer();
    }

    private void startTimer() {
        if (timer != null)
            timer.cancel();
        timer = new CountDownTimer(remainingTime.getValue(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingTime.setValue(0L);
                // Handle timeout if needed
            }
        }.start();
    }

    public void setSelection(int questionIndex, int choiceIndex) {
        Integer[] current = userSelections.getValue();
        if (current != null && questionIndex < current.length) {
            current[questionIndex] = choiceIndex;
            userSelections.setValue(current);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null)
            timer.cancel();
    }
}
