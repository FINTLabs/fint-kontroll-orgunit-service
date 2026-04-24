package no.novari.fintkontrollorgunitservice.orgunitdistance

import no.novari.cache.FintCache
import no.novari.cache.exceptions.NoSuchCacheEntryException
import no.novari.fintkontrollorgunitservice.orgunit.OrgUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrgUnitDistanceService::class.java)

@Service
class OrgUnitDistanceService(
    private val orgUnitCache: FintCache<String, OrgUnit>,
    private val orgUnitDistanceCache: FintCache<String, OrgunitDistance>,
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

    fun needsUpdate(orgunitDistance: OrgunitDistance): Boolean {
        val distanceInCache =

            try {
                orgUnitDistanceCache.get(orgunitDistance.id)
            } catch (e: NoSuchCacheEntryException) {
                logger.info(
                    "OrgUnitDistance with id {} is missing from cache and needs update",
                    orgunitDistance.id,
                )

                return true
            }

        return distanceInCache.distance != orgunitDistance.distance
    }
}
