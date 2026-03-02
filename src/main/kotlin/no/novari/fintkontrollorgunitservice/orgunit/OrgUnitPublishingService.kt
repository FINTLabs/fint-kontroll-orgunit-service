package no.novari.fintkontrollorgunitservice.orgunit

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

private val logger = LoggerFactory.getLogger(OrgUnitPublishingService::class.java)

@Service
class OrgUnitPublishingService(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
) {
    private val parameterizedTemplat: ParameterizedTemplate<OrgUnitDTO> =
        parameterizedTemplateFactory.createTemplate(OrgUnitDTO::class.java)

    private val entityTopicNameParameters: EntityTopicNameParameters =
        EntityTopicNameParameters
            .builder()
            .resourceName("orgunit")
            .topicNamePrefixParameters(topicNameParameters())
            .build()

    private fun topicNameParameters() =
        TopicNamePrefixParameters
            .stepBuilder()
            .orgIdApplicationDefault()
            .domainContextApplicationDefault()
            .build()

    fun entityTopicConfiguration() =
        EntityTopicConfiguration
            .stepBuilder()
            .partitions(1)
            .lastValueRetentionTime(Duration.ofDays(10))
            .nullValueRetentionTime(Duration.ofDays(10))
            .cleanupFrequency(EntityCleanupFrequency.NORMAL)
            .build()

    init {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration(),
        )
    }

    fun publishOrgUnit(orgUnitDTO: OrgUnitDTO) {
        val producerRecord =
            ParameterizedProducerRecord
                .builder<OrgUnitDTO>()
                .topicNameParameters(entityTopicNameParameters)
                .key(orgUnitDTO.organisationUnitId)
                .value(orgUnitDTO)
                .build()

        parameterizedTemplat.send(producerRecord)
        logger.info("Published orgUnit: ${orgUnitDTO.organisationUnitId} :: ${orgUnitDTO.name}")
    }
}
