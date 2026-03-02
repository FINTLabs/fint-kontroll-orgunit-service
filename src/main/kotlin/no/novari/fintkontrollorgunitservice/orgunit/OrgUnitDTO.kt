package no.novari.fintkontrollorgunitservice.orgunit

data class OrgUnitDTO(
    val id: Long,
    val resourceId: String,
    val organisationUnitId: String,
    val name: String = "",
    val shortName: String? = null,
    val parentRef: String,
    val managerRef: String? = null,
    val childrenRef: MutableList<String> = mutableListOf(),
    val allSubOrgUnitsRef: MutableList<String> = mutableListOf(),
)
