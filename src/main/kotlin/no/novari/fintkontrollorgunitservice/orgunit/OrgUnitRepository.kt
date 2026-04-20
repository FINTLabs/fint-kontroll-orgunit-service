package no.novari.fintkontrollorgunitservice.orgunit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrgUnitRepository : JpaRepository<OrgUnit, Long> {
    fun findByOrganisationUnitId(organisasjonsUnitId: String): OrgUnit?

    fun findAllByNameContainingIgnoreCase(
        search: String,
        pageRequest: Pageable,
    ): Page<OrgUnit>

    @Query(
        """
           select o from OrgUnit o
           where upper(o.name) like upper(concat('%', ?1, '%'))
           and o.organisationUnitId in ?2

           """,
    )
    fun findOrgUnitsByNameAndOrOrganisationUnitId(
        name: String,
        organisationUnitId: Set<String>,
        pageRequest: Pageable,
    ): Page<OrgUnit>

    @Query(
        value = """
        WITH RECURSIVE
            starting (id, organisation_unit_id, name, parent_ref) AS
            (
                SELECT id, organisation_unit_id, name, parent_ref
                FROM org_unit
                WHERE id = ?1
            ),
            descendants (id, organisation_unit_id, name, parent_ref) AS
            (
                SELECT id, organisation_unit_id, name, parent_ref
                FROM starting AS s
                UNION ALL
                SELECT t.id, t.organisation_unit_id, t.name, t.parent_ref
                FROM org_unit AS t
                JOIN descendants AS d ON t.parent_ref = d.organisation_unit_id
                WHERE NOT t.organisation_unit_id = t.parent_ref
            )
        SELECT * FROM org_unit
        WHERE id IN
        (
            SELECT id FROM descendants
            UNION
            SELECT id FROM starting
        )
    """,
        nativeQuery = true,
    )
    fun findChildrenOrgUnitsByOrganisationUnitId(id: Long): List<OrgUnit>

    @Query(
        value = """
            WITH RECURSIVE
            starting (id, organisation_unit_id, parent_ref) AS
            (
                SELECT id, organisation_unit_id, parent_ref
                FROM org_unit
                WHERE id = ?1
            ),
            ancestors (id,  organisation_unit_id,  parent_ref) AS
            (
              SELECT id,  organisation_unit_id, parent_ref
              FROM org_unit AS t
              WHERE t.organisation_unit_id IN (SELECT parent_ref FROM starting)
              UNION ALL
              SELECT t.id, t.organisation_unit_id, t.parent_ref
              FROM org_unit AS t JOIN ancestors AS a ON t.organisation_unit_id = a.parent_ref
              WHERE NOT a.organisation_unit_id = a.parent_ref
            )
        SELECT * FROM org_unit
        WHERE id IN
        (
            SELECT id FROM ancestors
            UNION
            SELECT id FROM starting
        );
        """,
        nativeQuery = true,
    )
    fun findParentOrgUnitsByOrganisationUnitId(id: Long): List<OrgUnit>
}
