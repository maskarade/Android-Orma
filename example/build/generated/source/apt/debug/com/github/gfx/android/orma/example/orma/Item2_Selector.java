package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.example.tool.TypeAdapters;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

public class Item2_Selector extends Selector<Item2, Item2_Selector> {
  final Item2_Schema schema;

  public Item2_Selector(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Selector(Item2_Selector that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item2_Selector(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item2_Selector clone() {
    return new Item2_Selector(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Selector category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id);
  }

  public Item2_Selector category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Selector category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Selector category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Selector category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id);
  }

  public Item2_Selector category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  @Deprecated
  public Item2_Selector zonedTimestampEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector zonedTimestampNotEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<>", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector zonedTimestampIn(@NonNull Collection<ZonedDateTime> values) {
    return in(false, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Selector zonedTimestampNotIn(@NonNull Collection<ZonedDateTime> values) {
    return in(true, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Selector zonedTimestampIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Selector zonedTimestampNotIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Selector zonedTimestampLt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector zonedTimestampLe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector zonedTimestampGt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector zonedTimestampGe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Selector localDateTimeEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Selector localDateTimeNotEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<>", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Selector localDateTimeIn(@NonNull Collection<LocalDateTime> values) {
    return in(false, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Selector localDateTimeNotIn(@NonNull Collection<LocalDateTime> values) {
    return in(true, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Selector localDateTimeIn(@NonNull LocalDateTime... values) {
    return localDateTimeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Selector localDateTimeNotIn(@NonNull LocalDateTime... values) {
    return localDateTimeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Selector localDateTimeLt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Selector localDateTimeLe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Selector localDateTimeGt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Selector localDateTimeGe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  public Item2_Selector nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Selector nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Selector nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Selector nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Selector nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Selector nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Selector nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Selector nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Selector nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Selector nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }

  public Item2_Selector orderByCategory1Asc() {
    return orderBy(schema.category1.orderInAscending());
  }

  public Item2_Selector orderByCategory1Desc() {
    return orderBy(schema.category1.orderInDescending());
  }

  public Item2_Selector orderByCategory2Asc() {
    return orderBy(schema.category2.orderInAscending());
  }

  public Item2_Selector orderByCategory2Desc() {
    return orderBy(schema.category2.orderInDescending());
  }

  @Deprecated
  public Item2_Selector orderByZonedTimestampAsc() {
    return orderBy(schema.zonedTimestamp.orderInAscending());
  }

  @Deprecated
  public Item2_Selector orderByZonedTimestampDesc() {
    return orderBy(schema.zonedTimestamp.orderInDescending());
  }

  @Deprecated
  public Item2_Selector orderByLocalDateTimeAsc() {
    return orderBy(schema.localDateTime.orderInAscending());
  }

  @Deprecated
  public Item2_Selector orderByLocalDateTimeDesc() {
    return orderBy(schema.localDateTime.orderInDescending());
  }

  public Item2_Selector orderByNameAsc() {
    return orderBy(schema.name.orderInAscending());
  }

  public Item2_Selector orderByNameDesc() {
    return orderBy(schema.name.orderInDescending());
  }
}
