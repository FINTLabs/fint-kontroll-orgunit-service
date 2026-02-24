package no.novari.fintkontrollorgunitfactoryv2.organisasjonsenhet

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.cache.FintCache
import no.novari.fintkontrollorgunitfactoryv2.FintLinkUtils
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import kotlin.reflect.KClass

private val logger = LoggerFactory.getLogger(OrganisasjonsenhetConsumer::class.java)

@Configuration
class OrganisasjonsenhetConsumer(
    private val organisasjonselementCache: FintCache<String, OrganisasjonselementResource>,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
    private val errorHandlerFactory: ErrorHandlerFactory,
) {
    @Bean
    fun organisasjonselementConsumer() =
        createContainer(
            topicName = "administrasjon-organisasjon-organisasjonselement",
            consumingClass = OrganisasjonselementResource::class,
            cache = organisasjonselementCache,
        )

    private fun <T : Any> createContainer(
        topicName: String,
        consumingClass: KClass<T>,
        cache: FintCache<String, T>,
    ): ConcurrentMessageListenerContainer<String, T> {
        val nameParameters =
            EntityTopicNameParameters
                .builder()
                .resourceName(topicName)
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).build()

        val listenerConfiguration =
            ListenerConfiguration
                .stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .continueFromPreviousOffsetOnAssignment()
                .build()

        val errorHandler =
            errorHandlerFactory.createErrorHandler<T>(
                ErrorHandlerConfiguration
                    .stepBuilder<T>()
                    .noRetries()
                    .skipFailedRecords()
                    .build(),
            )

        var listenerContainerFactory =
            parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                consumingClass.java,
                { record: ConsumerRecord<String, T> ->
                    val key = FintLinkUtils.getSystemIdFromMessageKey(record.key())
                    val value = record.value()
                    cache.put(key, value)
                    logger.info("Added $key to cache. With value $value")
                },
                listenerConfiguration,
                errorHandler,
            )
        return listenerContainerFactory.createContainer(nameParameters)
    }
}
