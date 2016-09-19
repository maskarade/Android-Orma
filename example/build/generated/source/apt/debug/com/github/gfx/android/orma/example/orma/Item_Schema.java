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
import java.util.List;

public class Item_Schema implements Schema<Item> {
  public static final Item_Schema INSTANCE = Schemas.register(new Item_Schema());

  public final String alias;

  public final ColumnDef<Item, Category> category = new ColumnDef<Item, Category>(this, "category", Category.class, "INTEGER", ColumnDef.INDEXED) {
    @Override
    @NonNull
    public Category get(@NonNull Item model) {
      return model.category;
    }

    @Override
    @NonNull
    public Long getSerialized(@NonNull Item model) {
      return model.category.id;
    }
  };

  public final ColumnDef<Item, String> name = new ColumnDef<Item, String>(this, "name", String.class, "TEXT", ColumnDef.PRIMARY_KEY) {
    @Override
    @NonNull
    public String get(@NonNull Item model) {
      return model.name;
    }

    @Override
    @NonNull
    public String getSerialized(@NonNull Item model) {
      return model.name;
    }
  };

  final List<ColumnDef<Item, ?>> $COLUMNS = Arrays.<ColumnDef<Item, ?>>asList(
    category,
    name
  );

  final String[] $DEFAULT_RESULT_COLUMNS = {
    "`Item`.`category`",
      "`Category`.`name`",
      "`Category`.`id`"
    ,
    "`Item`.`name`"
  };

  Item_Schema(@NonNull String alias) {
    this.alias = alias;
  }

  Item_Schema() {
    this("Item");
  }

  @NonNull
  @Override
  public Class<Item> getModelClass() {
    return Item.class;
  }

  @NonNull
  @Override
  public String getTableName() {
    return "Item";
  }

  @NonNull
  @Override
  public String getEscapedTableName() {
    return "`Item`";
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
    return "`Item` LEFT OUTER JOIN `Category` ON `Item`.`category` = `Category`.`id`";
  }

  @NonNull
  @Override
  public ColumnDef<Item, ?> getPrimaryKey() {
    return name;
  }

  @NonNull
  @Override
  public List<ColumnDef<Item, ?>> getColumns() {
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
    return "CREATE TABLE `Item` (`category` INTEGER NOT NULL REFERENCES `Category`(`id`) ON UPDATE CASCADE ON DELETE CASCADE, `name` TEXT PRIMARY KEY)";
  }

  @NonNull
  @Override
  public List<String> getCreateIndexStatements() {
    return Arrays.asList(
      "CREATE INDEX `index_category_on_Item` ON `Item` (`category`)"
    );
  }

  @NonNull
  @Override
  public String getDropTableStatement() {
    return "DROP TABLE IF EXISTS `Item`";
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
    s.append(" INTO `Item` (`category`,`name`) VALUES (?,?)");
    return s.toString();
  }

  /**
   * Convert models to {@code Object[]}. Provided for debugging
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Item model, boolean withoutAutoId) {
    Object[] args = new Object[2];
    if (model.category != null) {
      args[0] = model.category.id;
    }
    else {
      throw new NullPointerException("Item.category" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (model.name != null) {
      args[1] = model.name;
    }
    else {
      throw new NullPointerException("Item.name" + " must not be null, or use @Nullable to declare it as NULL");
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement, @NonNull Item model, boolean withoutAutoId) {
    statement.bindLong(1, model.category.id);
    statement.bindString(2, model.name);
  }

  @NonNull
  @Override
  public Item newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int offset) {
    Category category = Category_Schema.INSTANCE.newModelFromCursor(conn, cursor, offset + 0 + 1) /* consumes items: 2 */;
    String name = cursor.getString(offset + 3);
    return new Item(name, category);
  }
}
