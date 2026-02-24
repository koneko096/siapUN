package io.github.koneko096.siapun;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import io.github.koneko096.siapun.data.QuestionRepository;

public class ExerciseViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Application application;
    private final int paket;
    private final String mapel;

    public ExerciseViewModelFactory(@NonNull Application application, int paket, String mapel) {
        this.application = application;
        this.paket = paket;
        this.mapel = mapel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExerciseViewModel.class)) {
            return (T) new ExerciseViewModel(application, paket, mapel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
