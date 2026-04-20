package no.novari.fintkontrollorgunitservice.orgunit

import io.mockk.every
import io.mockk.mockk
import no.novari.fintkontrollorgunitservice.orgUnit
import no.novari.fintkontrollorgunitservice.organisasjonselementResource
import no.novari.fintkontrollorgunitservice.organisasjonsenhet.OrganisasjonselementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrgUnitMappingServiceTest {
    private val organisasjonselementService = mockk<OrganisasjonselementService>()
    private val orgUnitSubOrgUnitService = mockk<OrgUnitSubOrgUnitService>()
    private val service = OrgUnitMappingService(organisasjonselementService, orgUnitSubOrgUnitService)

    @Test
    fun `maps organisasjonselement to new org unit`() {
        val resource =
            organisasjonselementResource(
                organisasjonsIdValue = "ORG-1",
                navn = "Name",
                kortnavn = "Short",
            )
        every {
            organisasjonselementService.getParentOrganisasjonselementOrganisasjonsId(resource)
        } returns "PARENT-1"
        every {
            organisasjonselementService.getChildrenOrganisasjonselementOrganisasjonsId(resource)
        } returns listOf("CHILD-1", "CHILD-2")
        every { organisasjonselementService.getResourceId(resource) } returns "resource-1"

        val result = service.mapOrganisasjonsenhetToOrgUnit(resource)

        assertEquals(
            orgUnit(
                id = null,
                resourceId = "resource-1",
                organisationUnitId = "ORG-1",
                name = "Name",
                shortName = "Short",
                parentRef = "PARENT-1",
                managerRef = "[https://api.felleskomponent.no/person/systemId/LEADER-1]",
                childrenRef = mutableListOf("CHILD-1", "CHILD-2"),
            ),
            result,
        )
    }

    @Test
    fun `keeps existing name and organisationUnitId when incoming values are null`() {
        val resource =
            organisasjonselementResource(
                organisasjonsIdValue = null,
                navn = null,
                kortnavn = "Updated",
            )
        val existing =
            orgUnit(
                id = 10L,
                resourceId = "old-resource",
                organisationUnitId = "ORG-EXISTING",
                name = "Existing name",
                shortName = "Old",
                parentRef = "OLD-PARENT",
                managerRef = "OLD-MANAGER",
                childrenRef = mutableListOf("OLD-CHILD"),
            )
        every {
            organisasjonselementService.getParentOrganisasjonselementOrganisasjonsId(resource)
        } returns ""
        every {
            organisasjonselementService.getChildrenOrganisasjonselementOrganisasjonsId(resource)
        } returns mutableListOf()
        every { organisasjonselementService.getResourceId(resource) } returns "new-resource"

        val result = service.mapOrganisasjonsenhetToOrgUnit(resource, existing)

        assertEquals(
            existing.copy(
                resourceId = "new-resource",
                shortName = "Updated",
                organisationUnitId = "ORG-EXISTING",
                name = "Existing name",
                parentRef = "",
                childrenRef = mutableListOf(),
                managerRef = "[https://api.felleskomponent.no/person/systemId/LEADER-1]",
            ),
            result,
        )
    }

    @Test
    fun `maps org unit to kafka dto with descendant references`() {
        val orgUnit = orgUnit(id = 2L, organisationUnitId = "ORG-2", childrenRef = mutableListOf("CHILD-1"))
        every {
            orgUnitSubOrgUnitService.getAllDescendantOrganisationUnitIds("ORG-2")
        } returns listOf("CHILD-1", "CHILD-2")

        val result = service.mapOrgUnitToOrgUnitKafkaDTO(orgUnit)

        assertEquals(
            OrgUnitKafkaDTO(
                id = 2L,
                resourceId = "resource-1",
                organisationUnitId = "ORG-2",
                name = "Org unit",
                shortName = "OU",
                parentRef = "PARENT-1",
                managerRef = "MANAGER-1",
                childrenRef = mutableListOf("CHILD-1"),
                allSubOrgUnitsRef = mutableListOf("CHILD-1", "CHILD-2"),
            ),
            result,
        )
    }

    @Test
    fun `maps org units to api dtos`() {
        val orgUnits =
            listOf(
                orgUnit(
                    id = 1L,
                    organisationUnitId = "ORG-1",
                    name = "One",
                    parentRef = "",
                    childrenRef = mutableListOf("CHILD-1"),
                ),
                orgUnit(
                    id = 2L,
                    organisationUnitId = "ORG-2",
                    name = "Two",
                    parentRef = "ORG-1",
                    childrenRef = mutableListOf(),
                ),
            )

        val result = service.mapOrgunitToOrgUnitApiDTO(orgUnits)

        assertEquals(
            listOf(
                OrgUnitApiDTO(
                    1L,
                    "One",
                    "ORG-1",
                    "",
                    mutableListOf("CHILD-1"),
                ),
                OrgUnitApiDTO(
                    2L,
                    "Two",
                    "ORG-2",
                    "ORG-1",
                    mutableListOf(),
                ),
            ),
            result,
        )
    }
}
