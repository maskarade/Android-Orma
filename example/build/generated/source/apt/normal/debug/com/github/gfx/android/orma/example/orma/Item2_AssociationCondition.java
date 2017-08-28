package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.AssociationCondition;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;

public class Item2_AssociationCondition extends AssociationCondition<Item2, Item2_AssociationCondition> {
  final Item2_Schema schema;

  public Item2_AssociationCondition(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_AssociationCondition(Item2_AssociationCondition that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item2_AssociationCondition(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item2_AssociationCondition clone() {
    return new Item2_AssociationCondition(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_AssociationCondition category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id);
  }

  public Item2_AssociationCondition category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_AssociationCondition category1(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category1.associationSchema)).appendTo(this);
  }

  public Item2_AssociationCondition category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_AssociationCondition category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_AssociationCondition category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id);
  }

  public Item2_AssociationCondition category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  public Item2_AssociationCondition category2(@NonNull Function1<Category_AssociationCondition, Category_AssociationCondition> block) {
    return block.apply(new Category_AssociationCondition(getConnection(), schema.category2.associationSchema)).appendTo(this);
  }

  public Item2_AssociationCondition nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_AssociationCondition nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_AssociationCondition nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_AssociationCondition nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_AssociationCondition nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_AssociationCondition nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_AssociationCondition nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_AssociationCondition nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_AssociationCondition nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_AssociationCondition nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
