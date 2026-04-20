package no.novari.fintkontrollorgunitservice.orgunit

import jakarta.persistence.EntityNotFoundException
import no.fintlabs.opa.AuthorizationClient
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnitType.ALLORGUNITS
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class OrgUnitService(
    private val orgUnitRepository: OrgUnitRepository,
    private val orgUnitMappingService: OrgUnitMappingService,
    private val authorizationClient: AuthorizationClient,
) {
    fun findAllOrgUnitsPagedForApiRespons(pageRequest: Pageable): Page<OrgUnitApiDTO> {
        val allOrgUnits = orgUnitRepository.findAll(pageRequest)

        return allOrgUnits.map { orgUnitMappingService.mapOrgUnitToOrgUnitApiDTO(it) }
    }

    fun findOrgunitsByScope(
        pageRequest: Pageable,
        search: String,
    ): Page<OrgUnitApiDTO> {
        val allowedOrgUnits = getAllowedOrgUnitIds()

        val orgUnitsInScope =
            if (hasALLORGUNITSinScope(allowedOrgUnits)) {
                orgUnitRepository.findAllByNameContainingIgnoreCase(search, pageRequest)
            } else {
                orgUnitRepository.findOrgUnitsByNameAndOrOrganisationUnitId(search, allowedOrgUnits, pageRequest)
            }
        return orgUnitsInScope.map { orgUnitMappingService.mapOrgUnitToOrgUnitApiDTO(it) }
    }

    fun getAllowedOrgUnitIds(): Set<String> {
        return authorizationClient.userScopesList
            .filter { scope ->
                scope.objectType.equals(ALLORGUNITS.name, ignoreCase = true) ||
                    scope.objectType.equals("orgunit", ignoreCase = true)
            }.flatMap { scope -> scope.orgUnits.orEmpty().asSequence() }
            .toSet()
    }

    fun hasALLORGUNITSinScope(allowdOrgUnitIDs: Set<String>): Boolean = allowdOrgUnitIDs.contains(ALLORGUNITS.name)

    fun findOrgUnitById(id: Long): OrgUnit {
        return orgUnitRepository.findById(id).orElseThrow { EntityNotFoundException("OrgUnit with id $id not found") }
    }

    fun findParentOrgUnitsById(id: Long): List<OrgUnit> = orgUnitRepository.findParentOrgUnitsByOrganisationUnitId(id)

    fun findChildrenOrgUnitsById(id: Long): List<OrgUnit> =
        orgUnitRepository.findChildrenOrgUnitsByOrganisationUnitId(id)
}
