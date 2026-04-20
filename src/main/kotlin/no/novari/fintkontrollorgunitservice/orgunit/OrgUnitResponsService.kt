package no.novari.fintkontrollorgunitservice.orgunit

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.math.min

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

    fun pageResponse(
        orgUnits: List<OrgUnitApiDTO>,
        page: Int,
        size: Int,
    ): ResponseEntity<Map<String, Any>> {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceAtLeast(1)
        val start = safePage * safeSize
        val end = min(start + safeSize, orgUnits.size)

        val content =
            if (start >= orgUnits.size) {
                emptyList()
            } else {
                orgUnits.subList(start, end)
            }

        val pageResult = PageImpl(content, PageRequest.of(safePage, safeSize), orgUnits.size.toLong())

        return pageResponse(pageResult)
    }

    fun toResponseEntity(orgUnit: OrgUnit?): ResponseEntity<OrgUnit> =
        orgUnit?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}
