package no.novari.fintkontrollorgunitservice.organisasjonsenhet

import no.novari.fintkontrollorgunitservice.organisasjonselementResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrganisasjonselementServiceTest {
    private val service = OrganisasjonselementService()

    @Test
    fun `extracts parent id from overordnet link`() {
        val resource =
            organisasjonselementResource(
                parentHref = "https://api.felleskomponent.no/organisasjonselement/systemId/PARENT-1",
            )

        val result = service.getParentOrganisasjonselementOrganisasjonsId(resource)

        assertEquals("PARENT-1", result)
    }

    @Test
    fun `returns empty parent when overordnet is missing`() {
        val result =
            service.getParentOrganisasjonselementOrganisasjonsId(
                organisasjonselementResource(parentHref = null),
            )

        assertEquals("", result)
    }

    @Test
    fun `returns normalized resource id from self link`() {
        val resource =
            organisasjonselementResource(
                selfHref = "https://api.felleskomponent.no/organisasjonselement/systemId/ORG-1",
            )

        val result = service.getResourceId(resource)

        assertEquals("https://api.felleskomponent.no/organisasjonselement/systemid/ORG-1", result)
    }

    @Test
    fun `extracts children ids from underordnet links`() {
        val resource =
            organisasjonselementResource(
                childHrefs =
                    listOf(
                        "https://api.felleskomponent.no/organisasjonselement/systemId/CHILD-1",
                        "https://api.felleskomponent.no/organisasjonselement/systemId/CHILD-2",
                    ),
            )

        val result = service.getChildrenOrganisasjonselementOrganisasjonsId(resource)

        assertEquals(listOf("CHILD-1", "CHILD-2"), result)
    }

    @Test
    fun `getAllSubOrgUnitsRefs includes self id first and then children`() {
        val resource =
            organisasjonselementResource(
                organisasjonsIdValue = "ORG-1",
                childHrefs =
                    listOf(
                        "https://api.felleskomponent.no/organisasjonselement/systemId/CHILD-1",
                        "https://api.felleskomponent.no/organisasjonselement/systemId/CHILD-2",
                    ),
            )

        val result = service.getAllSubOrgUnitsRefs(resource)

        assertEquals(listOf("ORG-1", "CHILD-1", "CHILD-2"), result)
    }
}
