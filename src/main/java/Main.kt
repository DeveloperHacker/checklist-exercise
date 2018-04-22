import java.io.File

class ProcessException(message: String) : Exception(message)

fun process(storageFile: File, checklistFile: File) {
    val storageData = storageFile.readText()
    val checklistData = checklistFile.readText()
    val parser = Parser()
    val storage = parser.parse(storageFile.name, storageData)
    val checklist = parser.parse(checklistFile.name, checklistData)
    val executor = Executor()
    val (normal, underflow, overflow) = executor.exec(storage, checklist)
    for (item in underflow) println("- $item")
    for (item in overflow) println("+ $item")
    for (item in normal) println("= $item")
}

fun main(args: Array<String>) {
    val help = "Usage checklist [storage] [checklist]"
    if (args.size == 1 && (args[0] == "-h" || args[0] == "--help")) {
        println(help)
        System.exit(0)
    }
    if (args.size != 2) {
        System.err.println("Wrong number of arguments\n$help")
        System.exit(1)
    }
    val (storageName, checklistName) = args
    val storageFile = File(storageName)
    val checklistFile = File(checklistName)
    if (!storageFile.isFile) {
        System.err.println("Storage file '${storageFile.absolutePath}' not exist")
        System.exit(1)
    }
    if (!checklistFile.isFile) {
        System.err.println("CheckList file '${checklistFile.absolutePath}' not exist")
        System.exit(1)
    }
    try {
        process(storageFile, checklistFile)
    } catch (ex: ProcessException) {
        System.err.println(ex.message)
        System.exit(1)
    }
}