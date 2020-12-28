package com.springbootplayground.order.fulfillment.service.configuration;

import com.springbootplayground.order.fulfillment.service.order.KinesisRecordProcessorFactory;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.CoordinatorConfig;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.metrics.MetricsConfig;
import software.amazon.kinesis.metrics.NullMetricsFactory;
import software.amazon.kinesis.retrieval.RecordsFetcherFactory;
import software.amazon.kinesis.retrieval.polling.PollingConfig;
import software.amazon.kinesis.retrieval.polling.SimpleRecordsFetcherFactory;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class KinesisConfig {
  private KinesisConfigProperties props;

  public KinesisConfig(KinesisConfigProperties props) {
    this.props = props;
  }

  @PostConstruct
  public void start() throws URISyntaxException {
    System.setProperty("aws.accessKeyId", props.getAccessKeyId());
    System.setProperty("aws.secretAccessKey", props.getSecretKey());

    Region region = Region.US_EAST_1;

    String workerId = "worker-" + UUID.randomUUID().toString();

    String applicationName = "SpringBootPlayground";
    String streamName = props.getStreamName();
    long pollingIntervalMillis = 1000;

    KinesisRecordProcessorFactory kinesisRecordProcessorFactory = new KinesisRecordProcessorFactory();

    SdkAsyncHttpClient nettyClient =
      NettyNioAsyncHttpClient.builder()
        .connectionMaxIdleTime(Duration.ofSeconds(5))
        .maxConcurrency(100)
        .maxPendingConnectionAcquires(10000)
        .build();

    CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder()
      .region(region)
      .endpointOverride(new URI(props.getEndpoint()))
      .httpClient(nettyClient)
      .build();

    KinesisAsyncClient kinesisClient =
      KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder()
        .endpointOverride(new URI(props.getEndpoint())).region(region));

    DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder()
      .endpointOverride(new URI(props.getEndpoint()))
      .region(region)
      .build();

    ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName,
      applicationName, kinesisClient, dynamoClient, cloudWatchClient,
      workerId, kinesisRecordProcessorFactory);

    RecordsFetcherFactory recordsFetcherFactory = new SimpleRecordsFetcherFactory();
    recordsFetcherFactory.idleMillisBetweenCalls(pollingIntervalMillis);
    PollingConfig pollingConfig = new PollingConfig(streamName, kinesisClient)
      .recordsFetcherFactory(recordsFetcherFactory)
      .idleTimeBetweenReadsInMillis(pollingIntervalMillis);

    //Disabled metrics for Localstack
    //https://github.com/localstack/localstack/issues/712
    //Supposed to be working, need to revisit
    MetricsConfig metricsConfig = configsBuilder.metricsConfig();
    metricsConfig.metricsFactory(new NullMetricsFactory());

    CoordinatorConfig coordinatorConfig = configsBuilder.coordinatorConfig();

    Scheduler scheduler = new Scheduler(
      configsBuilder.checkpointConfig(),
      coordinatorConfig,
      configsBuilder.leaseManagementConfig(),
      configsBuilder.lifecycleConfig().logWarningForTaskAfterMillis(Optional.of(10000L)),
      metricsConfig,
      configsBuilder.processorConfig(),
      configsBuilder.retrievalConfig().retrievalSpecificConfig(pollingConfig)
    );

    scheduler.run();
  }
}
