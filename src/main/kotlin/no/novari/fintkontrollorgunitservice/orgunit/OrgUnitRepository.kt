package no.novari.fintkontrollorgunitservice.orgunit

import org.springframework.data.jpa.repository.JpaRepository

interface OrgUnitRepository : JpaRepository<OrgUnit, Long> {
    fun findByOrganisationUnitId(organisasjonsUnitId: String): OrgUnit?
}
