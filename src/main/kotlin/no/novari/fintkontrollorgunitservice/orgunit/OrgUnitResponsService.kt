package no.novari.fintkontrollorgunitservice.orgunit

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class OrgUnitResponsService {
    fun pageResponse(page: Page<OrgUnitApiDTO>): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(
            mapOf(
                "orgunits" to page.content,
                "totalElements" to page.totalElements,
                "totalPages" to page.totalPages,
                "currentPage" to page.number,
                "itemsInPage" to page.numberOfElements,
            ),
        )

    fun toResponseEntity(orgUnit: OrgUnit?): ResponseEntity<OrgUnit> =
        orgUnit?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}
