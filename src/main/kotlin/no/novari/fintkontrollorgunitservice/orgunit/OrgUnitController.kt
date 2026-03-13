package no.novari.fintkontrollorgunitservice.orgunit

import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(OrgUnitController::class.java)

@RestController
@RequestMapping("/api/orgunits")
class OrgUnitController(
    val orgUnitService: OrgUnitService,
    val orgUnitResponsService: OrgUnitResponsService,
) {
    @GetMapping()
    public fun getAllOrgUnits(
        @ParameterObject @PageableDefault(page = 0, size = 10) pageRequest: Pageable,
    ): ResponseEntity<Map<String, Any>> {
        val orgUnitsPaged: Page<OrgUnitApiDTO> = orgUnitService.findAllOrgUnitsPaged(pageRequest)

        return orgUnitResponsService.pageResponse(orgUnitsPaged)
    }
}
