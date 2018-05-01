fun title(name) = header(1, name)
fun header(name) = header(2, name)
fun header(type, name) = "#" * type + " " + name
fun item(name) = "* " + name
fun enum(name) = "1. " + name
