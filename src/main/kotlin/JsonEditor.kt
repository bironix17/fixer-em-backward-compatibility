import org.apache.commons.io.FilenameUtils.isExtension
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.System.lineSeparator
import java.util.Scanner

private val CARTRIDGE_NAME_KEY = "@type"

private data class AddValue(val cartridgeName: String, val fieldName: String, val value: String)
private data class RemoveValue(val cartridgeName: String, val fieldName: String)

class JsonEditor {
    private val removeFields: MutableList<RemoveValue> = arrayListOf()
    private val addFields: MutableList<AddValue> = arrayListOf()
    private var singleFileUpdates = 0
    private var totalUpdates = 0

    fun processContent(path: String): String {
        readDirectory(path)
        return "Total number of found and edited templates is $totalUpdates"
    }

    fun removeField(cartridgeName: String, fieldName: String) =
        removeFields.add(RemoveValue(cartridgeName, fieldName))

    fun addField(cartridgeName: String, fieldName: String, value: String) =
        addFields.add(AddValue(cartridgeName, fieldName, value))

    // рекурсивный обход папок
    private fun readDirectory(rootPath: String) {
        val rootDir = File(rootPath)
        if (rootDir.exists()) {
            if (rootDir.isDirectory) {
                for (element in rootDir.list()!!) {
                    val child = File(rootDir.path + File.separator + element)
                    if (child.exists()) {
                        if (child.isDirectory) readDirectory(child.path)
                        else if (child.isFile) readFile(child.path)
                    } else System.err.println("There is no file here: " + child.path)
                }
            } else if (rootDir.isFile) {
                readFile(rootDir.path)
            }
        } else {
            System.err.println("There is no file here: $rootPath")
        }
    }

    // чтение файлов с json'ами
    private fun readFile(path: String) {
        try {
            if (!isExtension(path, "json")) return

            val sb = StringBuilder()
            val scanner = Scanner(FileReader(path))
            scanner.use { scanner ->
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine())
                }
            }
            val baseFile = JSONObject(sb.toString())
            findDesiredObject(baseFile)
            if (singleFileUpdates > 0) {
                val writer = FileWriter(path)
                writer.use { writer -> baseFile.write(writer, 4, 0) }
                totalUpdates += singleFileUpdates
                singleFileUpdates = 0
            }
        } catch (e: IOException) {
            System.err.println("Something goes wrong while reading and writing file " + path + lineSeparator() + "Exception: " + e + lineSeparator() + e.message)
            e.printStackTrace()
        }
    }

    // рекурсивный обход элементов json для массивов
    private fun findDesiredObject(arr: JSONArray) {
        for (obj in arr) {
            if (obj is JSONObject) findDesiredObject(obj)
            else if (obj is JSONArray) findDesiredObject(obj)
        }
    }

    // рекурсивный обход элементов json
    private fun findDesiredObject(obj: JSONObject) {

        if (obj.containsKey(CARTRIDGE_NAME_KEY))
            checkAndUpdateCartridge(cartridge = obj, cartridgeName = obj.getString(CARTRIDGE_NAME_KEY))

        val iterator = obj.keys()
        while (iterator.hasNext()) {
            when (val value = obj.get(iterator.next())) {
                is JSONObject -> findDesiredObject(value)
                is JSONArray -> findDesiredObject(value)
            }
        }
    }

    private fun checkAndUpdateCartridge(cartridgeName: String, cartridge: JSONObject) {

        addFields.filter { cartridgeName == it.cartridgeName && !cartridge.containsKey(it.fieldName) }
            .map {
                cartridge.put(it.fieldName, it.value)
                singleFileUpdates++
            }

        removeFields.filter { cartridgeName == it.cartridgeName && cartridge.containsKey(it.fieldName) }
            .map {
                cartridge.remove(it.fieldName)
                singleFileUpdates++
            }
    }

    private fun JSONObject.containsKey(key: String) = this.opt(key) != null
}
