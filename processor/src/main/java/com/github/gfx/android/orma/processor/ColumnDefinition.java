package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class ColumnDefinition {

    public final Element element;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean indexed;

    public final boolean unique;

    public ColumnDefinition(Element element) {
        this.element = element;

        // TODO: autoincrement, conflict clause, default value, etc...
        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        primaryKey = primaryKeyAnnotation != null;
        indexed = primaryKey || column.indexed();
        unique = primaryKey || column.unique();

        nullable = hasNullableAnnotation(element);
    }

    public String getName() {
        return element.getSimpleName().toString();
    }

    public TypeName getType() {
        return ClassName.get(element.asType());
    }

    /**
     * @return A representation of {@code ColumnDef<T>}
     */
    public ParameterizedTypeName getColumnDefType() {
        return Types.getColumnDef(getType().box());
    }

    public static boolean hasNullableAnnotation(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            // allow anything named "Nullable"
            if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                return true;
            }
        }
        return false;
    }
}
