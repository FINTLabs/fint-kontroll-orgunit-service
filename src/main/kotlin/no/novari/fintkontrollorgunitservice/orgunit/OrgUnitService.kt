package no.novari.fintkontrollorgunitservice.orgunit

import no.novari.cache.FintCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrgUnitService::class.java)

@Service
class OrgUnitService(
    private val orgUnitCache: FintCache<String, OrgUnit>,
    private val orgUnitRepository: OrgUnitRepository,
) {
    fun getAllDescendantOrganisationUnitIds(rootOrganisationUnitId: String): List<String> {
        val allOrgUnits = orgUnitRepository.findAll()

        // Build lookup map in memory
        val byOrganisationUnitId = allOrgUnits.associateBy { it.organisationUnitId }

        val startOrgUnit =
            byOrganisationUnitId[rootOrganisationUnitId]
                ?: return emptyList()

        return dfsFromMap(startOrgUnit, byOrganisationUnitId)
    }

    private fun dfsFromMap(
        root: OrgUnit,
        byOrganisationUnitId: Map<String, OrgUnit>,
    ): List<String> {
        val result = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        val stack = ArrayDeque<String>()

        for (id in root.childrenRef.asReversed()) {
            stack.addLast(id)
        }

        while (stack.isNotEmpty()) {
            val id = stack.removeLast()
            if (!visited.add(id)) continue

            result.add(id)

            val child = byOrganisationUnitId[id] ?: continue
            for (cid in child.childrenRef.asReversed()) {
                stack.addLast(cid)
            }
        }

        return result
    }
}
