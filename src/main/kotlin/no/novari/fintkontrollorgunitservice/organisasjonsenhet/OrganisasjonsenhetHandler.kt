package no.novari.fintkontrollorgunitservice.organisasjonsenhet

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnitMappingService
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnitPublishingService
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnitRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrganisasjonsenhetHandler::class.java)

@Service
class OrganisasjonsenhetHandler(
    private val orgUnitRepository: OrgUnitRepository,
    private val orgUnitMappingService: OrgUnitMappingService,
    private val orgUnitPublishingService: OrgUnitPublishingService,
) {
    fun handle(
        key: String,
        kafkaValue: OrganisasjonselementResource,
    ) {
        kafkaValue.organisasjonsId
            ?.identifikatorverdi
            ?.takeIf { it.isNotBlank() }
            ?: run {
                logger.warn(
                    "Skipping record because organisasjonsId.identifikatorverdi is missing/blank. key:: $key navn:: ${kafkaValue.navn}",
                )
                return
            }

        val existOrgUnit = orgUnitRepository.findByOrganisationUnitId(kafkaValue.organisasjonsId.identifikatorverdi)
        val mappedOrgUnit =
            orgUnitMappingService.mapOrganisasjonsenhetToOrgUnit(kafkaValue, existOrgUnit)
        orgUnitRepository.save(mappedOrgUnit)
        orgUnitPublishingService.publishOrgUnit(orgUnitMappingService.mapOrgUnitToOrgUnitDTO(mappedOrgUnit))
    }
}
