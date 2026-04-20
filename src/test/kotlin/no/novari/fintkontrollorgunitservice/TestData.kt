package no.novari.fintkontrollorgunitservice

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnit

fun identifikator(value: String?): Identifikator =
    Identifikator().apply {
        identifikatorverdi = value
    }

fun organisasjonselementResource(
    organisasjonsIdValue: String? = "ORG-1",
    navn: String? = "Org unit",
    kortnavn: String? = "OU",
    selfHref: String = "https://api.felleskomponent.no/organisasjonselement/systemId/ORG-1",
    parentHref: String? = null,
    childHrefs: List<String> = emptyList(),
    leaderHref: String? = "https://api.felleskomponent.no/person/systemId/LEADER-1",
): OrganisasjonselementResource =
    OrganisasjonselementResource().apply {
        organisasjonsId = identifikator(organisasjonsIdValue)
        this.navn = navn
        this.kortnavn = kortnavn
        addSelf(Link.with(selfHref))
        parentHref?.let { addOverordnet(Link.with(it)) }
        childHrefs.forEach { addUnderordnet(Link.with(it)) }
        leaderHref?.let { addLeder(Link.with(it)) }
    }

fun orgUnit(
    id: Long? = 1L,
    resourceId: String = "resource-1",
    organisationUnitId: String = "ORG-1",
    name: String = "Org unit",
    shortName: String? = "OU",
    parentRef: String = "PARENT-1",
    managerRef: String? = "MANAGER-1",
    childrenRef: MutableList<String> = mutableListOf("CHILD-1"),
): OrgUnit =
    OrgUnit(
        id = id,
        resourceId = resourceId,
        organisationUnitId = organisationUnitId,
        name = name,
        shortName = shortName,
        parentRef = parentRef,
        managerRef = managerRef,
        childrenRef = childrenRef,
    )
