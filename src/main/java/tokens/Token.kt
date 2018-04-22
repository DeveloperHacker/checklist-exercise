package tokens

open class Token(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Token
        if (name != other.name) return false
        return true
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = name
}

class ValueToken(name: String, val value: String) : Token(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as ValueToken
        if (value != other.value) return false
        return true
    }

    override fun hashCode() = 31 * super.hashCode() + value.hashCode()
}
