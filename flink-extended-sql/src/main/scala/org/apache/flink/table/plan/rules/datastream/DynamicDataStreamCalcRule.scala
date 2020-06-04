package org.apache.flink.table.plan.rules.datastream

import org.apache.calcite.plan.{RelOptRule, RelTraitSet}
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.convert.ConverterRule
import org.apache.flink.table.plan.nodes.datastream.DynamicDataStreamCalc
import org.apache.flink.table.plan.nodes.logical.FlinkLogicalCalc
import org.apache.flink.table.plan.nodes.{DynamicConventions, FlinkConventions}
import org.apache.flink.table.plan.schema.RowSchema

class DynamicDataStreamCalcRule extends ConverterRule (
  classOf[FlinkLogicalCalc],
  FlinkConventions.LOGICAL,
  DynamicConventions.DYNAMIC_DATASTREAM,
  "DynamicDataStreamCalcRule") {

  override def convert(rel: RelNode): RelNode = {
    val calc: FlinkLogicalCalc = rel.asInstanceOf[FlinkLogicalCalc]
    val traitSet: RelTraitSet = rel.getTraitSet.replace(DynamicConventions.DYNAMIC_DATASTREAM)
    val convInput: RelNode = RelOptRule.convert(calc.getInput, DynamicConventions.DYNAMIC_DATASTREAM)

    new DynamicDataStreamCalc(
      rel.getCluster,
      traitSet,
      convInput,
      new RowSchema(convInput.getRowType),
      new RowSchema(rel.getRowType),
      calc.getProgram,
      "DynamicDataStreamCalcRule")
  }

}


object DynamicDataStreamCalcRule {

  val INSTANCE = new DynamicDataStreamCalcRule

}