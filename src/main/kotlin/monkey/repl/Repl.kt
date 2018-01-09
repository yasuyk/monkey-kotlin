package monkey.repl

import monkey.lexer.Lexer
import monkey.token.EOF
import java.io.InputStream
import java.io.OutputStream
import java.util.*

const val PROMPT = ">> "

fun start(input : InputStream, output : OutputStream) {
	val scanner = Scanner(input)

    while (true) {
        print(PROMPT)

		val line = scanner.nextLine()
		val l = Lexer.newInstance(line)

        do {
            val tok = l.nextToken()
            if (tok.type != EOF) {
                println("$tok")
            }
        } while (tok.type != EOF)
	}
}
