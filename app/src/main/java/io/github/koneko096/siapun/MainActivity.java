package io.github.koneko096.siapun;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.github.koneko096.siapun.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
