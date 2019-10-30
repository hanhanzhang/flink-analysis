package org.apache.flink.types;


import static org.apache.calcite.sql.type.SqlTypeName.BIGINT;
import static org.apache.calcite.sql.type.SqlTypeName.BOOLEAN;
import static org.apache.calcite.sql.type.SqlTypeName.CHAR;
import static org.apache.calcite.sql.type.SqlTypeName.DOUBLE;
import static org.apache.calcite.sql.type.SqlTypeName.FLOAT;
import static org.apache.calcite.sql.type.SqlTypeName.INTEGER;
import static org.apache.calcite.sql.type.SqlTypeName.SMALLINT;
import static org.apache.calcite.sql.type.SqlTypeName.TINYINT;
import static org.apache.calcite.sql.type.SqlTypeName.VARCHAR;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.flink.calcite.shaded.com.google.common.collect.ImmutableMap;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.types.DataType;

public class DTypeUtils {

  private static Map<SqlTypeName, Class<?>> SqlTypeToJavaTypes = ImmutableMap.<SqlTypeName, Class<?>>builder()
      .put(SqlTypeName.BOOLEAN, Boolean.class)
      // String
      .put(SqlTypeName.CHAR, Character.class)
      .put(SqlTypeName.VARCHAR, String.class)
      // Number
      .put(SqlTypeName.SMALLINT, Byte.class)
      .put(SqlTypeName.INTEGER, Integer.class)
      .put(SqlTypeName.BIGINT, Long.class)
      .put(SqlTypeName.REAL, BigDecimal.class)
      .put(SqlTypeName.FLOAT, Float.class)
      .put(SqlTypeName.DOUBLE, Double.class)
      // byte[]
      .put(SqlTypeName.VARBINARY, Byte[].class)
      .put(SqlTypeName.ARRAY, List.class)

      // date
      .put(SqlTypeName.DATE, Date.class)
      .put(SqlTypeName.TIMESTAMP, Timestamp.class)
      .put(SqlTypeName.TIME, Time.class)
      .put(SqlTypeName.DECIMAL, BigDecimal.class)

      .build();


  private static Map<String, SqlTypeName> javaTypeToSqlTypes = ImmutableMap.<String, SqlTypeName>builder()
      // Boolean
      .put(Boolean.class.getSimpleName(), SqlTypeName.BOOLEAN)

      // String
      .put(Character.class.getSimpleName(), SqlTypeName.CHAR)
      .put(String.class.getSimpleName(), SqlTypeName.VARCHAR)

      // Number
      .put(Byte.class.getSimpleName(), SqlTypeName.SMALLINT)
      .put(Integer.class.getSimpleName(), SqlTypeName.INTEGER)
      .put(Long.class.getSimpleName(), SqlTypeName.BIGINT)
      .put(BigDecimal.class.getSimpleName(), SqlTypeName.REAL)
      .put(Float.class.getSimpleName(), SqlTypeName.FLOAT)
      .put(Double.class.getSimpleName(), SqlTypeName.DOUBLE)

      // byte[]
      .put(Byte[].class.getSimpleName(), SqlTypeName.VARBINARY)
      .put(List.class.getSimpleName(), SqlTypeName.ARRAY)

      // date
      .put(Date.class.getSimpleName(), SqlTypeName.DATE)
      .put(Timestamp.class.getSimpleName(), SqlTypeName.TIMESTAMP)
      .put(Time.class.getSimpleName(), SqlTypeName.TIME)
//      .put(BigDecimal.class.getSimpleName(), SqlTypeName.DECIMAL)

      .build();

  private DTypeUtils() {
  }

  public static Class<?> sqlTypeToJavaType(SqlTypeName sqlTypeName) {
    Class<?> javaType = SqlTypeToJavaTypes.get(sqlTypeName);
    if (javaType == null) {
      throw new UnsupportedTypeException(sqlTypeName.getName());
    }
    return javaType;
  }

  public static String sqlTypeToJavaTypeAsString(SqlTypeName sqlTypeName) {
    return sqlTypeToJavaType(sqlTypeName).getSimpleName();
  }

  public static DataType javaTypeToDataType(String javaType) {
    switch (javaType) {
      case "Boolean":
        return DataTypes.BOOLEAN();
      case "Integer":
        return DataTypes.INT();
      case "Long":
        return DataTypes.BIGINT();
      case "Double":
        return DataTypes.DOUBLE();
      case "Float":
        return DataTypes.FLOAT();
      case "String":
        return DataTypes.STRING();
      default:
        throw new UnsupportedTypeException(javaType);
    }
  }

  public static boolean isNumeric(SqlTypeName sqlTypeName) {
    return sqlTypeName == INTEGER || sqlTypeName == TINYINT || sqlTypeName == SMALLINT || sqlTypeName == BIGINT
        || sqlTypeName == FLOAT || sqlTypeName == DOUBLE;

  }

  public static boolean isString(SqlTypeName sqlTypeName) {
    return sqlTypeName == VARCHAR || sqlTypeName == CHAR;
  }

  public static boolean isBoolean(SqlTypeName sqlTypeName) {
    return sqlTypeName == BOOLEAN;
  }

  public static boolean canCastToDouble(SqlTypeName sqlTypeName) {
    return sqlTypeName == FLOAT || sqlTypeName == DOUBLE;
  }

}
