package monkey

import monkey.`object`.Environment
import monkey.`object`.isError
import monkey.evaluator.eval
import monkey.lexer.Lexer
import monkey.parser.Parser
import monkey.repl.Repl
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        repl()
    } else {
        interpret(args)
    }
}


private fun repl() {
    val user = System.getProperty("user.name")
    println("Hello $user! This is the Monkey programming language!\n")
    println("Feel free to type in commands\n")
    Repl().start(System.`in`, System.out)
}

fun interpret(args: Array<String>) {

    val input = StringBuffer().apply {
        args.map { f -> append(File(f).bufferedReader().readText()) }
    }.toString()
    val p = Parser.newInstance(Lexer.newInstance(input)).parseProgram()
    val env = Environment()
    val ret = eval(p, env)
    if (ret.isError()) {
        ret?.let {
            System.err.println(it.inspect())
        }
    }
}

