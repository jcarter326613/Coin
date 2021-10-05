package storage

class NullStorage: IStorage {
    override fun insertData(data: ByteArray, key: ByteArray) {
    }

    override fun retrieveData(key: ByteArray) {
        throw Exception("Not implemented")
    }
}