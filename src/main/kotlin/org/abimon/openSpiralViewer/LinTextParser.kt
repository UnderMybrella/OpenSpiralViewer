package org.abimon.openSpiralViewer

import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.BasicParseRunner
import org.parboiled.support.StringVar
import org.parboiled.support.Var

@BuildParseTree
open class LinTextParser(parboiledCreated: Boolean) : BaseParser<Any>() {
    companion object {
        operator fun invoke(): LinTextParser = Parboiled.createParser(LinTextParser::class.java, true)

        fun splitUp(text: String): List<Any> {
            val parser = LinTextParser()
            val runner = BasicParseRunner<Any>(parser.LinText())
            val result = runner.run(text)
            return result.valueStack.reversed()
        }
    }

    open fun LinText(): Rule {
        val textVar = StringVar("")
        val cltVar = Var<Int>(0)

        return Sequence(
                Action<Any> { textVar.set("") },

                ZeroOrMore(
                    FirstOf(
                            Sequence(
                                    '<',
                                    "CLT",
                                    FirstOf(
                                            Sequence(
                                                    '>',
                                                    Action<Any> {
                                                        push(textVar.getAndSet(""))
                                                        push(-1)
                                                    }
                                            ),
                                            Sequence(
                                                    " ",
                                                    OneOrMore(AnyOf(charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'))),
                                                    Action<Any> { cltVar.set(match().toIntOrNull() ?: -1) },
                                                    ">",
                                                    Action<Any> {
                                                        push(textVar.getAndSet(""))
                                                        push(cltVar.getAndSet(0))
                                                    }
                                            )
                                    )
                            ),
                            Sequence(
                                    ANY,
                                    Action<Any> { textVar.append(match()) }
                            )
                    )
                ),

                Action<Any> { push(textVar.get()) }
        )
    }
}