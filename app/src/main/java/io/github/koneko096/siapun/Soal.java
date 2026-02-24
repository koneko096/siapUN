package io.github.koneko096.siapun;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Afrizal on 1/8/2016.
 */
@Entity(tableName = "soal")
public class Soal {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private int paket;
  private String mapel;
  private String question;
  private List<String> choices;
  private int answer;

  public Soal() {
    // Required for Room
  }

  @Ignore
  public Soal(JSONObject soalJSON) {
    List<String> choices = new LinkedList<>();
    JSONArray choicesJSON = null;
    try {
      choicesJSON = soalJSON.getJSONArray("choices");
      for (int j = 0; j < choicesJSON.length(); ++j) {
        choices.add(choicesJSON.getString(j));
      }
      this.answer = soalJSON.getInt("answer");
      this.question = soalJSON.getString("question");
      this.choices = choices;
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPaket() {
    return paket;
  }

  public void setPaket(int paket) {
    this.paket = paket;
  }

  public String getMapel() {
    return mapel;
  }

  public void setMapel(String mapel) {
    this.mapel = mapel;
  }

  public String getQuestion() {
    return question;
  }

  public List<String> getChoices() {
    return choices;
  }

  public int getAnswer() {
    return answer;
  }

  public void setQuestion(String question) { this.question = question;  }

  public void setChoices(List<String> choices) { this.choices = choices; }

  public void setAnswer(int answer) { this.answer = answer; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Soal)) return false;

    Soal soal = (Soal) o;

    if (getAnswer() != soal.getAnswer()) return false;
    if (!getQuestion().equals(soal.getQuestion())) return false;
    return getChoices().equals(soal.getChoices());

  }

  @Override
  public int hashCode() {
    int result = getQuestion().hashCode();
    result = 31 * result + getChoices().hashCode();
    result = 31 * result + getAnswer();
    return result;
  }

  @Override
  public String toString() {
    JSONObject soalJSON = new JSONObject();
    try {
      JSONArray pilihan = new JSONArray();
      for (String q: choices) { pilihan.put(q); }

      soalJSON.put("answer", answer);
      soalJSON.put("question", question);
      soalJSON.put("choices", pilihan);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return soalJSON.toString();
  }
}
