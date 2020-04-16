package nav.enro.example.data

class SimpleDataRepository() {
    fun getList(userId: String) = simpleData.filter {
        it.isPublic || (!it.isPublic && it.ownerId == userId)
    }

    fun getDetails(userId: String, id: String) =
        simpleData
            .filter {
                it.id == id
            }
            .first {
                it.isPublic || it.ownerId == userId
            }
}