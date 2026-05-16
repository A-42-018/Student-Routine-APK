package com.alif.studentroutine.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.alif.studentroutine.data.entity.ClassItem;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ClassItemDao_Impl implements ClassItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ClassItem> __insertionAdapterOfClassItem;

  private final EntityDeletionOrUpdateAdapter<ClassItem> __deletionAdapterOfClassItem;

  private final EntityDeletionOrUpdateAdapter<ClassItem> __updateAdapterOfClassItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllClasses;

  public ClassItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfClassItem = new EntityInsertionAdapter<ClassItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `classes` (`id`,`subject`,`dayOfWeek`,`startTimeHour`,`startTimeMinute`,`endTimeHour`,`endTimeMinute`,`room`,`reminderMinutes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClassItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSubject());
        statement.bindLong(3, entity.getDayOfWeek());
        statement.bindLong(4, entity.getStartTimeHour());
        statement.bindLong(5, entity.getStartTimeMinute());
        statement.bindLong(6, entity.getEndTimeHour());
        statement.bindLong(7, entity.getEndTimeMinute());
        statement.bindString(8, entity.getRoom());
        statement.bindLong(9, entity.getReminderMinutes());
      }
    };
    this.__deletionAdapterOfClassItem = new EntityDeletionOrUpdateAdapter<ClassItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `classes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClassItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfClassItem = new EntityDeletionOrUpdateAdapter<ClassItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `classes` SET `id` = ?,`subject` = ?,`dayOfWeek` = ?,`startTimeHour` = ?,`startTimeMinute` = ?,`endTimeHour` = ?,`endTimeMinute` = ?,`room` = ?,`reminderMinutes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClassItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSubject());
        statement.bindLong(3, entity.getDayOfWeek());
        statement.bindLong(4, entity.getStartTimeHour());
        statement.bindLong(5, entity.getStartTimeMinute());
        statement.bindLong(6, entity.getEndTimeHour());
        statement.bindLong(7, entity.getEndTimeMinute());
        statement.bindString(8, entity.getRoom());
        statement.bindLong(9, entity.getReminderMinutes());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllClasses = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM classes";
        return _query;
      }
    };
  }

  @Override
  public Object insertClass(final ClassItem classItem,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfClassItem.insertAndReturnId(classItem);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteClass(final ClassItem classItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfClassItem.handle(classItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateClass(final ClassItem classItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfClassItem.handle(classItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllClasses(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllClasses.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllClasses.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ClassItem>> getAllClasses() {
    final String _sql = "SELECT * FROM classes ORDER BY dayOfWeek, startTimeHour, startTimeMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"classes"}, new Callable<List<ClassItem>>() {
      @Override
      @NonNull
      public List<ClassItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeHour");
          final int _cursorIndexOfStartTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinute");
          final int _cursorIndexOfEndTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeHour");
          final int _cursorIndexOfEndTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinute");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfReminderMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutes");
          final List<ClassItem> _result = new ArrayList<ClassItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClassItem _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartTimeHour;
            _tmpStartTimeHour = _cursor.getInt(_cursorIndexOfStartTimeHour);
            final int _tmpStartTimeMinute;
            _tmpStartTimeMinute = _cursor.getInt(_cursorIndexOfStartTimeMinute);
            final int _tmpEndTimeHour;
            _tmpEndTimeHour = _cursor.getInt(_cursorIndexOfEndTimeHour);
            final int _tmpEndTimeMinute;
            _tmpEndTimeMinute = _cursor.getInt(_cursorIndexOfEndTimeMinute);
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final int _tmpReminderMinutes;
            _tmpReminderMinutes = _cursor.getInt(_cursorIndexOfReminderMinutes);
            _item = new ClassItem(_tmpId,_tmpSubject,_tmpDayOfWeek,_tmpStartTimeHour,_tmpStartTimeMinute,_tmpEndTimeHour,_tmpEndTimeMinute,_tmpRoom,_tmpReminderMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllClassesList(final Continuation<? super List<ClassItem>> $completion) {
    final String _sql = "SELECT * FROM classes ORDER BY dayOfWeek, startTimeHour, startTimeMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ClassItem>>() {
      @Override
      @NonNull
      public List<ClassItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeHour");
          final int _cursorIndexOfStartTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinute");
          final int _cursorIndexOfEndTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeHour");
          final int _cursorIndexOfEndTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinute");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfReminderMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutes");
          final List<ClassItem> _result = new ArrayList<ClassItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClassItem _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartTimeHour;
            _tmpStartTimeHour = _cursor.getInt(_cursorIndexOfStartTimeHour);
            final int _tmpStartTimeMinute;
            _tmpStartTimeMinute = _cursor.getInt(_cursorIndexOfStartTimeMinute);
            final int _tmpEndTimeHour;
            _tmpEndTimeHour = _cursor.getInt(_cursorIndexOfEndTimeHour);
            final int _tmpEndTimeMinute;
            _tmpEndTimeMinute = _cursor.getInt(_cursorIndexOfEndTimeMinute);
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final int _tmpReminderMinutes;
            _tmpReminderMinutes = _cursor.getInt(_cursorIndexOfReminderMinutes);
            _item = new ClassItem(_tmpId,_tmpSubject,_tmpDayOfWeek,_tmpStartTimeHour,_tmpStartTimeMinute,_tmpEndTimeHour,_tmpEndTimeMinute,_tmpRoom,_tmpReminderMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ClassItem>> getClassesByDay(final int day) {
    final String _sql = "SELECT * FROM classes WHERE dayOfWeek = ? ORDER BY startTimeHour, startTimeMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"classes"}, new Callable<List<ClassItem>>() {
      @Override
      @NonNull
      public List<ClassItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeHour");
          final int _cursorIndexOfStartTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinute");
          final int _cursorIndexOfEndTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeHour");
          final int _cursorIndexOfEndTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinute");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfReminderMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutes");
          final List<ClassItem> _result = new ArrayList<ClassItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClassItem _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartTimeHour;
            _tmpStartTimeHour = _cursor.getInt(_cursorIndexOfStartTimeHour);
            final int _tmpStartTimeMinute;
            _tmpStartTimeMinute = _cursor.getInt(_cursorIndexOfStartTimeMinute);
            final int _tmpEndTimeHour;
            _tmpEndTimeHour = _cursor.getInt(_cursorIndexOfEndTimeHour);
            final int _tmpEndTimeMinute;
            _tmpEndTimeMinute = _cursor.getInt(_cursorIndexOfEndTimeMinute);
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final int _tmpReminderMinutes;
            _tmpReminderMinutes = _cursor.getInt(_cursorIndexOfReminderMinutes);
            _item = new ClassItem(_tmpId,_tmpSubject,_tmpDayOfWeek,_tmpStartTimeHour,_tmpStartTimeMinute,_tmpEndTimeHour,_tmpEndTimeMinute,_tmpRoom,_tmpReminderMinutes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getClassById(final int id, final Continuation<? super ClassItem> $completion) {
    final String _sql = "SELECT * FROM classes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ClassItem>() {
      @Override
      @Nullable
      public ClassItem call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeHour");
          final int _cursorIndexOfStartTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinute");
          final int _cursorIndexOfEndTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeHour");
          final int _cursorIndexOfEndTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinute");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfReminderMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutes");
          final ClassItem _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSubject;
            _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartTimeHour;
            _tmpStartTimeHour = _cursor.getInt(_cursorIndexOfStartTimeHour);
            final int _tmpStartTimeMinute;
            _tmpStartTimeMinute = _cursor.getInt(_cursorIndexOfStartTimeMinute);
            final int _tmpEndTimeHour;
            _tmpEndTimeHour = _cursor.getInt(_cursorIndexOfEndTimeHour);
            final int _tmpEndTimeMinute;
            _tmpEndTimeMinute = _cursor.getInt(_cursorIndexOfEndTimeMinute);
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final int _tmpReminderMinutes;
            _tmpReminderMinutes = _cursor.getInt(_cursorIndexOfReminderMinutes);
            _result = new ClassItem(_tmpId,_tmpSubject,_tmpDayOfWeek,_tmpStartTimeHour,_tmpStartTimeMinute,_tmpEndTimeHour,_tmpEndTimeMinute,_tmpRoom,_tmpReminderMinutes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
