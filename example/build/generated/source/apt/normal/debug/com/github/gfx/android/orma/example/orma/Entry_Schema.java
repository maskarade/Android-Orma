package com.github.gfx.android.orma.example.orma;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.core.DatabaseStatement;
import com.github.gfx.android.orma.internal.Aliases;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.List;

public class Entry_Schema implements Schema<Entry> {
  public static final Entry_Schema INSTANCE = Schemas.register(new Entry_Schema());

  @Nullable
  private final String $alias;

  public final ColumnDef<Entry, String> resourceType;

  public final ColumnDef<Entry, Long> resourceId;

  public final ColumnDef<Entry, Long> id;

  private final String[] $defaultResultColumns;

  public Entry_Schema() {
    this(null);
  }

  public Entry_Schema(@Nullable Aliases.ColumnPath current) {
    $alias = current != null ? current.getAlias() : null;
    this.id = new ColumnDef<Entry, Long>(this, "id", long.class, "INTEGER", ColumnDef.PRIMARY_KEY | ColumnDef.AUTO_VALUE) {
      @Override
      @NonNull
      public Long get(@NonNull Entry model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Entry model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getLong(index);
      }
    };
    this.resourceType = new ColumnDef<Entry, String>(this, "resourceType", String.class, "TEXT", 0) {
      @Override
      @NonNull
      public String get(@NonNull Entry model) {
        return model.resourceType;
      }

      @NonNull
      @Override
      public String getSerialized(@NonNull Entry model) {
        return model.resourceType;
      }

      @NonNull
      @Override
      public String getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getString(index);
      }
    };
    this.resourceId = new ColumnDef<Entry, Long>(this, "resourceId", long.class, "INTEGER", 0) {
      @Override
      @NonNull
      public Long get(@NonNull Entry model) {
        return model.resourceId;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Entry model) {
        return model.resourceId;
      }

      @NonNull
      @Override
      public Long getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getLong(index);
      }
    };
    $defaultResultColumns = new String[]{
          resourceType.getQualifiedName(),
          resourceId.getQualifiedName(),
          id.getQualifiedName()
        };
  }

  @NonNull
  @Override
  public Class<Entry> getModelClass() {
    return Entry.class;
  }

  @NonNull
  @Override
  public String getTableName() {
    return "Entry";
  }

  @NonNull
  @Override
  public String getEscapedTableName() {
    return "`Entry`";
  }

  @Nullable
  @Override
  public String getTableAlias() {
    return $alias;
  }

  @Nullable
  @Override
  public String getEscapedTableAlias() {
    return $alias != null ? '`' + $alias + '`' : null;
  }

  @NonNull
  @Override
  public String getSelectFromTableClause() {
    return "`Entry`"+ ($alias != null ? " AS " + '`' + $alias +  '`' : "");
  }

  @NonNull
  @Override
  public ColumnDef<Entry, Long> getPrimaryKey() {
    return id;
  }

  @NonNull
  @Override
  public List<ColumnDef<Entry, ?>> getColumns() {
    return Arrays.<ColumnDef<Entry, ?>>asList(
          resourceType,
          resourceId,
          id
        );
  }

  @NonNull
  @Override
  public String[] getDefaultResultColumns() {
    return $defaultResultColumns;
  }

  @NonNull
  @Override
  public String getCreateTableStatement() {
    return "CREATE TABLE `Entry` (`resourceType` TEXT NOT NULL, `resourceId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY)";
  }

  @NonNull
  @Override
  public List<String> getCreateIndexStatements() {
    return Arrays.asList(
      "CREATE UNIQUE INDEX `index_resourceType_resourceId_on_Entry` ON `Entry` (`resourceType`, `resourceId`)"
    );
  }

  @NonNull
  @Override
  public String getDropTableStatement() {
    return "DROP TABLE IF EXISTS `Entry`";
  }

  @NonNull
  @Override
  public String getInsertStatement(@OnConflict int onConflictAlgorithm, boolean withoutAutoId) {
    StringBuilder s = new StringBuilder();
    s.append("INSERT");
    switch (onConflictAlgorithm) {
      case OnConflict.NONE: /* nop */ break;
      case OnConflict.ABORT: s.append(" OR ABORT"); break;
      case OnConflict.FAIL: s.append(" OR FAIL"); break;
      case OnConflict.IGNORE: s.append(" OR IGNORE"); break;
      case OnConflict.REPLACE: s.append(" OR REPLACE"); break;
      case OnConflict.ROLLBACK: s.append(" OR ROLLBACK"); break;
      default: throw new IllegalArgumentException("Invalid OnConflict algorithm: " + onConflictAlgorithm);
    }
    if (withoutAutoId) {
      s.append(" INTO `Entry` (`resourceType`,`resourceId`) VALUES (?,?)");
    }
    else {
      s.append(" INTO `Entry` (`resourceType`,`resourceId`,`id`) VALUES (?,?,?)");
    }
    return s.toString();
  }

  /**
   * Convert a model to {@code ContentValues). You can use the content values for UPDATE and/or INSERT.
   */
  @NonNull
  @Override
  public ContentValues convertToContentValues(@NonNull OrmaConnection conn, @NonNull Entry model,
      boolean withoutAutoId) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("resourceType", model.resourceType);
    contentValues.put("resourceId", model.resourceId);
    if (!withoutAutoId) {
      contentValues.put("id", model.id);
    }
    return contentValues;
  }

  /**
   * Convert a model to {@code Object[]}. Provided for debugging.
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Entry model,
      boolean withoutAutoId) {
    Object[] args = new Object[withoutAutoId ? 2 : 3];
    if (model.resourceType != null) {
      args[0] = model.resourceType;
    }
    else {
      throw new IllegalArgumentException("Entry.resourceType" + " must not be null, or use @Nullable to declare it as NULL");
    }
    args[1] = model.resourceId;
    if (!withoutAutoId) {
      args[2] = model.id;
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull DatabaseStatement statement,
      @NonNull Entry model, boolean withoutAutoId) {
    statement.bindString(1, model.resourceType);
    statement.bindLong(2, model.resourceId);
    if (!withoutAutoId) {
      statement.bindLong(3, model.id);
    }
  }

  @NonNull
  @Override
  public Entry newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor,
      int offset) {
    Entry model = new Entry();
    model.resourceType = cursor.getString(offset + 0);
    model.resourceId = cursor.getLong(offset + 1);
    model.id = cursor.getLong(offset + 2);
    return model;
  }
}
