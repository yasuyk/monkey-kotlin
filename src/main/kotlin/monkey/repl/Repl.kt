package monkey.repl

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

        while (true) {
            print(PROMPT)

            val line = scanner.nextLine()
            val l = Lexer.newInstance(line)
            val p = Parser.newInstance(l)
            val program = p.parseProgram()

            if (p.errors.isNotEmpty()) {
                printParserErrors(output, p.errors)
                continue
            }

            output.write(program.string().toByteArray())
            output.write("\n".toByteArray())
        }
    }


    private fun printParserErrors(output: OutputStream, errors: List<String>) {
        output.write(monkeyFace.toByteArray())
        output.write("Woops! We ran into some monkey business here!\n".toByteArray())
        output.write(" parser errors:\n".toByteArray())
        for (msg in errors) {
            output.write("\t$msg\n".toByteArray())
        }
    }
}
