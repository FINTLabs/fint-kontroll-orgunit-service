package no.novari.fintkontrollorgunitservice.orgunit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class OrgUnitService(
    private val orgUnitRepository: OrgUnitRepository,
    private val orgUnitMappingService: OrgUnitMappingService,
) {
    fun findAllOrgUnitsPaged(pageRequest: Pageable): Page<OrgUnitApiDTO> {
        val allOrgUnits = orgUnitRepository.findAll(pageRequest)
        return allOrgUnits.map { orgUnitMappingService.mapOrgUnitToOrgUnitApiDTO(it) }
    }
}
