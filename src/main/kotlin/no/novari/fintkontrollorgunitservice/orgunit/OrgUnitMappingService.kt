package no.novari.fintkontrollorgunitservice.orgunit

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitservice.organisasjonsenhet.OrganisasjonselementService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrgUnitMappingService::class.java)

@Service
class OrgUnitMappingService(
    private val organisasjonselementService: OrganisasjonselementService,
    private val orgUnitService: OrgUnitService,
) {
    fun mapOrganisasjonsenhetToOrgUnit(
        organisasjonselement: OrganisasjonselementResource,
        existingOrgUnit: OrgUnit? = null,
    ): OrgUnit {
        logger.info("Mapping organisasjonselement to orgUnit. OrganisasjonsId: ${organisasjonselement.organisasjonsId}")

        val parent =
            organisasjonselementService
                .getParentOrganisasjonselementOrganisasjonsId(organisasjonselement)
        val children =
            organisasjonselementService
                .getChildrenOrganisasjonselementOrganisasjonsId(organisasjonselement)
        val manager = organisasjonselement.leder.toString()

        val base =
            existingOrgUnit ?: OrgUnit(
                resourceId = organisasjonselementService.getResourceId(organisasjonselement),
                shortName = organisasjonselement.kortnavn,
                organisationUnitId = organisasjonselement.organisasjonsId.identifikatorverdi,
                name = organisasjonselement.navn,
                parentRef = parent,
                childrenRef = children as MutableList<String>,
                managerRef = manager,
            )

        return base.copy(
            resourceId = organisasjonselementService.getResourceId(organisasjonselement),
            shortName = organisasjonselement.kortnavn,
            organisationUnitId = organisasjonselement.organisasjonsId.identifikatorverdi ?: base.organisationUnitId,
            name = organisasjonselement.navn ?: base.name,
            parentRef = parent,
            childrenRef = children as MutableList<String>,
            managerRef = manager,
        )
    }

    fun mapOrgUnitToOrgUnitDTO(orgUnit: OrgUnit): OrgUnitDTO {
        // val subOrgUnits = listOf(orgUnit.organisationUnitId) + orgUnit.childrenRef
        val allSubOrgUnits = orgUnitService.getAllDescendantOrganisationUnitIds(orgUnit.organisationUnitId)

        return OrgUnitDTO(
            id = orgUnit.id ?: 0,
            resourceId = orgUnit.resourceId,
            organisationUnitId = orgUnit.organisationUnitId,
            name = orgUnit.name,
            shortName = orgUnit.shortName,
            parentRef = orgUnit.parentRef,
            managerRef = orgUnit.managerRef,
            childrenRef = orgUnit.childrenRef,
            allSubOrgUnitsRef = allSubOrgUnits as MutableList<String>,
        )
    }
}
