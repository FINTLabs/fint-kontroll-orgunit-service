package no.novari.fintkontrollorgunitservice.orgunit

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "org_unit")
data class OrgUnit(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val resourceId: String,
    val organisationUnitId: String,
    val name: String,
    val shortName: String?,
    val parentRef: String,
    val childrenRef: List<String>?,
    val allSubOrgUnitsRef: List<String>?,
    val managerRef: String?,
)
