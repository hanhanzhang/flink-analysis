package org.apache.flink.table.api;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.plan.nodes.datastream.StreamTableSourceScan;
import org.apache.flink.table.sources.DStreamTableSource;
import org.apache.flink.table.sources.DefinedRowtimeAttributes;
import org.apache.flink.table.sources.RowtimeAttributeDescriptor;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.wmstrategies.BoundedOutOfOrderTimestamps;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.DRecordTuple;
import org.apache.flink.types.DSchemaTuple;
import org.apache.flink.types.DSqlTypeUtils;

/**
 * {@link StreamTableSourceScan}
 *
 * @author hanhan.zhang
 * */
public class DStreamTableSourceImpl<T> implements DStreamTableSource, DefinedRowtimeAttributes {

  private final TableSchema tableSchema;

  private final DStreamSourceFunction<T> sourceFunction;

  private final String eventTimeName;
  private final long lateness;

  public DStreamTableSourceImpl(DStreamSourceFunction<T> sourceFunction, TableSchema tableSchema, String eventTimeName, long lateness) {
    this.sourceFunction = sourceFunction;
    this.tableSchema = tableSchema;
    this.eventTimeName = eventTimeName;
    this.lateness = lateness;
  }

  @Override
  public BroadcastStream<DSchemaTuple> getBroadcastStream(StreamExecutionEnvironment execEnv) {
    DataStream<DSchemaTuple> schemaUpdateStream = execEnv.addSource(new DSchemaUpdateFunction());
    MapStateDescriptor<Void, Map<String, String>> broadcastStateDescriptor = new MapStateDescriptor<>(
        "BroadcastSqlProjectSchemaState", Types.VOID, Types.MAP(Types.STRING, Types.STRING));
    return schemaUpdateStream.broadcast(broadcastStateDescriptor);
  }

  @Override
  public DataStream<DRecordTuple> getDataStream(StreamExecutionEnvironment execEnv) {
    final DRecordTupleFactory<T> factory = sourceFunction.getRecordTupleFactory();
    int parallelism = sourceFunction.getStreamParallelism();

    return execEnv.addSource(sourceFunction)
        .map(factory::buildRecordTuple)
        .returns(TypeInformation.of(DRecordTuple.class))
        .setParallelism(parallelism);
  }

  @Override
  public List<RowtimeAttributeDescriptor> getRowtimeAttributeDescriptors() {
    /*
     * 源头设置水位线, 仅支持单个字段.
     * **/
    if (eventTimeName == null || eventTimeName.isEmpty()) {
      return Collections.emptyList();
    }

    RowtimeAttributeDescriptor descriptor = new RowtimeAttributeDescriptor(
        eventTimeName, new ExistingField(eventTimeName), new BoundedOutOfOrderTimestamps(lateness));

    return Collections.singletonList(descriptor);
  }

  @Override
  public TableSchema getTableSchema() {
    return tableSchema;
  }

  @Override
  public DataType getProducedDataType() {
    return DSqlTypeUtils.fromTableSchema(tableSchema);
  }
}
