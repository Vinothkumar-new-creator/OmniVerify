package com.omniverify;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class ScanHistoryDao_Impl implements ScanHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<ScanHistory> __insertAdapterOfScanHistory;

  public ScanHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfScanHistory = new EntityInsertAdapter<ScanHistory>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `scan_history` (`id`,`scanType`,`rawContent`,`verdict`,`confidence`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ScanHistory entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getScanType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getScanType());
        }
        if (entity.getRawContent() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getRawContent());
        }
        if (entity.getVerdict() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getVerdict());
        }
        statement.bindLong(5, entity.getConfidence());
        statement.bindLong(6, entity.getTimestamp());
      }
    };
  }

  @Override
  public Object insert(final ScanHistory scan, final Continuation<? super Unit> $completion) {
    if (scan == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfScanHistory.insert(_connection, scan);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<ScanHistory>> getAllScans() {
    final String _sql = "SELECT * FROM scan_history ORDER BY timestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"scan_history"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfScanType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "scanType");
        final int _columnIndexOfRawContent = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rawContent");
        final int _columnIndexOfVerdict = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "verdict");
        final int _columnIndexOfConfidence = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "confidence");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final List<ScanHistory> _result = new ArrayList<ScanHistory>();
        while (_stmt.step()) {
          final ScanHistory _item;
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          final String _tmpScanType;
          if (_stmt.isNull(_columnIndexOfScanType)) {
            _tmpScanType = null;
          } else {
            _tmpScanType = _stmt.getText(_columnIndexOfScanType);
          }
          final String _tmpRawContent;
          if (_stmt.isNull(_columnIndexOfRawContent)) {
            _tmpRawContent = null;
          } else {
            _tmpRawContent = _stmt.getText(_columnIndexOfRawContent);
          }
          final String _tmpVerdict;
          if (_stmt.isNull(_columnIndexOfVerdict)) {
            _tmpVerdict = null;
          } else {
            _tmpVerdict = _stmt.getText(_columnIndexOfVerdict);
          }
          final int _tmpConfidence;
          _tmpConfidence = (int) (_stmt.getLong(_columnIndexOfConfidence));
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          _item = new ScanHistory(_tmpId,_tmpScanType,_tmpRawContent,_tmpVerdict,_tmpConfidence,_tmpTimestamp);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM scan_history";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
