package io.github.koneko096.siapun;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.koneko096.siapun.databinding.FragmentExerciseBinding;

public class ExerciseFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentExerciseBinding binding;
    private ExerciseViewModel viewModel;

    private int paket;
    private String mapel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            paket = getArguments().getInt(AppConstants.KEY_PAKET);
            mapel = getArguments().getString(AppConstants.KEY_MAPEL);
        }

        ExerciseViewModelFactory factory = new ExerciseViewModelFactory(requireActivity().getApplication(), paket, mapel);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ExerciseViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsQuizStarted().observe(getViewLifecycleOwner(), started -> {
            if (started) {
                binding.layoutInit.setVisibility(View.GONE);
                binding.layoutResume.setVisibility(View.GONE);
                binding.layoutPage.setVisibility(View.VISIBLE);
                setupQuizUI();
            } else {
                binding.layoutInit.setVisibility(View.VISIBLE);
                binding.layoutPage.setVisibility(View.GONE);
                binding.layoutResume.setVisibility(View.GONE);
            }
        });

        viewModel.getRemainingTime().observe(getViewLifecycleOwner(), this::updateTimerUI);
        viewModel.getCurrentQuestionIndex().observe(getViewLifecycleOwner(), this::updateQuestionUI);
        viewModel.getSoals().observe(getViewLifecycleOwner(), soals -> {
            if (soals != null && !soals.isEmpty()) {
                setupQuizUI(); // Refresh UI if questions are loaded/updated
            }
        });
    }

    private void setupListeners() {
        binding.ready.setOnClickListener(v -> viewModel.startQuiz());
        binding.cancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.resumeButton.setOnClickListener(v -> viewModel.startQuiz());
        binding.button.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.left.setOnClickListener(v -> navigateQuestion(-1));
        binding.right.setOnClickListener(v -> navigateQuestion(1));
        binding.done.setOnClickListener(v -> finishExercise());

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checked = group.findViewById(checkedId);
            if (checked != null) {
                int index = group.indexOfChild(checked);
                viewModel.setSelection(viewModel.getCurrentQuestionIndex().getValue(), index);
            }
        });
    }

    private void setupQuizUI() {
        List<Soal> soals = viewModel.getSoals().getValue();
        if (soals == null)
            return;

        ArrayList<String> options = new ArrayList<>();
        for (int i = 0; i < soals.size(); ++i) {
            options.add("Soal " + (i + 1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                options);
        binding.noSoal.setAdapter(adapter);
        binding.noSoal.setOnItemSelectedListener(this);

        updateQuestionUI(viewModel.getCurrentQuestionIndex().getValue()); // Update UI with current question
    }

    private void updateQuestionUI(int index) {
        List<Soal> soals = viewModel.getSoals().getValue();
        if (soals == null || index < 0 || index >= soals.size())
            return;

        Soal soal = soals.get(index);
        binding.noSoal.setSelection(index);

        class ImageHandler implements Html.ImageGetter {
            @Override
            public Drawable getDrawable(String source) {
                try {
                    Drawable pics = Drawable.createFromStream(requireContext().getAssets().open(source), null);
                    int width = binding.question.getWidth();
                    if (width > 0 && pics.getIntrinsicWidth() > 0) {
                        int height = pics.getIntrinsicHeight() * width / pics.getIntrinsicWidth();
                        pics.setBounds(0, 0, width, height);
                    }
                    return pics;
                } catch (IOException e) {
                    return null;
                }
            }
        }

        ImageHandler handler = new ImageHandler();
        binding.question
                .setText(Html.fromHtml(soal.getQuestion(), Html.FROM_HTML_MODE_COMPACT, handler, null));
        binding.choice1
                .setText(Html.fromHtml(soal.getChoices().get(0), Html.FROM_HTML_MODE_COMPACT, handler, null));
        binding.choice2
                .setText(Html.fromHtml(soal.getChoices().get(1), Html.FROM_HTML_MODE_COMPACT, handler, null));
        binding.choice3
                .setText(Html.fromHtml(soal.getChoices().get(2), Html.FROM_HTML_MODE_COMPACT, handler, null));
        binding.choice4
                .setText(Html.fromHtml(soal.getChoices().get(3), Html.FROM_HTML_MODE_COMPACT, handler, null));

        // Restore selection
        Integer choice = viewModel.getUserSelections().getValue()[index];
        binding.radioGroup.clearCheck();
        if (choice != null) {
            RadioButton rb = (RadioButton) binding.radioGroup.getChildAt(choice);
            if (rb != null)
                rb.setChecked(true);
        }
    }

    private void updateTimerUI(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        binding.timerText.setText(String.format("%02d : %02d : %02d", h, m, s));
    }

    private void navigateQuestion(int direction) {
        int next = viewModel.getCurrentQuestionIndex().getValue() + direction;
        List<Soal> soals = viewModel.getSoals().getValue();
        if (soals != null && next >= 0 && next < soals.size()) {
            viewModel.setCurrentQuestionIndex(next);
        }
    }

    private void finishExercise() {
        new AlertDialog.Builder(requireContext())
                .setMessage("Selesaikan latihan?")
                .setPositiveButton("Ya", (d, id) -> calculateAndShowResult())
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void calculateAndShowResult() {
        List<Soal> soals = viewModel.getSoals().getValue();
        Integer[] selections = viewModel.getUserSelections().getValue();
        int score = 0;
        if (soals != null) {
            for (int i = 0; i < soals.size(); i++) {
                if (selections[i] != null && selections[i] == soals.get(i).getAnswer()) {
                    score += 100;
                }
            }
        }

        Bundle args = new Bundle();
        args.putFloat("score", (float) score / (soals != null ? soals.size() : 1)); // Prevent division by zero
        args.putInt("paket", paket);
        args.putString("mapel", mapel);
        Navigation.findNavController(requireView()).navigate(R.id.action_exerciseFragment_to_resultFragment, args);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setCurrentQuestionIndex(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
