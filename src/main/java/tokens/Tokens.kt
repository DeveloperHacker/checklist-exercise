package tokens


val PAR = ValueToken("PAR", "par")
val VAL = ValueToken("VAL", "val")
val FUN = ValueToken("FUN", "fun")
val AND_AND = ValueToken("AND_AND", "&&")
val OR_OR = ValueToken("OR_OR", "||")
val PLUS = ValueToken("PLUS", "+")
val MINUS = ValueToken("MINUS", "-")
val MUL = ValueToken("MUL", "*")
val DIV = ValueToken("DIV", "/")
val EQ_EQ = ValueToken("EQ_EQ", "==")
val EXCL_EQ = ValueToken("EXCL_EQ", "!=")
val LT = ValueToken("LT", "<")
val GT = ValueToken("GT", ">")
val LT_EQ = ValueToken("LT_EQ", "<=")
val GT_EQ = ValueToken("GT_EQ", ">=")
val LPAR = ValueToken("LPAR", "(")
val RPAR = ValueToken("RPAR", ")")
val LBRACE = ValueToken("LBRACE", "{")
val RBRACE = ValueToken("RBRACE", "}")
val EQ = ValueToken("EQ", "=")
val COMMA = ValueToken("COMMA", ",")
val IF = ValueToken("IF", "if")
val ELSE = ValueToken("ELSE", "else")
val IDENTIFIER = Token("IDENTIFIER")
val STRING = Token("STRING")
val BOOLEAN = Token("BOOLEAN")
val INTEGER = Token("INTEGER")
val EOF = Token("EOF")

val simpleComment = Pair("//", "\n")
val complexComment = Pair("/*", "*/")

val whiteSpaces = setOf(' ', '\n', '\t', '\r')

val symbols = setOf(',', '(', ')', '{', '}', '=', '<', '>', '!', '+', '-', '*', '/', '&', '|')

val valueTokens = setOf(
        PAR, VAL, FUN,
        AND_AND, OR_OR, PLUS, MINUS, MUL, DIV,
        EQ_EQ, EXCL_EQ, LT, GT, LT_EQ, GT_EQ,
        LPAR, RPAR, LBRACE, RBRACE,
        EQ, IF, ELSE, COMMA
)

val binaryOperationTokens = setOf(
        AND_AND, OR_OR, PLUS, MINUS, MUL, DIV,
        EQ_EQ, EXCL_EQ, LT, GT, LT_EQ, GT_EQ
)

val binaryOperationPrecedence = mapOf(
        MUL to 0, DIV to 0,
        PLUS to 1, MINUS to 1,
        EQ_EQ to 2, EXCL_EQ to 2, LT to 2, GT to 2, LT_EQ to 2, GT_EQ to 2,
        AND_AND to 3,
        OR_OR to 4
)

