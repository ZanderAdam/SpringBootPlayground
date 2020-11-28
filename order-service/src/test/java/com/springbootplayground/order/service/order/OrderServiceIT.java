package com.springbootplayground.order.service.order;

import com.springbootplayground.order.service.configuration.KinesisConfigProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
public class OrderServiceIT {
  static {
    System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
  }

  private KinesisClient kinesisClient;

  static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack");

  @Autowired
  KinesisConfigProperties configProperties;

  @Autowired
  OrderSender orderSender;

  @Container
  public static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
    .withServices(LocalStackContainer.Service.KINESIS)
    .withEnv("AWS_CBOR_DISABLE", "1");

  @DynamicPropertySource
  static void dataSourceProperties(DynamicPropertyRegistry registry) {
    URI endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.KINESIS);
    registry.add("aws.endpoint", () -> endpoint.toString());
  }

  @Test
  public void newOrder_GivenNewOrder_SendToKinesisStream() {
    String shardIterator = setupKinesisStreamAndGetIterator();

    OrderService orderService = new OrderService(orderSender);

    NewOrderDTO newOrder = new NewOrderDTO();
    newOrder.setUserId(1);
    newOrder.setOrderedProductIds(Arrays.asList(1, 2, 3));

    orderService.placeOrder(newOrder);

    GetRecordsRequest request = GetRecordsRequest.builder()
      .shardIterator(shardIterator)
      .limit(100)
      .build();

    List<Record> records = kinesisClient.getRecords(request).records();

    Assertions.assertEquals(1, records.size());
  }

  private String setupKinesisStreamAndGetIterator() {

    kinesisClient = KinesisClient.builder()
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.KINESIS))
      .region(Region.of(localstack.getRegion()))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
        localstack.getAccessKey(), localstack.getSecretKey()
      )))
      .build();

    CreateStreamRequest createStreamRequest = CreateStreamRequest.builder()
      .streamName(configProperties.getStreamName())
      .shardCount(1)
      .build();

    kinesisClient.createStream(createStreamRequest);

    DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
      .streamName(configProperties.getStreamName())
      .build();

    await().until(() ->
      {
        DescribeStreamResponse describeStreamResponse = kinesisClient.describeStream(describeStreamRequest);
        return describeStreamResponse.streamDescription().streamStatus() == StreamStatus.ACTIVE;
      }
    );

    GetShardIteratorRequest readShardsRequest = GetShardIteratorRequest.builder()
      .streamName(configProperties.getStreamName())
      .shardIteratorType(ShardIteratorType.LATEST)
      .shardId("0")
      .build();

    GetShardIteratorResponse shardIteratorResponse = kinesisClient
      .getShardIterator(readShardsRequest);

    return shardIteratorResponse.shardIterator();
  }
}
