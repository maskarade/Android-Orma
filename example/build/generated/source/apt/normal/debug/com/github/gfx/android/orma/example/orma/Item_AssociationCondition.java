package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.AssociationCondition;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;

public class Item_AssociationCondition extends AssociationCondition<Item, Item_AssociationCondition> {
  final Item_Schema schema;

  public Item_AssociationCondition(OrmaConnection conn, Item_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item_AssociationCondition(Item_AssociationCondition that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item_AssociationCondition(Item_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item_AssociationCondition clone() {
    return new Item_AssociationCondition(this);
  }

  @NonNull
  @Override
  public Item_Schema getSchema() {
    return schema;
  }

  public Item_AssociationCondition categoryEq(@NonNull Category category) {
    return where(schema.category, "=", category.id);
  }

  public Item_AssociationCondition categoryEq(long categoryId) {
    return where(schema.category, "=", categoryId);
  }

  public Item_AssociationCondition category(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category.associationSchema)).appendTo(this);
  }

  public Item_AssociationCondition nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item_AssociationCondition nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item_AssociationCondition nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item_AssociationCondition nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item_AssociationCondition nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item_AssociationCondition nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item_AssociationCondition nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item_AssociationCondition nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item_AssociationCondition nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item_AssociationCondition nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
