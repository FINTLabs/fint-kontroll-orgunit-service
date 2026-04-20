package no.novari.fintkontrollorgunitservice.orgunit

import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.EntityNotFoundException
import no.fintlabs.opa.AuthorizationClient
import no.fintlabs.opa.model.Scope
import no.novari.fintkontrollorgunitservice.orgUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class OrgUnitServiceTest {
    private val orgUnitRepository = mockk<OrgUnitRepository>()
    private val orgUnitMappingService = mockk<OrgUnitMappingService>()
    private val authorizationClient = mockk<AuthorizationClient>()
    private val service = OrgUnitService(orgUnitRepository, orgUnitMappingService, authorizationClient)

    @Test
    fun `getAllowedOrgUnitIds filters supported scopes and deduplicates ids`() {
        every { authorizationClient.userScopesList } returns
            listOf(
                Scope("orgunit", listOf("ORG-1", "ORG-2")),
                Scope("allorgunits", listOf("ALLORGUNITS", "ORG-2")),
                Scope("school", listOf("SCHOOL-1")),
                Scope("ORGUNIT", null),
            )

        val result = service.getAllowedOrgUnitIds()

        assertEquals(setOf("ORG-1", "ORG-2", "ALLORGUNITS"), result)
    }

    @Test
    fun `findOrgunitsByScope uses unrestricted query when ALLORGUNITS is present`() {
        val pageRequest = PageRequest.of(0, 10)
        val orgUnit = orgUnit(id = 1L, name = "Alpha")
        every {
            authorizationClient.userScopesList
        } returns listOf(Scope("allorgunits", listOf("ALLORGUNITS")))
        every {
            orgUnitRepository.findAllByNameContainingIgnoreCase("alp", pageRequest)
        } returns PageImpl(listOf(orgUnit))
        every { orgUnitMappingService.mapOrgUnitToOrgUnitApiDTO(orgUnit) } returns
            OrgUnitApiDTO(1L, "Alpha", "ORG-1", "PARENT-1", mutableListOf("CHILD-1"))

        val result = service.findOrgunitsByScope(pageRequest, "alp")

        assertEquals(1, result.totalElements)
        assertEquals("Alpha", result.content.single().name)
    }

    @Test
    fun `findOrgunitsByScope uses scoped query when ALLORGUNITS is absent`() {
        val pageRequest = PageRequest.of(0, 10)
        val allowedIds = listOf("ORG-1", "ORG-2")
        val orgUnit = orgUnit(id = 2L, organisationUnitId = "ORG-2", name = "Beta")
        every {
            authorizationClient.userScopesList
        } returns listOf(Scope("orgunit", allowedIds))
        every {
            orgUnitRepository.findOrgUnitsByNameAndOrOrganisationUnitId(
                "bet",
                allowedIds.toSet(),
                pageRequest,
            )
        } returns PageImpl(listOf(orgUnit))
        every { orgUnitMappingService.mapOrgUnitToOrgUnitApiDTO(orgUnit) } returns
            OrgUnitApiDTO(2L, "Beta", "ORG-2", "PARENT-1", mutableListOf("CHILD-1"))

        val result = service.findOrgunitsByScope(pageRequest, "bet")

        assertEquals(1, result.totalElements)
        assertEquals("ORG-2", result.content.single().organisationUnitId)
    }

    @Test
    fun `findOrgUnitById throws when repository has no entity`() {
        every { orgUnitRepository.findById(99L) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            service.findOrgUnitById(99L)
        }
    }
}
