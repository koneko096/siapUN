package io.github.koneko096.siapun;

import android.provider.BaseColumns;

/**
 * Created by Afrizal on 1/18/2016.
 */
public class StatisticsSchema {

  public StatisticsSchema() {}

  /* Inner class that defines the table contents */
  public static abstract class StatisticEntry implements BaseColumns {
    public static final String TABLE_NAME = "entry";
    public static final String COLUMN_NAME_SUBJECT = "subject";
    public static final String COLUMN_NAME_PACKAGE = "package";
    public static final String COLUMN_NAME_SCORE = "score";
    public static final String COLUMN_NAME_CREATED = "created_at";
  }

}
