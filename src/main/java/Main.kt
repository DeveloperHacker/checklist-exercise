import java.io.File

class ProcessException(message: String) : Exception(message)

fun process(files: List<File>) {
    val parser = Parser()
    val trees = files.map { parser.parse(it.name, it.readText()) }
    val executor = Executor()
    val result = executor.exec(trees)
    for (item in result) println(item)
}

fun main(args: Array<String>) {
    val help = "Usage checklist [file [, file]"
    if (args.isEmpty()) {
        System.err.println("Wrong number of arguments\n$help")
        System.exit(1)
    }
    if (args.size == 1 && (args[0] == "-h" || args[0] == "--help")) {
        println(help)
        System.exit(0)
    }
    val files = args.map(::File)
    files.filterNot(File::isFile).map(File::getAbsolutePath).forEach {
        System.err.println("Storage file '$it' not exist")
        System.exit(1)
    }
    try {
        process(files)
    } catch (ex: ProcessException) {
        System.err.println(ex.message)
        System.exit(1)
    }
}