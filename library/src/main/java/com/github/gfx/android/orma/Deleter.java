package com.github.gfx.android.orma;

public class Deleter<T, C extends Deleter> extends ConditionBase<T, C> {

    public Deleter(OrmaConnection connection, Schema<T> schema) {
        super(connection, schema);
    }

    /**
     * @return Number of rows updated
     */
    public int execute() {
        return connection.delete(schema.getTableName(), getWhereClause(), getWhereArgs());
    }
}
