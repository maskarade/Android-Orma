package com.github.gfx.android.orma.example.orma;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Category_Schema implements Schema<Category> {
  public static final Category_Schema INSTANCE = Schemas.register(new Category_Schema());

  public final String alias;

  public final ColumnDef<Category, String> name = new ColumnDef<Category, String>(this, "name", String.class, "TEXT", ColumnDef.UNIQUE) {
    @Override
    @NonNull
    public String get(@NonNull Category model) {
      return model.name;
    }

    @Override
    @NonNull
    public String getSerialized(@NonNull Category model) {
      return model.name;
    }
  };

  public final ColumnDef<Category, Long> id = new ColumnDef<Category, Long>(this, "id", long.class, "INTEGER", ColumnDef.PRIMARY_KEY | ColumnDef.AUTO_VALUE) {
    @Override
    @NonNull
    public Long get(@NonNull Category model) {
      return model.id;
    }

    @Override
    @NonNull
    public Long getSerialized(@NonNull Category model) {
      return model.id;
    }
  };

  final List<ColumnDef<Category, ?>> $COLUMNS = Arrays.<ColumnDef<Category, ?>>asList(
    name,
    id
  );

  final String[] $DEFAULT_RESULT_COLUMNS = {
    "`name`",
    "`id`"
  };

  Category_Schema(@NonNull String alias) {
    this.alias = alias;
  }

  Category_Schema() {
    this("Category");
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

  @NonNull
  @Override
  public String getTableAlias() {
    return alias;
  }

  @NonNull
  @Override
  public String getEscapedTableAlias() {
    return '`' + alias + '`';
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
    return $COLUMNS;
  }

  @NonNull
  @Override
  public String[] getDefaultResultColumns() {
    return $DEFAULT_RESULT_COLUMNS;
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
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Category model, boolean withoutAutoId) {
    Object[] args = new Object[withoutAutoId ? 1 : 2];
    if (model.name != null) {
      args[0] = model.name;
    }
    else {
      throw new NullPointerException("Category.name" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (!withoutAutoId) {
      args[1] = model.id;
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement, @NonNull Category model, boolean withoutAutoId) {
    statement.bindString(1, model.name);
    if (!withoutAutoId) {
      statement.bindLong(2, model.id);
    }
  }

  @NonNull
  @Override
  public Category newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int offset) {
    String name = cursor.getString(offset + 0);
    long id = cursor.getLong(offset + 1);
    return new Category(id, name);
  }
}
