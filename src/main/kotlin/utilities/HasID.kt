package utilities

interface HasID {
    val id: Int
}

fun Collection<HasID>.uniqueId(): Int {
    var newID = 0
    while (newID in this.map { it.id }) { newID++ }
    return newID
}

fun Collection<HasID>.multipleUniqueIDs(count: Int): List<Int> {
    val newIDs: MutableList<Int> = mutableListOf()

    repeat(count) {
        var newID = 0
        while (newID in this.map { it.id } || newID in newIDs) { newID++ }
        newIDs.add(newID)
    }

    return newIDs
}