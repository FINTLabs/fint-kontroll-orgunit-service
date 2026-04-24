package no.novari.fintkontrollorgunitservice.orgunitdistance

import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

private val logger = LoggerFactory.getLogger(OrgUnitDistanceProducerService::class.java)

@Service
class OrgUnitDistanceProducerService(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
) {
    private val parameterizedTemplate: ParameterizedTemplate<OrgunitDistance> =
        parameterizedTemplateFactory.createTemplate(OrgunitDistance::class.java)

    private fun topicNamePrefixParameters() =
        TopicNamePrefixParameters
            .stepBuilder()
            .orgIdApplicationDefault()
            .domainContextApplicationDefault()
            .build()

    private val entityTopicNameParameters: EntityTopicNameParameters =
        EntityTopicNameParameters
            .builder()
            .topicNamePrefixParameters(topicNamePrefixParameters())
            .resourceName("orgunitdistance")
            .build()

    private fun entityTopicConfiguration() =
        EntityTopicConfiguration
            .stepBuilder()
            .partitions(1)
            .lastValueRetainedForever()
            .nullValueRetentionTime(Duration.ofDays(10))
            .cleanupFrequency(EntityCleanupFrequency.NORMAL)
            .build()

    init {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration(),
        )
    }

    fun publish(allOrgUnitDistances: List<OrgunitDistance>): List<OrgunitDistance> {
        logger.info("Publishing ${allOrgUnitDistances.size} orgunit distances")

        allOrgUnitDistances.forEach { orgUnitDistance ->

            logger.debug("Publishing orgunit distance: ${orgUnitDistance.id}")
            publish(orgUnitDistance)
        }
        return allOrgUnitDistances
    }

    fun publish(orgUnitDistance: OrgunitDistance) {
        val producerRecord =
            ParameterizedProducerRecord
                .builder<OrgunitDistance>()
                .topicNameParameters(entityTopicNameParameters)
                .key(orgUnitDistance.id)
                .value(orgUnitDistance)
                .build()
        parameterizedTemplate.send(producerRecord)
        logger.info("Published orgunit distance to kafka: ${orgUnitDistance.id}")
    }
}
