package ppl.delite.framework.codegen.delite.generators

import scala.virtualization.lms.common.VariablesExp
import java.io.PrintWriter
import scala.virtualization.lms.internal.ScalaGenEffect

trait DeliteGenScalaVariables extends ScalaGenEffect {
  val IR: VariablesExp
  import IR._

  override def emitNode(sym: Sym[_], rhs: Def[_])(implicit stream: PrintWriter) = rhs match {
    case ReadVar(Variable(a)) => emitValDef(sym, quote(a) + ".get")
    case NewVar(init) => emitValDef(sym, "generated.Ref(" + quote(init) + ")")
    case Assign(Variable(a), b) => stream.println(quote(a) + ".set(" + quote(b) + ")")
    case _ => super.emitNode(sym, rhs)
  }
}