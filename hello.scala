#!/usr/bin/env bash
set -euo pipefail

CP=$(cs fetch --classpath \
    "org.jline:jline:3.21.0" \
    "org.scala-lang:scala-library:2.13.8" \
    "org.scala-lang:scala3-library_3:3.1.2" \
    "org.junit.jupiter:junit-jupiter-api:5.8.2" )

JUNIT="org.junit.platform:junit-platform-console-standalone:1.8.2"

CMD=""; if [ $# -gt 0 ]; then CMD="$1"; shift; fi
WD="$(cd $(dirname "${BASH_SOURCE[0]}") > /dev/null && pwd)"
FN=${0#*/}; BN="${FN%%.*}" # filename and basename
mkdir -p "./out" "./lib" "./bin"; rm -rf "./out/*" "./lib/*" "./bin/$BN.jar";
if [ "$CMD" = "run" ]; then exec scala -classpath "$CP" "$0" "$@"; fi
if [ "$CMD" = "test" -o "$CMD" = "debug" -o "$CMD" == "assemble" ]; then scalac -explain -sourcepath "." -classpath "$CP" -d "./out" "$0"; fi
if [ "$CMD" = "test" ]; then exec java -jar $(cs fetch "$JUNIT") --disable-banner --classpath "$CP:out" --exclude-engine junit-vintage --select-package test; fi
if [ "$CMD" = "debug" ]; then echo "stop at $BN\$package$.breakpoint()" > "./jdb.ini"; echo "run" >> "./jdb.ini"; exec jdb -sourcepath "." -classpath "$CP:out" "$BN" "$@"; fi
if [ "$CMD" = "assemble" ]; then FILES=$(echo "$CP" | tr ":" "\n"); cd "./lib"; for FILE in $FILES; do jar xf "$FILE"; done; cd ".."; rm -f "./lib/META-INF/MANIFEST.MF"; fi
if [ "$CMD" = "assemble" ]; then scalac -classpath "$CP" -d "./bin/$BN.jar" -Xmain-class "$BN" "$0"; jar uf "./bin/$BN.jar" "$0"; exec jar uf "./bin/$BN.jar" -C "./lib" "."; fi
if [ "$CMD" = "clean" ]; then rm -rf "./jdb.ini" "./bin" "./lib" "./out"; exit 0; fi
echo "usage: $0 <run|test|debug|assemble|clean> [options]"; exit 1
!#
import scala.annotation.nowarn

// do not move breakpoint into a package
def breakpoint(): Unit =
    println("breakpoint!")


object hello:
    def main(args: Array[String]) =
        println("Hello, world")
        val x = 1
        var y = 2
        breakpoint()
        for arg <- args do println(s"arg=$arg")
        args.foreach(println)
        // repl.start(Array())


package repl:
    import org.jline.reader.LineReader
    import org.jline.reader.LineReaderBuilder
    import org.jline.reader.EndOfFileException
    import org.jline.reader.UserInterruptException

    @nowarn def start(args: Array[String]): Unit =
        println(s"donjonz!")
        val reader = LineReaderBuilder.builder().build()
        val prompt = ">> "
        while (true)
            var line: String = null
            try line = reader.readLine(prompt)
            catch
                case e: UserInterruptException => return;
                case e: EndOfFileException     => return;
            if (line != null && line != "")
                println(s"line=${line}")


package test:
    import org.junit.jupiter.api.Assertions._
    import org.junit.jupiter.api.Test

    @nowarn class TestSuite:
        @Test def testTheAnswer: Unit =
            val expected = 42
            val obtained = 42
            assertEquals(expected, obtained)

        @Test def testStrings: Unit =
            val expected = "hello!"
            val obtained = "Hello!"
            assertEquals(expected, obtained)
