package no.novari.fintkontrollorgunitservice.orgunit

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "org_unit")
data class OrgUnit(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val resourceId: String = "",
    val organisationUnitId: String = "",
    val name: String = "",
    val shortName: String? = null,
    val parentRef: String = "",
    val managerRef: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    val childrenRef: MutableList<String> = mutableListOf(),
)
