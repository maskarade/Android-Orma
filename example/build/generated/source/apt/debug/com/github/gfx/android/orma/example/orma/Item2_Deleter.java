package com.github.gfx.android.orma.example.orma;

import android.support.annotation.NonNull;
import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.example.tool.TypeAdapters;
import com.github.gfx.android.orma.function.Function1;
import java.util.Arrays;
import java.util.Collection;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

public class Item2_Deleter extends Deleter<Item2, Item2_Deleter> {
  final Item2_Schema schema;

  public Item2_Deleter(OrmaConnection conn, Item2_Schema schema) {
    super(conn);
    this.schema = schema;
  }

  public Item2_Deleter(Item2_Deleter that) {
    super(that);
    this.schema = that.getSchema();
  }

  public Item2_Deleter(Item2_Relation relation) {
    super(relation);
    this.schema = relation.getSchema();
  }

  @Override
  public Item2_Deleter clone() {
    return new Item2_Deleter(this);
  }

  @NonNull
  @Override
  public Item2_Schema getSchema() {
    return schema;
  }

  public Item2_Deleter category1Eq(@NonNull Category category1) {
    return where(schema.category1, "=", category1.id);
  }

  public Item2_Deleter category1Eq(long category1Id) {
    return where(schema.category1, "=", category1Id);
  }

  public Item2_Deleter category2IsNull() {
    return where(schema.category2, " IS NULL");
  }

  public Item2_Deleter category2IsNotNull() {
    return where(schema.category2, " IS NOT NULL");
  }

  public Item2_Deleter category2Eq(@NonNull Category category2) {
    return where(schema.category2, "=", category2.id);
  }

  public Item2_Deleter category2Eq(long category2Id) {
    return where(schema.category2, "=", category2Id);
  }

  @Deprecated
  public Item2_Deleter zonedTimestampEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampNotEq(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<>", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampIn(@NonNull Collection<ZonedDateTime> values) {
    return in(false, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Deleter zonedTimestampNotIn(@NonNull Collection<ZonedDateTime> values) {
    return in(true, schema.zonedTimestamp, values, new Function1<ZonedDateTime, String>() {
      @Override
      public String apply(ZonedDateTime value) {
        return TypeAdapters.serializeZonedDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Deleter zonedTimestampIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Deleter zonedTimestampNotIn(@NonNull ZonedDateTime... values) {
    return zonedTimestampNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampLt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampLe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, "<=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampGt(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter zonedTimestampGe(@NonNull ZonedDateTime zonedTimestamp) {
    return where(schema.zonedTimestamp, ">=", TypeAdapters.serializeZonedDateTime(zonedTimestamp));
  }

  @Deprecated
  public Item2_Deleter localDateTimeEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Deleter localDateTimeNotEq(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<>", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Deleter localDateTimeIn(@NonNull Collection<LocalDateTime> values) {
    return in(false, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public Item2_Deleter localDateTimeNotIn(@NonNull Collection<LocalDateTime> values) {
    return in(true, schema.localDateTime, values, new Function1<LocalDateTime, String>() {
      @Override
      public String apply(LocalDateTime value) {
        return TypeAdapters.serializeLocalDateTime(value);
      }
    });
  }

  @Deprecated
  public final Item2_Deleter localDateTimeIn(@NonNull LocalDateTime... values) {
    return localDateTimeIn(Arrays.asList(values));
  }

  @Deprecated
  public final Item2_Deleter localDateTimeNotIn(@NonNull LocalDateTime... values) {
    return localDateTimeNotIn(Arrays.asList(values));
  }

  @Deprecated
  public Item2_Deleter localDateTimeLt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Deleter localDateTimeLe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, "<=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Deleter localDateTimeGt(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  @Deprecated
  public Item2_Deleter localDateTimeGe(@NonNull LocalDateTime localDateTime) {
    return where(schema.localDateTime, ">=", TypeAdapters.serializeLocalDateTime(localDateTime));
  }

  public Item2_Deleter nameEq(@NonNull String name) {
    return where(schema.name, "=", name);
  }

  public Item2_Deleter nameNotEq(@NonNull String name) {
    return where(schema.name, "<>", name);
  }

  public Item2_Deleter nameIn(@NonNull Collection<String> values) {
    return in(false, schema.name, values);
  }

  public Item2_Deleter nameNotIn(@NonNull Collection<String> values) {
    return in(true, schema.name, values);
  }

  public final Item2_Deleter nameIn(@NonNull String... values) {
    return nameIn(Arrays.asList(values));
  }

  public final Item2_Deleter nameNotIn(@NonNull String... values) {
    return nameNotIn(Arrays.asList(values));
  }

  public Item2_Deleter nameLt(@NonNull String name) {
    return where(schema.name, "<", name);
  }

  public Item2_Deleter nameLe(@NonNull String name) {
    return where(schema.name, "<=", name);
  }

  public Item2_Deleter nameGt(@NonNull String name) {
    return where(schema.name, ">", name);
  }

  public Item2_Deleter nameGe(@NonNull String name) {
    return where(schema.name, ">=", name);
  }
}
