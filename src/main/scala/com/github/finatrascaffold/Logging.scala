package com.github.finatrascaffold

import com.twitter.inject.{Logging => TwitterLogging}

trait Logging extends TwitterLogging {

  val defaultStackOffset = 4

  private def getCaller(callStack: Array[StackTraceElement],
                        depth: Int, offset: Int = defaultStackOffset): String = {
    if (offset + depth >= callStack.length) {
      return "<bottom of call stack>"
    }
    val caller = callStack(offset + depth)
    caller.getClassName + "." + caller.getMethodName + ":" + caller.getLineNumber
  }

  def getCallers(depth: Int, includeAnonFunc: Boolean = false,
                 sep: String = "\n", offset: Int = defaultStackOffset): String = {
    def callStack = Thread.currentThread.getStackTrace
    val sb = new StringBuffer
    var i = 0
    var cur = 0
    while (cur < depth && i < callStack.length) {
      val funcCall = getCaller(callStack, i, offset)
      if (includeAnonFunc || !funcCall.contains("$$")) {
        sb.append(funcCall).append(sep)
        cur += 1
      }
      i += 1
    }
    sb.toString
  }

  def getCaller: String = {
    getCaller(Thread.currentThread.getStackTrace, 0)
  }

  def traceEnterMethod(msg: => Any = "", depth: Int = 1): Unit = {
    debug(">> " + (if (msg == "") "" else msg + " ") +
      getCallers(depth, includeAnonFunc = false, " ", 11))
  }

  def debugEnterMethod(msg: => Any = "")
                      (implicit methodName: sourcecode.Name, line: sourcecode.Line): Unit = {
    debug(s">> ${methodName.value}:${line.value} $msg")
  }

  def getArgs(implicit args: sourcecode.Args): String = {
    args.value.map(_.map(a => a.source + "=" + a.value).mkString("(", ", ", ")")).mkString("")
  }
}
