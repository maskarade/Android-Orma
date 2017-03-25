package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Updater;
import com.github.gfx.android.orma.example.tool.TypeAdapters;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

public class Item2_Updater extends Updater<Item2, Item2_Updater> {
  final Item2_Schema schema;

  public Item2_Updater(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Updater(Item2_Updater that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item2_Updater(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item2_Updater clone() {
    return new Item2_Updater(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Updater category1(@NonNull Category category1) {
    contents.put("`category1`", category1.id);
    return this;
  }

  public Item2_Updater category2(@Nullable Category category2) {
    contents.put("`category2`", category2.id);
    return this;
  }

  public Item2_Updater zonedTimestamp(@NonNull ZonedDateTime zonedTimestamp) {
    contents.put("`zonedTimestamp`", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
    return this;
  }

  public Item2_Updater localDateTime(@NonNull LocalDateTime localDateTime) {
    contents.put("`localDateTime`", TypeAdapters.serializeLocalDateTime(localDateTime));
    return this;
  }

  public Item2_Updater name(@NonNull String name) {
    contents.put("`name`", name);
    return this;
  }

  public Item2_Updater category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id);
  }

  public Item2_Updater category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Updater category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Updater category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Updater category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id);
  }

  public Item2_Updater category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  @Deprecated
  public Item2_Updater zonedTimestampEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater zonedTimestampNotEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<>", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater zonedTimestampIn(@NonNull Collection<ZonedDateTime> values) {
    return in(false, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Updater zonedTimestampNotIn(@NonNull Collection<ZonedDateTime> values) {
    return in(true, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Updater zonedTimestampIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Updater zonedTimestampNotIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Updater zonedTimestampLt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater zonedTimestampLe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater zonedTimestampGt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater zonedTimestampGe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Updater localDateTimeEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Updater localDateTimeNotEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<>", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Updater localDateTimeIn(@NonNull Collection<LocalDateTime> values) {
    return in(false, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Updater localDateTimeNotIn(@NonNull Collection<LocalDateTime> values) {
    return in(true, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Updater localDateTimeIn(@NonNull LocalDateTime... values) {
    return localDateTimeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Updater localDateTimeNotIn(@NonNull LocalDateTime... values) {
    return localDateTimeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Updater localDateTimeLt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Updater localDateTimeLe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Updater localDateTimeGt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Updater localDateTimeGe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  public Item2_Updater nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Updater nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Updater nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Updater nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Updater nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Updater nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Updater nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Updater nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Updater nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Updater nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
