package io.github.koneko096.siapun;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.CountDownTimer;

import java.util.List;

public class ExerciseViewModel extends ViewModel {
    private MutableLiveData<List<Soal>> soals = new MutableLiveData<>();
    private MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private MutableLiveData<Long> remainingTime = new MutableLiveData<>(7200000L); // 2 hours
    private MutableLiveData<Integer[]> userSelections = new MutableLiveData<>(new Integer[50]);
    private MutableLiveData<Boolean> isQuizStarted = new MutableLiveData<>(false);

    private CountDownTimer timer;

    public LiveData<List<Soal>> getSoals() {
        return soals;
    }

    public void setSoals(List<Soal> list) {
        soals.setValue(list);
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
