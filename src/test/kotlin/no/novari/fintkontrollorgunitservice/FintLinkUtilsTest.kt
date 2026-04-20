package no.novari.fintkontrollorgunitservice

import io.mockk.every
import io.mockk.mockk
import no.fint.model.resource.FintLinks
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fintkontrollorgunitservice.organisasjonsenhet.NoSuchLinkException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.function.Supplier

class FintLinkUtilsTest {
    @Test
    fun `getSystemIdFromMessageKey returns last path segment`() {
        val result = FintLinkUtils.getSystemIdFromMessageKey("/resource/path/ORG-123")

        assertEquals("ORG-123", result)
    }

    @Test
    fun `getFirstLink returns normalized first href`() {
        val resource = OrganisasjonselementResource()
        val supplier =
            Supplier {
                listOf(Link.with("https://api.felleskomponent.no/organisasjonselement/systemId/ORG-123"))
            }

        val result = FintLinkUtils.getFirstLink(supplier, resource, "organisasjonselement")

        assertEquals("https://api.felleskomponent.no/organisasjonselement/systemid/ORG-123", result)
    }

    @Test
    fun `getFirstLink throws when no links exist`() {
        val resource = OrganisasjonselementResource()

        assertThrows(NoSuchLinkException::class.java) {
            FintLinkUtils.getFirstLink(Supplier { emptyList() }, resource, "organisasjonselement")
        }
    }

    @Test
    fun `getFirstSelfLink returns normalized self link`() {
        val resource =
            OrganisasjonselementResource().apply {
                addSelf(Link.with("https://api.felleskomponent.no/organisasjonselement/systemId/ORG-123"))
            }

        val result = FintLinkUtils.getFirstSelfLink(resource)

        assertEquals("https://api.felleskomponent.no/organisasjonselement/systemid/ORG-123", result)
    }

    @Test
    fun `getFirstSelfLink throws when self link is missing`() {
        val resource = mockk<FintLinks>()
        every { resource.selfLinks } returns emptyList()

        assertThrows(NoSuchLinkException::class.java) {
            FintLinkUtils.getFirstSelfLink(resource)
        }
    }

    @Test
    fun `case helpers normalize expected path segments`() {
        assertEquals(
            "https://example.no/organisasjonselement/systemid/ORG-123",
            FintLinkUtils.systemIdToLowerCase("https://example.no/organisasjonselement/systemId/ORG-123"),
        )
        assertEquals(
            "https://example.no/organisasjon/organisasjonsid/42",
            FintLinkUtils.organisasjonsIdToLowerCase("https://example.no/organisasjon/organisasjonsId/42"),
        )
    }
}
