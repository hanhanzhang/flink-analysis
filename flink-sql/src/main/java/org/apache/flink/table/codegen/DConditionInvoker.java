package org.apache.flink.table.codegen;

import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.flink.types.DRecordTuple;

public class DConditionInvoker implements DRexInvoker<Boolean> {

  @Override
  public Boolean invoke(DRecordTuple recordTuple) throws DExpressionInvokeException {
    return null;
  }

  @Override
  public SqlTypeName getResultType() {
    return SqlTypeName.BOOLEAN;
  }

}