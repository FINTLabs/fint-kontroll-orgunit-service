package no.novari.fintkontrollorgunitservice.orgunit

data class OrgUnitApiDTO(
    val id: Long,
    val name: String,
    val organisationUnitId: String,
    val parentRef: String,
    // val parentName: String,
    val childrenRef: MutableList<String> = mutableListOf(),
)
