import java.io.File

fun process(storageFile: File, checklistFile: File) {
    println("hello")
    println(storageFile.name)
    println(checklistFile.name)
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
    process(storageFile, checklistFile)
}