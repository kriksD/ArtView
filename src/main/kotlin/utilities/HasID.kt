package utilities

interface HasID {
    val id: Int
}

fun Collection<HasID>.uniqueId(): Int {
    var newID = 0
    while (newID in this.map { it.id }) { newID++ }
    return newID
}