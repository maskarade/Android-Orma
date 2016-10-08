package com.github.gfx.android.orma.example.orma;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.AssociationDef;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.List;

public class Item2_Schema implements Schema<Item2> {
  public static final Item2_Schema INSTANCE = Schemas.register(new Item2_Schema("i2"));

  @Nullable
  public final String alias;

  public final AssociationDef<Item2, Category, Category_Schema> category1 = new AssociationDef<Item2, Category, Category_Schema>(this, "category1", Category.class, "INTEGER", ColumnDef.INDEXED, new Category_Schema("c4")) {
    @Override
    @NonNull
    public Category get(@NonNull Item2 model) {
      return model.category1;
    }

    @Override
    @NonNull
    public Long getSerialized(@NonNull Item2 model) {
      return model.category1.id;
    }
  };

  public final AssociationDef<Item2, Category, Category_Schema> category2 = new AssociationDef<Item2, Category, Category_Schema>(this, "category2", Category.class, "INTEGER", ColumnDef.INDEXED, new Category_Schema("c5")) {
    @Override
    @NonNull
    public Category get(@NonNull Item2 model) {
      return model.category2;
    }

    @Override
    @NonNull
    public Long getSerialized(@NonNull Item2 model) {
      return model.category2.id;
    }
  };

  public final ColumnDef<Item2, String> name = new ColumnDef<Item2, String>(this, "name", String.class, "TEXT", ColumnDef.PRIMARY_KEY) {
    @Override
    @NonNull
    public String get(@NonNull Item2 model) {
      return model.name;
    }

    @Override
    @NonNull
    public String getSerialized(@NonNull Item2 model) {
      return model.name;
    }
  };

  final List<ColumnDef<Item2, ?>> $COLUMNS = Arrays.<ColumnDef<Item2, ?>>asList(
    category1,
    category2,
    name
  );

  final String[] $DEFAULT_RESULT_COLUMNS;

  Item2_Schema(@Nullable String alias) {
    this.alias = alias;
    $DEFAULT_RESULT_COLUMNS = new String[]{
          category1.getQualifiedName(),
            category1.associationSchema.name.getQualifiedName(),
            category1.associationSchema.id.getQualifiedName()
          ,
          category2.getQualifiedName(),
            category2.associationSchema.name.getQualifiedName(),
            category2.associationSchema.id.getQualifiedName()
          ,
          name.getQualifiedName()
        };
  }

  Item2_Schema() {
    this(null);
  }

  @NonNull
  @Override
  public Class<Item2> getModelClass() {
    return Item2.class;
  }

  @NonNull
  @Override
  public String getTableName() {
    return "Item2";
  }

  @NonNull
  @Override
  public String getEscapedTableName() {
    return "`Item2`";
  }

  @NonNull
  @Override
  public String getTableAlias() {
    return alias;
  }

  @NonNull
  @Override
  public String getEscapedTableAlias() {
    return alias != null ? '`' + alias + '`' : null;
  }

  @NonNull
  @Override
  public String getSelectFromTableClause() {
    return "`Item2` AS `i2`\n"
            + " LEFT OUTER JOIN `Category` AS `c4` ON `i2`.`category1` = `c4`.`id`\n"
            + " LEFT OUTER JOIN `Category` AS `c5` ON `i2`.`category2` = `c5`.`id`";
  }

  @NonNull
  @Override
  public ColumnDef<Item2, ?> getPrimaryKey() {
    return name;
  }

  @NonNull
  @Override
  public List<ColumnDef<Item2, ?>> getColumns() {
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
    return "CREATE TABLE `Item2` (`category1` INTEGER NOT NULL REFERENCES `Category`(`id`) ON UPDATE CASCADE ON DELETE CASCADE, `category2` INTEGER NOT NULL REFERENCES `Category`(`id`) ON UPDATE CASCADE ON DELETE CASCADE, `name` TEXT PRIMARY KEY)";
  }

  @NonNull
  @Override
  public List<String> getCreateIndexStatements() {
    return Arrays.asList(
      "CREATE INDEX `index_category1_on_Item2` ON `Item2` (`category1`)",
      "CREATE INDEX `index_category2_on_Item2` ON `Item2` (`category2`)"
    );
  }

  @NonNull
  @Override
  public String getDropTableStatement() {
    return "DROP TABLE IF EXISTS `Item2`";
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
    s.append(" INTO `Item2` (`category1`,`category2`,`name`) VALUES (?,?,?)");
    return s.toString();
  }

  /**
   * Convert models to {@code Object[]}. Provided for debugging
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Item2 model, boolean withoutAutoId) {
    Object[] args = new Object[3];
    if (model.category1 != null) {
      args[0] = model.category1.id;
    }
    else {
      throw new IllegalArgumentException("Item2.category1" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (model.category2 != null) {
      args[1] = model.category2.id;
    }
    else {
      throw new IllegalArgumentException("Item2.category2" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (model.name != null) {
      args[2] = model.name;
    }
    else {
      throw new IllegalArgumentException("Item2.name" + " must not be null, or use @Nullable to declare it as NULL");
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement, @NonNull Item2 model, boolean withoutAutoId) {
    statement.bindLong(1, model.category1.id);
    statement.bindLong(2, model.category2.id);
    statement.bindString(3, model.name);
  }

  @NonNull
  @Override
  public Item2 newModelFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int offset) {
    Category category1 = Category_Schema.INSTANCE.newModelFromCursor(conn, cursor, offset + 0 + 1) /* consumes items: 2 */;
    Category category2 = Category_Schema.INSTANCE.newModelFromCursor(conn, cursor, offset + 3 + 1) /* consumes items: 2 */;
    String name = cursor.getString(offset + 6);
    return new Item2(name, category1, category2);
  }
}
