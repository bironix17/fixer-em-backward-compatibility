import org.apache.commons.io.FilenameUtils.isExtension
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.System.lineSeparator
import java.util.Scanner

class JsonEditor {
    private val removeActions: MutableList<(JSONObject, String, MutableIterator<String>) -> Boolean> = arrayListOf()
    private val addActions: MutableList<(JSONObject) -> Boolean> = arrayListOf()
    private var updateCounter = 0

    fun processContent(path: String): String {
        readDirectory(path)
        return "Total number of found and edited templates is $updateCounter"
    }

    fun removeField(cartridgeName: String, fieldName: String) =
        removeActions.add(removeFieldAction(cartridgeName, fieldName))

    fun addField(cartridgeName: String, fieldName: String, value: Any) =
        addActions.add(addFieldAction(cartridgeName, fieldName, value))

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
            scanner.use { scanner -> while (scanner.hasNextLine()) { sb.append(scanner.nextLine()) } }
            val baseFile = JSONObject(sb.toString())
            if (findDesiredObject(baseFile)) {
                val writer = FileWriter(path)
                writer.use { writer -> baseFile.write(writer, 4, 0) }
            }
        } catch (e: IOException) {
            System.err.println("Something goes wrong while reading and writing file " + path + lineSeparator() + "Exception: " + e + lineSeparator() + e.message)
            e.printStackTrace()
        }
    }

    // рекурсивный обход элементов json для массивов
    private fun findDesiredObject(arr: JSONArray): Boolean {
        var changed = false
        for (obj in arr) {
            if (obj is JSONObject) changed = findDesiredObject(obj) || changed
            else if (obj is JSONArray) changed = findDesiredObject(obj) || changed
        }
        return changed
    }

    // рекурсивный обход элементов json
    private fun findDesiredObject(obj: JSONObject): Boolean {
        var changed = false

        val iterator = obj.keySet().iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val child = obj[key]
            if (child is JSONObject) changed = findDesiredObject(child) || changed
            if (child is JSONArray) changed = findDesiredObject(child) || changed
            else {
                // целевое изменение данных
                changed =  addActions.map { it(obj) }.firstOrNull { it } ?: changed
                changed = removeActions.map { it(obj, key, iterator) }.firstOrNull { it } ?: changed
            }
        }
        return changed
    }

    private fun removeFieldAction(cartridgeName: String, name: String) =
        { obj: JSONObject, currentField: String, fieldIterator: MutableIterator<String> ->
            if (isDesiredCartridge(obj, name = cartridgeName) && currentField == name) {
                fieldIterator.remove()
                updateCounter++
                true
            }
            else false
        }


    private fun addFieldAction(cartridgeName: String, name: String, value: Any) =
        { obj: JSONObject ->
            if (isDesiredCartridge(obj, name = cartridgeName)) {
                obj.put(name, value)
                updateCounter++
                true
            }
            else false
        }


    private fun isDesiredCartridge(obj: JSONObject, name: String): Boolean =
        obj.has("@type") && obj.get("@type").toString().equals(name, ignoreCase = true)
}
