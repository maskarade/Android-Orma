package com.github.gfx.android.orma.example.orma;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.internal.Aliases;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Category_Schema implements Schema<Category> {
  public static final Category_Schema INSTANCE = Schemas.register(new Category_Schema());

  @Nullable
  private final String $alias;

  public final ColumnDef<Category, String> name;

  public final ColumnDef<Category, Long> id;

  private final String[] $defaultResultColumns;

  public Category_Schema() {
    this(null);
  }

  public Category_Schema(@Nullable Aliases.ColumnPath current) {
    $alias = current != null ? current.getAlias() : null;
    this.id = new ColumnDef<Category, Long>(this, "id", long.class, "INTEGER", ColumnDef.PRIMARY_KEY | ColumnDef.AUTO_VALUE) {
      @Override
      @NonNull
      public Long get(@NonNull Category model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Category model) {
        return model.id;
      }

      @NonNull
      @Override
      public Long getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getLong(index);
      }
    };
    this.name = new ColumnDef<Category, String>(this, "name", String.class, "TEXT", ColumnDef.UNIQUE) {
      @Override
      @NonNull
      public String get(@NonNull Category model) {
        return model.name;
      }

      @NonNull
      @Override
      public String getSerialized(@NonNull Category model) {
        return model.name;
      }

      @NonNull
      @Override
      public String getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getString(index);
      }
    };
    $defaultResultColumns = new String[]{
          name.getQualifiedName(),
          id.getQualifiedName()
        };
  }

  @NonNull
  @Override
  public Class<Category> getModelClass() {
    return Category.class;
  }

  @NonNull
  @Override
  public String getTableName() {
    return "Category";
  }

  @NonNull
  @Override
  public String getEscapedTableName() {
    return "`Category`";
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
    return "`Category`";
  }

  @NonNull
  @Override
  public ColumnDef<Category, ?> getPrimaryKey() {
    return id;
  }

  @NonNull
  @Override
  public List<ColumnDef<Category, ?>> getColumns() {
    return Arrays.<ColumnDef<Category, ?>>asList(
          name,
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
    return "CREATE TABLE `Category` (`name` TEXT UNIQUE ON CONFLICT IGNORE NOT NULL, `id` INTEGER PRIMARY KEY)";
  }

  @NonNull
  @Override
  public List<String> getCreateIndexStatements() {
    return Collections.emptyList();
  }

  @NonNull
  @Override
  public String getDropTableStatement() {
    return "DROP TABLE IF EXISTS `Category`";
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
      s.append(" INTO `Category` (`name`) VALUES (?)");
    }
    else {
      s.append(" INTO `Category` (`name`,`id`) VALUES (?,?)");
    }
    return s.toString();
  }

  /**
   * Convert models to {@code Object[]}. Provided for debugging
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Category model,
      boolean withoutAutoId) {
    Object[] args = new Object[withoutAutoId ? 1 : 2];
    if (model.name != null) {
      args[0] = model.name;
    }
    else {
      throw new IllegalArgumentException("Category.name" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (!withoutAutoId) {
      args[1] = model.id;
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement,
      @NonNull Category model, boolean withoutAutoId) {
    statement.bindString(1, model.name);
    if (!withoutAutoId) {
      statement.bindLong(2, model.id);
    }
  }

  @NonNull
  @Override
  public Category newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor,
      int offset) {
    String name = cursor.getString(offset + 0);
    long id = cursor.getLong(offset + 1);
    return new Category(id, name);
  }
}
