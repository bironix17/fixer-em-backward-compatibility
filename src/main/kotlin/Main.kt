fun main(args: Array<String>) {
    try {
        if (args.isEmpty()) {
            System.err.println("Add path to the variable!")
        } else {

            val editor = JsonEditor().apply {
                removeField("cartridge", "name") // Удаление поля в картридже
                addField("cartridge", "name", "") // Добавление поля в картридж
            }
            println(editor.processContent(args[0]))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
