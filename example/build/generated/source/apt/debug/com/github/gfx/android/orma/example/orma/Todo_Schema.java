package com.github.gfx.android.orma.example.orma;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.internal.Aliases;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Todo_Schema implements Schema<Todo> {
  public static final Todo_Schema INSTANCE = Schemas.register(new Todo_Schema());

  @Nullable
  private final String $alias;

  public final ColumnDef<Todo, String> title;

  public final ColumnDef<Todo, String> content;

  public final ColumnDef<Todo, Boolean> done;

  public final ColumnDef<Todo, Date> createdTime;

  public final ColumnDef<Todo, Long> id;

  private final String[] $defaultResultColumns;

  public Todo_Schema() {
    this(null);
  }

  public Todo_Schema(@Nullable Aliases.ColumnPath current) {
    $alias = current != null ? current.getAlias() : null;
    this.id = new ColumnDef<Todo, Long>(this, "id", long.class, "INTEGER", ColumnDef.PRIMARY_KEY | ColumnDef.AUTO_VALUE) {
      @Override
      @NonNull
      public Long get(@NonNull Todo model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Todo model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getLong(index);
      }
    };
    this.title = new ColumnDef<Todo, String>(this, "title", String.class, "TEXT", ColumnDef.INDEXED) {
      @Override
      @NonNull
      public String get(@NonNull Todo model) {
        return model.title;
      }

      @NonNull
      @Override
      public String getSerialized(@NonNull Todo model) {
        return model.title;
      }

      @NonNull
      @Override
      public String getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getString(index);
      }
    };
    this.content = new ColumnDef<Todo, String>(this, "content", String.class, "TEXT", ColumnDef.NULLABLE) {
      @Override
      @Nullable
      public String get(@NonNull Todo model) {
        return model.content;
      }

      @Nullable
      @Override
      public String getSerialized(@NonNull Todo model) {
        return model.content;
      }

      @Nullable
      @Override
      public String getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getString(index);
      }
    };
    this.done = new ColumnDef<Todo, Boolean>(this, "done", boolean.class, "BOOLEAN", ColumnDef.INDEXED) {
      @Override
      @NonNull
      public Boolean get(@NonNull Todo model) {
        return model.done;
      }

      @NonNull
      @Override
      public Boolean getSerialized(@NonNull Todo model) {
        return model.done;
      }

      @NonNull
      @Override
      public Boolean getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor,
          int index) {
        return cursor.getLong(index) != 0;
      }
    };
    this.createdTime = new ColumnDef<Todo, Date>(this, "createdTime", Date.class, "INTEGER", ColumnDef.INDEXED) {
      @Override
      @NonNull
      public Date get(@NonNull Todo model) {
        return model.createdTime;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Todo model) {
        return BuiltInSerializers.serializeDate(model.createdTime);
      }

      @NonNull
      @Override
      public Date getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return BuiltInSerializers.deserializeDate(cursor.getLong(index));
      }
    };
    $defaultResultColumns = new String[]{
          title.getQualifiedName(),
          content.getQualifiedName(),
          done.getQualifiedName(),
          createdTime.getQualifiedName(),
          id.getQualifiedName()
        };
  }

  @NonNull
  @Override
  public Class<Todo> getModelClass() {
    return Todo.class;
  }

  @NonNull
  @Override
  public String getTableName() {
    return "Todo";
  }

  @NonNull
  @Override
  public String getEscapedTableName() {
    return "`Todo`";
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
    return "`Todo`"+ ($alias != null ? " AS " + '`' + $alias +  '`' : "");
  }

  @NonNull
  @Override
  public ColumnDef<Todo, Long> getPrimaryKey() {
    return id;
  }

  @NonNull
  @Override
  public List<ColumnDef<Todo, ?>> getColumns() {
    return Arrays.<ColumnDef<Todo, ?>>asList(
          title,
          content,
          done,
          createdTime,
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
    return "CREATE TABLE `Todo` (`title` TEXT NOT NULL, `content` TEXT , `done` BOOLEAN NOT NULL DEFAULT 0, `createdTime` INTEGER NOT NULL DEFAULT 0, `id` INTEGER PRIMARY KEY)";
  }

  @NonNull
  @Override
  public List<String> getCreateIndexStatements() {
    return Arrays.asList(
      "CREATE INDEX `index_title_on_Todo` ON `Todo` (`title`)",
      "CREATE INDEX `index_done_on_Todo` ON `Todo` (`done`)",
      "CREATE INDEX `index_createdTime_on_Todo` ON `Todo` (`createdTime`)"
    );
  }

  @NonNull
  @Override
  public String getDropTableStatement() {
    return "DROP TABLE IF EXISTS `Todo`";
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
      s.append(" INTO `Todo` (`title`,`content`,`done`,`createdTime`) VALUES (?,?,?,?)");
    }
    else {
      s.append(" INTO `Todo` (`title`,`content`,`done`,`createdTime`,`id`) VALUES (?,?,?,?,?)");
    }
    return s.toString();
  }

  /**
   * Convert models to {@code Object[]}. Provided for debugging
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Todo model,
      boolean withoutAutoId) {
    Object[] args = new Object[withoutAutoId ? 4 : 5];
    if (model.title != null) {
      args[0] = model.title;
    }
    else {
      throw new IllegalArgumentException("Todo.title" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (model.content != null) {
      args[1] = model.content;
    }
    args[2] = model.done ? 1 : 0;
    if (model.createdTime != null) {
      args[3] = BuiltInSerializers.serializeDate(model.createdTime);
    }
    else {
      throw new IllegalArgumentException("Todo.createdTime" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (!withoutAutoId) {
      args[4] = model.id;
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement,
      @NonNull Todo model, boolean withoutAutoId) {
    statement.bindString(1, model.title);
    if (model.content != null) {
      statement.bindString(2, model.content);
    }
    else {
      statement.bindNull(2);
    }
    statement.bindLong(3, model.done ? 1 : 0);
    statement.bindLong(4, BuiltInSerializers.serializeDate(model.createdTime));
    if (!withoutAutoId) {
      statement.bindLong(5, model.id);
    }
  }

  @NonNull
  @Override
  public Todo newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int offset) {
    Todo model = new Todo();
    model.title = cursor.getString(offset + 0);
    model.content = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
    model.done = cursor.getLong(offset + 2) != 0;
    model.createdTime = BuiltInSerializers.deserializeDate(cursor.getLong(offset + 3));
    model.id = cursor.getLong(offset + 4);
    return model;
  }
}
