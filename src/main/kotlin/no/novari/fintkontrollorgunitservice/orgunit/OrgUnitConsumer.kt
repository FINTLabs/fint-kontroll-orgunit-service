package no.novari.fintkontrollorgunitservice.orgunit

import no.novari.cache.FintCache
import no.novari.fintkontrollorgunitservice.kafka.KafkaContainerFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = LoggerFactory.getLogger(OrgUnitConsumer::class.java)

@Configuration
class OrgUnitConsumer(
    private val orgUnitCache: FintCache<String, OrgUnit>,
    private val kafkaContainerFactory: KafkaContainerFactory,
) {
    @Bean
    fun orgUnitListenerContainer() =
        kafkaContainerFactory.createContainer(
            "orgunit",
            OrgUnit::class,
            orgUnitCache,
            { key, value ->
                logger.info("Consumed published orgUnit with key: $key  ")
            },
        )
}
