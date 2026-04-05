package cli.arguments

data class MultiValueArgument(val key: String, val values: List<String>) : Argument()
