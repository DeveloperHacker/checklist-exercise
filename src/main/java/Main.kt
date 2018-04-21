import java.io.File

fun process(file: File) {
    println("hello, ${file.name}")
}

fun main(args: Array<String>) {
    val help = "Usage checklist [file-name]"
    if (args.size == 1 && (args[0] == "-h" || args[0] == "--help")) {
        println(help)
        System.exit(0)
    }
    if (args.size != 1)
        error("Wrong number of arguments\n$help")
    val fileName = args[0]
    val inputFile = File(fileName)
    if (!inputFile.isFile)
        error("File '${inputFile.absolutePath}' not exist")
    process(inputFile)
}