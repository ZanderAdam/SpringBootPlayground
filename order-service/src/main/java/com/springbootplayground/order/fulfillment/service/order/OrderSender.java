package com.springbootplayground.order.fulfillment.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootplayground.order.fulfillment.service.configuration.KinesisConfigProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class OrderSender {
  private final KinesisClient kinesisClient;
  private final String streamName;

  public OrderSender(KinesisConfigProperties kinesisConfigProperties) throws URISyntaxException {
    System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");

    this.streamName = kinesisConfigProperties.getStreamName();

    AwsBasicCredentials cred = AwsBasicCredentials.create(kinesisConfigProperties.getAccessKeyId(),
      kinesisConfigProperties.getSecretKey());

    kinesisClient = KinesisClient.builder()
      .endpointOverride(new URI(kinesisConfigProperties.getEndpoint()))
      .credentialsProvider(StaticCredentialsProvider.create(cred))
      .region(Region.US_EAST_1)
      .build();
  }

  public void send(Order newOrder) {
    try {
      String newOrderJson = new ObjectMapper().writeValueAsString(newOrder);

      PutRecordRequest request = PutRecordRequest.builder()
        .streamName(streamName)
        .partitionKey(newOrder.getUserId().toString())
        .data(SdkBytes.fromByteArray(newOrderJson.getBytes()))
        .build();

      PutRecordResponse test = kinesisClient.putRecord(request);
      String test2 = test.shardId();

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
