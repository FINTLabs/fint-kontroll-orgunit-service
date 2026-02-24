package no.novari.fintkontrollorgunitfactoryv2.orgunit

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitfactoryv2.organisasjonsenhet.OrganisasjonselementService
import org.springframework.stereotype.Service

@Service
class OrgUnitMappingService(
    private val orgUnitMappingService: OrgUnitMappingService,
    private val organisasjonselementService: OrganisasjonselementService,
) {
    fun mapOrganisasjonsenhetToOrgUnit(organisasjonselement: OrganisasjonselementResource): OrgUnit {
        val parent =
            organisasjonselementService
                .getParentOrganisasjonselementOrganisasjonsId(organisasjonselement)
        val children =
            organisasjonselementService
                .getChildrenOrganisasjonselementUnitResourceOrganisasjonsId(organisasjonselement)
        val suborgunits = organisasjonselementService.getAllSubOrgUnitsRefs(organisasjonselement)
        val mappedOrgUnit: OrgUnit =
            OrgUnit(
                id = null,
                resourceId = organisasjonselementService.getResourceId(organisasjonselement),
                shortName = organisasjonselement.kortnavn,
                organisationUnitId = organisasjonselement.organisasjonsId.identifikatorverdi,
                name = organisasjonselement.navn,
                parentRef = parent,
                childrenRef = children,
                allSubOrgUnitsRef = suborgunits,
                managerRef = "sjef",
            )

        return mappedOrgUnit
    }
}
