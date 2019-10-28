package org.apache.flink.table.sources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.types.DFuncProjectFieldInfo;
import org.apache.flink.types.DProjectFieldInfo;
import org.apache.flink.types.DProjectSchema;
import org.apache.flink.types.DRecordTuple;
import org.apache.flink.types.DSchemaTuple;
import org.apache.flink.types.DStreamRecord;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;

/**
 * @author hanhan.zhang
 */
public class DProjectFieldsSelectProcessFunction extends
    BroadcastProcessFunction<DRecordTuple, DSchemaTuple, DStreamRecord> {

  private transient MapStateDescriptor<Void, Map<String, String>> projectFieldStateDesc;

  private final Map<String, String> projectNameToTypes;

  public DProjectFieldsSelectProcessFunction(Map<String, String> nameToTypes) {
    this.projectNameToTypes = nameToTypes;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    projectFieldStateDesc = new MapStateDescriptor<>(
        "BroadcastSqlProjectSchemaState", Types.VOID, Types.MAP(Types.STRING, Types.STRING));
  }


  @Override
  public void processElement(DRecordTuple recordTuple, ReadOnlyContext ctx,
      Collector<DStreamRecord> out) throws Exception {
    ReadOnlyBroadcastState<Void, Map<String, String>> state = ctx
        .getBroadcastState(projectFieldStateDesc);
    if (state == null || state.get(null) == null || state.get(null).isEmpty()) {
      out.collect(buildStreamTupleRecord(projectNameToTypes, recordTuple));
    } else {
      Map<String, String> nameToType = state.get(null);
      out.collect(buildStreamTupleRecord(nameToType, recordTuple));
    }
  }

  @Override
  public void processBroadcastElement(DSchemaTuple schemaTuple, Context ctx,
      Collector<DStreamRecord> out) throws Exception {
    BroadcastState<Void, Map<String, String>> broadcastState = ctx.getBroadcastState(projectFieldStateDesc);

    // TableScan负责更新映射字段
    DProjectSchema projectSchema = schemaTuple.getProjectSchema();
    Preconditions.checkNotNull(projectSchema);

    Map<String, DProjectFieldInfo> inputProjectSchemas = projectSchema.getInputProjectFields();
    Map<String, String> fieldNameToTypes = new HashMap<>();
    for (Entry<String, DProjectFieldInfo> entry : inputProjectSchemas.entrySet()) {
      fromProjectFieldInfo(entry.getValue(), fieldNameToTypes);
    }

    if (!fieldNameToTypes.isEmpty()) {
      broadcastState.put(null, fieldNameToTypes);
    }

    out.collect(new DStreamRecord(schemaTuple));
  }

  private static DStreamRecord buildStreamTupleRecord(Map<String, String> nameToType,
      DRecordTuple recordTuple) {
    Map<String, String> recordValues = new HashMap<>();
    Map<String, String> recordTypes = new HashMap<>();
    for (Entry<String, String> entry : nameToType.entrySet()) {
      String fieldName = entry.getKey();
      String fieldType = entry.getValue();

      recordValues.put(fieldName, recordTuple.getRecordValue(fieldName));
      recordTypes.put(fieldName, fieldType);
    }

    return new DStreamRecord(new DRecordTuple(recordTypes, recordValues));
  }

  private static void fromProjectFieldInfo(DProjectFieldInfo projectFieldInfo, Map<String, String> projectNameToTypes) {
    if (projectFieldInfo instanceof DFuncProjectFieldInfo) {
      DFuncProjectFieldInfo funcProjectFieldInfo = (DFuncProjectFieldInfo) projectFieldInfo;
      List<DProjectFieldInfo> projectFieldInfos = funcProjectFieldInfo.getParameterProjectFields();
      if (CollectionUtils.isEmpty(projectFieldInfos)) {
        return;
      }
      for (DProjectFieldInfo fieldInfo : projectFieldInfos) {
        if (fieldInfo instanceof DFuncProjectFieldInfo) {
          fromProjectFieldInfo(fieldInfo, projectNameToTypes);
          continue;
        }
        projectNameToTypes.put(projectFieldInfo.getFieldName(), projectFieldInfo.getFieldType());
      }
    }

    else {
      projectNameToTypes.put(projectFieldInfo.getFieldName(), projectFieldInfo.getFieldType());
    }

  }
}
