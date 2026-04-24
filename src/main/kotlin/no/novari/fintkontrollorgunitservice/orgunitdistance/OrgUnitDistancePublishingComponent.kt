package no.novari.fintkontrollorgunitservice.orgunitdistance

import no.novari.fintkontrollorgunitservice.orgunit.OrgUnit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrgUnitDistancePublishingComponent(
    private val orgUnitDistanceService: OrgUnitDistanceService,
    private val orgUnitDistanceProducerService: OrgUnitDistanceProducerService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        initialDelayString = "\${fint.kontroll.orgunitdistance.publishing.initial-delay}",
        fixedDelayString = "\${fint.kontroll.orgunitdistance.publishing.fixed-delay}",
    )
    fun publishOrgUnitDistance() {
        logger.info("Start publishing orgUnit distance")

        val allOrgUnitDistances =
            buildList {
                orgUnitDistanceService.getAllOrgUnits().forEach { orgUnit ->
                    run {
                        logger.debug("Creating distances for orgUnit: ${orgUnit.organisationUnitId} :: ${orgUnit.name}")
                        addAll(createDistancesForOrgUnit(orgUnit))
                    }
                }
            }

        val orgUnitDistanceToPublish =
            allOrgUnitDistances.filter {
                orgUnitDistanceService.needsUpdate(it)
            }
        logger.info(
            "Publishing ${orgUnitDistanceToPublish.size} of ${allOrgUnitDistances.size} orgUnit distances because they are missing or changed in cache",
        )

        val publishedOrgUnitDistances = orgUnitDistanceProducerService.publish(orgUnitDistanceToPublish)

        // TODO: flytte dette til publiseringskomponenten. Ikke retur. logging i komponenten
        logger.debug("Published {} orgUnitDistances", publishedOrgUnitDistances.size)
        logger.info("Finished publishing orgUnit distance")
    }

    private fun createDistancesForOrgUnit(orgUnit: OrgUnit): List<OrgunitDistance> =
        buildList {
            val startOrgUnitId = orgUnit.organisationUnitId
            var currentOrgUnit: OrgUnit? = orgUnit
            var distance = 0

            add(
                orgUnitDistanceService.createOrgUnitDistance(
                    startOrgUnitId = startOrgUnitId,
                    currentOrgUnitId = startOrgUnitId,
                    distance = distance,
                ),
            )

            // TODO: sjekk for parentorgunitID= currentogunitId

            while (currentOrgUnit != null && orgUnitDistanceService.hasParentOrgUnit(currentOrgUnit)) {
                currentOrgUnit = orgUnitDistanceService.getParentOrgUnit(currentOrgUnit) ?: break
                distance++

                add(
                    orgUnitDistanceService.createOrgUnitDistance(
                        startOrgUnitId = startOrgUnitId,
                        currentOrgUnitId = currentOrgUnit.organisationUnitId,
                        distance = distance,
                    ),
                )

                logger.debug(
                    "From orgUnitId: {} - to orgUnitId: {} - distance: {}",
                    startOrgUnitId,
                    currentOrgUnit.organisationUnitId,
                    distance,
                )
            }

            logger.debug("{} has {} levels of orgunits above", orgUnit.name, distance)
        }
}
