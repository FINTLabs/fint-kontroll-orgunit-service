package no.novari.fintkontrollorgunitservice.orgunitdistance

data class OrgunitDistance(
    val id: String,
    val orgUnitId: String,
    val subOrgUnitId: String,
    val distance: Int,
)
