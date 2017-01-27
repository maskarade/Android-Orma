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
import com.github.gfx.android.orma.internal.Aliases;
import com.github.gfx.android.orma.internal.Schemas;
import java.util.Arrays;
import java.util.List;

public class Item_Schema implements Schema<Item> {
  public static final Item_Schema INSTANCE = Schemas.register(new Item_Schema());

  @Nullable
  private final String $alias;

  public final AssociationDef<Item, Category, Category_Schema> category;

  public final ColumnDef<Item, String> name;

  private final String[] $defaultResultColumns;

  public Item_Schema() {
    this(new Aliases().createPath("Item"));
  }

  public Item_Schema(@Nullable Aliases.ColumnPath current) {
    $alias = current != null ? current.getAlias() : null;
    this.name = new ColumnDef<Item, String>(this, "name", String.class, "TEXT", ColumnDef.PRIMARY_KEY) {
      @Override
      @NonNull
      public String get(@NonNull Item model) {
        return model.name;
      }

      @NonNull
      @Override
      public String getSerialized(@NonNull Item model) {
        return model.name;
      }

      @NonNull
      @Override
      public String getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor, int index) {
        return cursor.getString(index);
      }
    };
    this.category = new AssociationDef<Item, Category, Category_Schema>(this, "category", Category.class, "INTEGER", ColumnDef.INDEXED, new Category_Schema(current != null ? current.add("category", "Category") : null)) {
      @Override
      @NonNull
      public Category get(@NonNull Item model) {
        return model.category;
      }

      @NonNull
      @Override
      public Long getSerialized(@NonNull Item model) {
        return model.category.id;
      }

      @NonNull
      @Override
      public Category getFromCursor(@NonNull OrmaConnection conn, @NonNull Cursor cursor,
          int index) {
        return Category_Schema.INSTANCE.newModelFromCursor(conn, cursor, index + 1) /* consumes items: 2 */;
      }
    };
    $defaultResultColumns = new String[]{
          category.getQualifiedName(),
            category.associationSchema.name.getQualifiedName(),
            category.associationSchema.id.getQualifiedName()
          ,
          name.getQualifiedName()
        };
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
    return "`Item`"+ " AS " + getEscapedTableAlias()
        + " LEFT OUTER JOIN `Category` AS " + category.associationSchema.getEscapedTableAlias() + " ON " + category.getQualifiedName() + " = " + category.associationSchema.id.getQualifiedName()

        ;
  }

  @NonNull
  @Override
  public ColumnDef<Item, String> getPrimaryKey() {
    return name;
  }

  @NonNull
  @Override
  public List<ColumnDef<Item, ?>> getColumns() {
    return Arrays.<ColumnDef<Item, ?>>asList(
          category,
          name
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
      default: throw new IllegalArgumentException("Invalid OnConflict algorithm: " + onConflictAlgorithm);
    }
    s.append(" INTO `Item` (`category`,`name`) VALUES (?,?)");
    return s.toString();
  }

  /**
   * Convert models to {@code Object[]}. Provided for debugging
   */
  @NonNull
  @Override
  public Object[] convertToArgs(@NonNull OrmaConnection conn, @NonNull Item model,
      boolean withoutAutoId) {
    Object[] args = new Object[2];
    if (model.category != null) {
      args[0] = model.category.id;
    }
    else {
      throw new IllegalArgumentException("Item.category" + " must not be null, or use @Nullable to declare it as NULL");
    }
    if (model.name != null) {
      args[1] = model.name;
    }
    else {
      throw new IllegalArgumentException("Item.name" + " must not be null, or use @Nullable to declare it as NULL");
    }
    return args;
  }

  @Override
  public void bindArgs(@NonNull OrmaConnection conn, @NonNull SQLiteStatement statement,
      @NonNull Item model, boolean withoutAutoId) {
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
