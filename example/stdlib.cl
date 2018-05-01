#global function title name: $header 1, name

#global function header name: $header 2, name

#global function header type name: ${"#" * type + " " + name}

#function item line: ${
    if (element is List)
        "    " + line
    else
        "* " + line
}

#function enum line: ${
    if (element is List)
        "    " + line
    else
        "1. " + line
}

#global function itemize list: ${list <- item}

#global function $enumerate list: ${list <- item}

#global function itemHeader name list:
    $header name;
    $itemize list;

#global function enumHeader name list:
    $header name;
    $enumerate list;
