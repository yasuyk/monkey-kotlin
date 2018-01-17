package monkey.repl

import monkey.evaluator.eval
import monkey.`object`.Environment
import monkey.lexer.Lexer
import monkey.parser.Parser
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*


const val PROMPT = ">> "

class Repl {

    private val monkeyFace: String by lazy {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("monkey_face.txt").file)
        val monkeyFaceInput = FileInputStream(file)
        monkeyFaceInput.bufferedReader().use { it.readText() }
    }

    fun start(input: InputStream, output: OutputStream) {
        val scanner = Scanner(input)
        val env = Environment()

        while (true) {
            output.write(PROMPT)

            val line = scanner.nextLine()
            val l = Lexer.newInstance(line)
            val p = Parser.newInstance(l)
            val program = p.parseProgram()

            if (p.errors.isNotEmpty()) {
                printParserErrors(output, p.errors)
                continue
            }

            val evaluated = eval(program, env)
            if (evaluated != null) {
                output.write(evaluated.inspect())
                output.write("\n")
            }

        }
    }


    private fun printParserErrors(output: OutputStream, errors: List<String>) {
        output.write(monkeyFace)
        output.write("Woops! We ran into some monkey business here!\n")
        output.write(" parser errors:\n")
        for (msg in errors) {
            output.write("\t$msg\n")
        }
    }

    private fun OutputStream.write(string: String) {
        this.write(string.toByteArray())
    }
}
