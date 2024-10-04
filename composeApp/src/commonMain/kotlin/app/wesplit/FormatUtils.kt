package app.wesplit

fun String.filterDoubleInput(): String {
    var dotCounter = 0
    var floatCounter = 0
    return mapNotNull { char ->
        if ((char == '.' || char == ',') && dotCounter++ == 0) return@mapNotNull '.'
        if (char.isDigit()) {
            if (dotCounter > 0 && floatCounter++ < 2) return@mapNotNull char
            if (dotCounter == 0) return@mapNotNull char
        }
        return@mapNotNull null
    }.joinToString(separator = "")
}
