package io.github.koneko096.siapun;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Afrizal on 1/18/2016.
 */
public class StatisticsDbHelper extends SQLiteOpenHelper {
  // If you change the database schema, you must increment the database version.
  public static final int DATABASE_VERSION = 4;
  public static final String DATABASE_NAME = "Statistics.db";

  private static final String TEXT_TYPE = " TEXT";
  private static final String REAL_TYPE = " REAL";
  private static final String INTEGER_TYPE = " INTEGER";
  private static final String PKEY = " PRIMARY KEY";
  private static final String COMMA_SEP = ",";

  private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + StatisticsSchema.StatisticEntry.TABLE_NAME + " (" +
      StatisticsSchema.StatisticEntry._ID + INTEGER_TYPE + PKEY + COMMA_SEP +
      StatisticsSchema.StatisticEntry.COLUMN_NAME_SUBJECT + TEXT_TYPE + COMMA_SEP +
      StatisticsSchema.StatisticEntry.COLUMN_NAME_PACKAGE + INTEGER_TYPE + COMMA_SEP +
      StatisticsSchema.StatisticEntry.COLUMN_NAME_SCORE + REAL_TYPE + COMMA_SEP +
      StatisticsSchema.StatisticEntry.COLUMN_NAME_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
      " )";
  private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + StatisticsSchema.StatisticEntry.TABLE_NAME;

  public StatisticsDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    // TODO : if database changed after release, CHANGE POLICY TO AND MIGRATE
    // INSTEAD OF DROP DB
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }

  public void insertScore(String subject, int pkg, double score) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(StatisticsSchema.StatisticEntry.COLUMN_NAME_SUBJECT, subject);
    contentValues.put(StatisticsSchema.StatisticEntry.COLUMN_NAME_PACKAGE, pkg);
    contentValues.put(StatisticsSchema.StatisticEntry.COLUMN_NAME_SCORE, score);
    db.insert(StatisticsSchema.StatisticEntry.TABLE_NAME, null, contentValues);
    db.close();
  }

  public ArrayList<String> getScores(int pkg, String subject) {
    SQLiteDatabase db = this.getReadableDatabase();
    String[] projection = { StatisticsSchema.StatisticEntry.COLUMN_NAME_SCORE };
    Cursor res = db.query(
        StatisticsSchema.StatisticEntry.TABLE_NAME, // The table to query
        projection, // The columns to return
        StatisticsSchema.StatisticEntry.COLUMN_NAME_SUBJECT + " = ? AND " +
            StatisticsSchema.StatisticEntry.COLUMN_NAME_PACKAGE + " = ?", // The columns for the WHERE clause
        new String[] { subject, String.valueOf(pkg) }, // The values for the WHERE clause
        null, // don't group the rows
        null, // don't filter by row groups
        StatisticsSchema.StatisticEntry.COLUMN_NAME_CREATED + " DESC" // sort order by created time
    );
    res.moveToFirst();

    ArrayList<String> list = new ArrayList<>();
    while (!res.isAfterLast()) {
      list.add(String.valueOf(
          res.getDouble(res.getColumnIndexOrThrow(StatisticsSchema.StatisticEntry.COLUMN_NAME_SCORE))));
      res.moveToNext();
    }
    res.close();
    db.close();
    return list;
  }
}
