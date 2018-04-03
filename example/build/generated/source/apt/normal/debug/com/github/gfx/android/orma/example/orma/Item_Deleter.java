package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.function.Function1;
import com.github.gfx.android.orma.rx.RxDeleter;
import com.github.gfx.android.orma.rx.RxOrmaConnection;
import java.util.Arrays;
import java.util.Collection;

public class Item_Deleter extends RxDeleter<Item, Item_Deleter> {
  final Item_Schema schema;

  public Item_Deleter(RxOrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_Deleter(Item_Deleter that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item_Deleter(Item_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item_Deleter clone() {
    return new Item_Deleter(this);
  }

  @NonNull
  @Override
  public Item_Schema getSchema() {
    return schema;
  }

  public Item_Deleter categoryEq(@NonNull Category category) {
    return where(schema.category, "=", category.id);
  }

  public Item_Deleter categoryEq(long categoryId) {
    return where(schema.category, "=", categoryId);
  }

  public Item_Deleter category(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category.associationSchema)).appendTo(this);
  }

  public Item_Deleter nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item_Deleter nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item_Deleter nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item_Deleter nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item_Deleter nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_Deleter nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_Deleter nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item_Deleter nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item_Deleter nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item_Deleter nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
