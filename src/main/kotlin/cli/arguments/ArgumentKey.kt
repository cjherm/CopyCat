package cli.arguments

enum class ArgumentKey(val key: String) {
    HELP("help"),
    GUI("gui"),
    LOGF("logf"),
    NO_LOGF("no-logf"),
    NO_LOGC("no-logc"),
    SRC("src"),
    DEST("dest"),
    COMP("comp"),
    TYPES_INCL("incl"),
    TYPES_EXCL("excl");

    companion object {
        fun fromString(key: String): ArgumentKey? = entries.find { it.key == key }
    }
}
