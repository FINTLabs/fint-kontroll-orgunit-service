package no.novari.fintkontrollorgunitservice.orgunitdistance

import no.novari.cache.FintCache
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrgUnitDistanceService::class.java)

@Service
class OrgUnitDistanceService(
    private val orgUnitCache: FintCache<String, OrgUnit>,
) {
    fun getAllOrgUnits(): List<OrgUnit> =
        orgUnitCache.getAllDistinct().also {
            logger.info("Number of distinct orgunits in cache: {}", it.size)
        }

    fun hasParentOrgUnit(orgUnit: OrgUnit): Boolean = orgUnit.organisationUnitId != orgUnit.parentRef

    fun getParentOrgUnit(orgUnit: OrgUnit): OrgUnit? = orgUnitCache.get(orgUnit.parentRef)

    fun createOrgUnitDistance(
        startOrgUnitId: String,
        currentOrgUnitId: String,
        distance: Int,
    ): OrgunitDistance =
        OrgunitDistance(
            id = "${currentOrgUnitId}_$startOrgUnitId",
            orgUnitId = currentOrgUnitId,
            subOrgUnitId = startOrgUnitId,
            distance = distance,
        )
}
