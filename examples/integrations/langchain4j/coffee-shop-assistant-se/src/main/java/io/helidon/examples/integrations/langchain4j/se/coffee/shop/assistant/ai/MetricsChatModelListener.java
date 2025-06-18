package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai;

import java.util.concurrent.TimeUnit;

import io.helidon.metrics.api.DistributionStatisticsConfig;
import io.helidon.metrics.api.DistributionSummary;
import io.helidon.metrics.api.MeterRegistry;
import io.helidon.metrics.api.Metrics;
import io.helidon.metrics.api.Tag;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequest;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;

/**
 * Creates metrics that follow the
 * <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/">Semantic Conventions for GenAI Metrics</a>.
 */
public class MetricsChatModelListener implements ChatModelListener {

    private static final String GEN_AI_CLIENT_OPERATION_START_TIME = "GEN_AI_CLIENT_OPERATION_START_TIME";
    private static final String GEN_AI_CLIENT_TOKEN_USAGE_METRICS_NAME = "gen_ai.client.token.usage";
    private static final String GEN_AI_CLIENT_OPERATION_DURATION_METRICS_NAME = "gen_ai.client.operation.duration";

    private MeterRegistry meterRegistry;
    private DistributionSummary clientTokenUsage;
    private DistributionSummary.Builder clientTokenUsageBuilder;
    private DistributionSummary clientOperationDuration;
    private DistributionSummary.Builder clientOperationDurationBuilder;

    public MetricsChatModelListener() {
        this.meterRegistry = Metrics.globalRegistry();

        DistributionStatisticsConfig.Builder clientTokenUsageStatisticsConfigBuilder = DistributionStatisticsConfig.builder()
                .buckets(1, 4, 16, 64, 256, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304, 16777216, 67108864);
        this.clientTokenUsageBuilder = DistributionSummary.builder(GEN_AI_CLIENT_TOKEN_USAGE_METRICS_NAME, clientTokenUsageStatisticsConfigBuilder)
                .scope(DistributionSummary.Scope.VENDOR)
                .baseUnit("token")
                .description("Measures number of input and output tokens used");

        DistributionStatisticsConfig.Builder clientOperationDurationStatisticsConfigBuilder = DistributionStatisticsConfig.builder()
                .buckets(0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.28, 2.56, 5.12, 10.24, 20.48, 40.96, 81.92);
        this.clientOperationDurationBuilder = DistributionSummary.builder(GEN_AI_CLIENT_OPERATION_DURATION_METRICS_NAME, clientOperationDurationStatisticsConfigBuilder)
                .scope(DistributionSummary.Scope.VENDOR)
                .baseUnit(DistributionSummary.BaseUnits.SECONDS)
                .description("GenAI operation duration");
        System.out.println(" ** MetricsChatModelListener initialized ** ");
    }

    @Override
    public void onRequest(ChatModelRequestContext chatModelRequestContext) {
        chatModelRequestContext.attributes().put(GEN_AI_CLIENT_OPERATION_START_TIME, System.nanoTime());
        System.out.println("** onRequest: Started timer clientOperationDuration ***");
    }

    @Override
    public void onResponse(ChatModelResponseContext chatModelResponseContext) {
        final long endTime = System.nanoTime();
        final long startTime = (Long) chatModelResponseContext.attributes().get(GEN_AI_CLIENT_OPERATION_START_TIME);

        final ChatModelRequest chatModelRequest = chatModelResponseContext.request();
        final ChatModelResponse chatModelResponse = chatModelResponseContext.response();

        DistributionSummary.Builder inputClientTokenUsageBuilder = this.clientTokenUsageBuilder
                .addTag(Tag.create("gen_ai.operation.name", "chat"))
                .addTag(Tag.create("gen_ai.request.model", chatModelRequest.model()))
                .addTag(Tag.create("gen_ai.token.type", "input"));
        this.clientTokenUsage = this.meterRegistry.getOrCreate(inputClientTokenUsageBuilder);
        System.out.println("** onResponse: Recording inputTokenCount ***");
        this.clientTokenUsage.record(chatModelResponse.tokenUsage().inputTokenCount());

        DistributionSummary.Builder outputClientTokenUsageBuilder = this.clientTokenUsageBuilder
                .addTag(Tag.create("gen_ai.operation.name", "chat"))
                .addTag(Tag.create("gen_ai.response.model", chatModelResponse.model()))
                .addTag(Tag.create("gen_ai.token.type", "output"));
        this.clientTokenUsage = this.meterRegistry.getOrCreate(outputClientTokenUsageBuilder);
        System.out.println("** onResponse: Recording outputTokenCount ***");
        this.clientTokenUsage.record(chatModelResponse.tokenUsage().outputTokenCount());

        DistributionSummary.Builder responseClientOperationDurationBuilder = this.clientOperationDurationBuilder
                .addTag(Tag.create("gen_ai.operation.name", "chat"))
                .addTag(Tag.create("gen_ai.request.model", chatModelRequest.model()))
                .addTag(Tag.create("gen_ai.response.model", chatModelResponse.model()));
        this.clientOperationDuration = this.meterRegistry.getOrCreate(responseClientOperationDurationBuilder);
        System.out.println("** onResponse: Recording clientOperationDuration ***");
        this.clientOperationDuration.record(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
    }

    @Override
    public void onError(ChatModelErrorContext chatModelErrorContext) {
        final long endTime = System.nanoTime();
        final long startTime = (Long) chatModelErrorContext.attributes().get(GEN_AI_CLIENT_OPERATION_START_TIME);

        final ChatModelRequest chatModelRequest = chatModelErrorContext.request();

        String sb = chatModelErrorContext.error().getClass().getName()
                + ";" + chatModelErrorContext.error().getLocalizedMessage();

        DistributionSummary.Builder errorClientOperationDurationBuilder = this.clientOperationDurationBuilder
                .addTag(Tag.create("gen_ai.operation.name", "chat"))
                .addTag(Tag.create("gen_ai.request.model", chatModelRequest.model()))
                .addTag(Tag.create("error.type", sb));
        this.clientOperationDuration = this.meterRegistry.getOrCreate(errorClientOperationDurationBuilder);
        System.out.println("** onError: Recording clientOperationDuration ***");
        this.clientOperationDuration.record(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
    }
}
