package no.novari.fintkontrollorgunitfactoryv2.orgunit

data class OrgUnit(
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
