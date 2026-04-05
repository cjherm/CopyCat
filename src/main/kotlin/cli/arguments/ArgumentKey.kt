package cli.arguments

enum class ArgumentKey(val key: String) {
    GUI("gui"),
    LOGF("logf"),
    LOGC("logc"),
    SRC("src"),
    DEST("dest"),
    COMP("comp"),
    TYPES("types");

    companion object {
        fun fromString(key: String): ArgumentKey? = entries.find { it.key == key }
    }
}
