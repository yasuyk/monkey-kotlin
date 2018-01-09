package monkey

import monkey.repl.start

fun main(args: Array<String>) {
    val user = System.getProperty("user.name")
    println("Hello $user! This is the Monkey programming language!\n")
	println("Feel free to type in commands\n")
    start(System.`in`, System.out)
}
