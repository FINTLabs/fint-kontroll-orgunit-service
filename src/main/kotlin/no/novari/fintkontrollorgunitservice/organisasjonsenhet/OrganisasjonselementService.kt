package no.novari.fintkontrollorgunitservice.organisasjonsenhet

import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitservice.FintLinkUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrganisasjonselementService::class.java)

@Service
class OrganisasjonselementService {
    fun getParentOrganisasjonselementOrganisasjonsId(
        organisasjonselementResource: OrganisasjonselementResource,
    ): String {
        return organisasjonselementResource.organisasjonsnummer.toString()
    }

    fun getResourceId(organisasjonselement: OrganisasjonselementResource): String {
        return FintLinkUtils.getFirstSelfLink(organisasjonselement)
    }

    fun getChildrenOrganisasjonselementOrganisasjonsId(resource: OrganisasjonselementResource): List<String> {
        val children = resource.underordnet.map { it.href }.map { href -> href.substringAfterLast("/") }
        logger.info("Children: $children")
        return children
    }

    fun getAllSubOrgUnitsRefs(resource: OrganisasjonselementResource): List<String> {
        val allSubOrgUnits =
            listOf(resource.organisasjonsId.identifikatorverdi) +
                resource.underordnet.map { it.href }.map { href -> href.substringAfterLast("/") }
        logger.info("AllSubOrgUnits: $allSubOrgUnits")
        return allSubOrgUnits
    }
}
