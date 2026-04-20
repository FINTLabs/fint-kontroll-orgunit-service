package no.novari.fintkontrollorgunitservice.orgunit

import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.math.max

private val logger = LoggerFactory.getLogger(OrgUnitController::class.java)

@RestController
@RequestMapping("/api/orgunits")
class OrgUnitController(
    val orgUnitService: OrgUnitService,
    val orgUnitResponsService: OrgUnitResponsService,
    val orgUnitMappingService: OrgUnitMappingService,
) {
    @GetMapping()
    fun getAllOrgUnits(
        @RequestParam(value = "search", defaultValue = "%") search: String,
        @ParameterObject
        @PageableDefault(page = 0, size = 10) pageRequest: Pageable,
    ): ResponseEntity<Map<String, Any>> {
        val orgUnitsForApiSeachAndPaged =
            orgUnitService.findOrgunitsByScope(
                pageRequest,
                search,
            )

        return orgUnitResponsService.pageResponse(orgUnitsForApiSeachAndPaged)
    }

    @GetMapping("/{id}")
    fun getOrgUnitById(
        @PathVariable id: Long,
    ): ResponseEntity<OrgUnit> {
        val orgUnit = orgUnitService.findOrgUnitById(id)

        return orgUnitResponsService.toResponseEntity(orgUnit)
    }

    @GetMapping("/{id}/parents")
    fun getParentOrgUnits(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting parent orgunits for orgunit with id: $id")
        val parentOrgUnitsDTOs: List<OrgUnitApiDTO> =
            orgUnitMappingService
                .mapOrgunitToOrgUnitApiDTO(orgUnitService.findParentOrgUnitsById(id))

        logger.debug("Mapped ${parentOrgUnitsDTOs.size} parent orgunits for orgunit with id $id")

        return orgUnitResponsService.pageResponse(parentOrgUnitsDTOs, 0, max(parentOrgUnitsDTOs.size, 1))
    }

    @GetMapping("/{id}/children")
    fun getChildrenOrgUnits(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting children orgunits for orgunit with id: $id")
        val childrenOrgUnitsDTOs: List<OrgUnitApiDTO> =
            orgUnitMappingService.mapOrgunitToOrgUnitApiDTO(orgUnitService.findChildrenOrgUnitsById(id))
        logger.info("Mapped ${childrenOrgUnitsDTOs.size} children orgunits for orgunit with id $id")

        return orgUnitResponsService.pageResponse(childrenOrgUnitsDTOs, 0, max(childrenOrgUnitsDTOs.size, 1))
    }
}
