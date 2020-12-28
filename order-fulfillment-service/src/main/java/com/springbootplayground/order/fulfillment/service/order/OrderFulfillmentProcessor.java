package com.springbootplayground.order.fulfillment.service.order;

import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;

import java.nio.charset.StandardCharsets;

public class OrderFulfillmentProcessor implements ShardRecordProcessor {
  @Override
  public void initialize(InitializationInput initializationInput) {

  }

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {
    System.out.println("            GOT DATA           ");
    processRecordsInput.records()
      .forEach(record -> System.out.println(
        StandardCharsets.UTF_8.decode(record.data()).toString()
      ));
  }

  @Override
  public void leaseLost(LeaseLostInput leaseLostInput) {

  }

  @Override
  public void shardEnded(ShardEndedInput shardEndedInput) {
    try {
      shardEndedInput.checkpointer().checkpoint();
    } catch (ShutdownException | InvalidStateException e) {
      //
      // Swallow the exception
      //
      e.printStackTrace();
    }
  }

  @Override
  public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    try {
      shutdownRequestedInput.checkpointer().checkpoint();
    } catch (ShutdownException | InvalidStateException e) {
      //
      // Swallow the exception
      //
      e.printStackTrace();
    }
  }
}
