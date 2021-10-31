package storage

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LocalStorage(private val name: String): IStorage {
    val version: Int

    init {
        val versionFileLocation = "$root$name/version"
        val versionFile = File(versionFileLocation)
        if (versionFile.isFile) {
            val versionLine = versionFile.bufferedReader().readLine()
            var version = versionLine?.toIntOrNull() ?: 1
            if (version < 1) {
                version = 1
            }
            this.version = version
        } else {
            version = 1
        }

        if (version != 1) {
            throw Exception("Invalid version")
        }
        val writer = versionFile.bufferedWriter()
        writer.write(version)
        writer.close()
    }

    override fun insertData(data: ByteArray, key: ByteArray) {
        val file = FileOutputStream(getFileLocation(key), false)
        file.write(data)
    }

    override fun retrieveData(key: ByteArray): ByteArray? {
        val file = File(getFileLocation(key))
        if (file.isFile) {
            val fileStream = FileInputStream(file)
            return fileStream.readAllBytes()
        }
        return null
    }

    override fun removeData(key: ByteArray) {
        val file = File(getFileLocation(key))
        file.delete()
    }

    private fun getFileLocation(key: ByteArray): String = "$root$name/${key[0]}/$key"

    companion object  {
        const val root: String = "caches/"
    }
}