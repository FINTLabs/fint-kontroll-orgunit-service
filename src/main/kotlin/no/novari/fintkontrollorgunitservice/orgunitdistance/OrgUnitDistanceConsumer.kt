package no.novari.fintkontrollorgunitservice.orgunitdistance

import no.novari.cache.FintCache
import no.novari.fintkontrollorgunitservice.kafka.KafkaContainerFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = LoggerFactory.getLogger(OrgUnitDistanceConsumer::class.java)

@Configuration
class OrgUnitDistanceConsumer(
    private val orgUnitDistanceCache: FintCache<String, OrgunitDistance>,
    private val kafkaContainerFactory: KafkaContainerFactory,
) {
    @Bean
    fun orgUnitDistanceListenerContainer() =
        kafkaContainerFactory.createContainer(
            "orgunitdistance",
            OrgunitDistance::class,
            orgUnitDistanceCache,
        ) { key, value ->
            logger.debug("Consumed published orgUnitDistance with key: $key  ")
        }
}
