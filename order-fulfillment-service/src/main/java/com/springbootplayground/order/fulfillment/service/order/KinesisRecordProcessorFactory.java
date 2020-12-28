package com.springbootplayground.order.fulfillment.service.order;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class KinesisRecordProcessorFactory implements ShardRecordProcessorFactory {
  @Override
  public ShardRecordProcessor shardRecordProcessor() {
    return new OrderFulfillmentProcessor();
  }
}
