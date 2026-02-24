package no.novari.fintkontrollorgunitservice.organisasjonsenhet

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.cache.FintCache
import no.novari.fintkontrollorgunitservice.FintLinkUtils
import org.springframework.stereotype.Service

@Service
class OrganisasjonselementService(
    private val organisasjonselementCache: FintCache<String, OrganisasjonselementResource>,
) {
    fun getParentOrganisasjonselementOrganisasjonsId(
        organisasjonselementResource: OrganisasjonselementResource,
    ): String {
        return organisasjonselementResource.organisasjonsnummer.toString()
    }

    fun getResourceId(organisasjonselement: OrganisasjonselementResource): String {
        return FintLinkUtils.getFirstSelfLink(organisasjonselement)
    }

    fun getChildrenOrganisasjonselementUnitResourceOrganisasjonsId(
        resource: OrganisasjonselementResource,
    ): List<String> {
        val parentHref = FintLinkUtils.getFirstSelfLink(resource)
        val parent =
            organisasjonselementCache
                .get(FintLinkUtils.organisasjonsIdToLowerCase(parentHref))

        return parent.underordnet
            .map { it.href }
            .map { href ->
                organisasjonselementCache
                    .get(FintLinkUtils.organisasjonsIdToLowerCase(href))
                    .organisasjonsId.identifikatorverdi
            }
    }

    fun getAllSubOrgUnitsRefs(resource: OrganisasjonselementResource): List<String> =
        listOf(resource.organisasjonsId.identifikatorverdi) +
            findSubOrgUnits(resource)
                .flatMap { getAllSubOrgUnitsRefs(it) }

    private fun findSubOrgUnitRefs(
        organisasjonselementResource: OrganisasjonselementResource,
        allSubOrgUnitRefs: MutableList<String>,
    ) {
        allSubOrgUnitRefs.add(
            organisasjonselementResource.organisasjonsId.identifikatorverdi,
        )

        findSubOrgUnits(organisasjonselementResource)
            .forEach { orgUnit ->
                findSubOrgUnitRefs(orgUnit, allSubOrgUnitRefs)
            }
    }

    private fun findSubOrgUnits(
        organisasjonselementResource: OrganisasjonselementResource,
    ): List<OrganisasjonselementResource> {
        val organisasjonsenhetHref =
            FintLinkUtils.getFirstSelfLink(organisasjonselementResource)

        return organisasjonselementCache
            .get(FintLinkUtils.organisasjonsIdToLowerCase(organisasjonsenhetHref))
            .underordnet
            .map { it.href }
            .map { href ->
                organisasjonselementCache.get(
                    FintLinkUtils.organisasjonsIdToLowerCase(href),
                )
            }
    }
}
