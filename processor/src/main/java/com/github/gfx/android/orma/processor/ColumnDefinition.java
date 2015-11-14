package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

public class ColumnDefinition {

    public final Element element;

    public final String name;

    public final String columnName;

    public final TypeName type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public ColumnDefinition(Element element) {
        this.element = element;

        // TODO: autoincrement, conflict clause, default value, etc...
        // See https://www.sqlite.org/lang_createtable.html for full specification
        Column column = element.getAnnotation(Column.class);
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);

        name = element.getSimpleName().toString();
        columnName = getColumnName(column, element);

        type = ClassName.get(element.asType());

        if (primaryKeyAnnotation != null) {
            primaryKey = true;
            autoincrement = primaryKeyAnnotation.autoincrement();
            autoId = primaryKeyAnnotation.auto() && Types.looksLikeIntegerType(type);
        } else {
            primaryKey = false;
            autoincrement = false;
            autoId = false;
        }

        indexed = primaryKey || column.indexed();
        unique = primaryKey || column.unique();

        nullable = hasNullableAnnotation(element);
    }

    public RelationDefinition getRelation() {
        if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName pt = (ParameterizedTypeName) type;
            if (pt.rawType.equals(Types.HasOne) || pt.rawType.equals(Types.HasMany)) {
                return new RelationDefinition(pt.rawType, pt.typeArguments.get(0));
            }
        }
        return null;

    }

    public TypeName getType() {
        return type;
    }

    public TypeName getRawType() {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName)type).rawType;
        } else {
            return type;
        }
    }

    /**
     * @return A representation of {@code ColumnDef<T>}
     */
    public ParameterizedTypeName getColumnDefType() {
        return Types.getColumnDef(type.box());
    }

    static String getColumnName(Column column, Element element) {
        if (column != null && !column.value().equals("")) {
            return column.value();
        } else {
            for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                Name annotationName = annotation.getAnnotationType().asElement().getSimpleName();
                if (annotationName.contentEquals("SerializedName") // GSON
                        || annotationName.contentEquals("JsonProperty") // Jackson
                        ) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
                            .getElementValues().entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("value")) {
                            return entry.getValue().getValue().toString();
                        }
                    }
                }
            }
        }
        return element.getSimpleName().toString();
    }

    static boolean hasNullableAnnotation(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            // allow anything named "Nullable"
            if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                return true;
            }
        }
        return false;
    }
}
