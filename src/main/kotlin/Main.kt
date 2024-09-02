fun main(args: Array<String>) {
    try {
        if (args.isEmpty()) {
            // add ./testData in program argument to use test data
            System.err.println("Add path to the variable!")
        } else {

            val editor = JsonEditor().apply {
                removeField("OneColumnPage", "staticPage") // Удаление поля в картридже
                addField("OneColumnPage", "myTestField", "Hello world!") // Добавление поля в картридж
            }
            println(editor.processContent(args[0]))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
