package io.github.koneko096.siapun.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.koneko096.siapun.Soal;

@Dao
public interface SoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Soal> soals);

    @Query("SELECT * FROM soal WHERE paket = :paket AND mapel = :mapel")
    LiveData<List<Soal>> getQuestions(int paket, String mapel);

    @Query("DELETE FROM soal WHERE paket = :paket AND mapel = :mapel")
    void deleteQuestions(int paket, String mapel);
}
