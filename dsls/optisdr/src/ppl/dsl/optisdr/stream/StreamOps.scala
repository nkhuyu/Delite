package ppl.dsl.optisdr.stream

import java.io._

import scala.reflect.Manifest
import scala.reflect.SourceContext

import scala.reflect.SourceContext
import scala.virtualization.lms.common._
import scala.virtualization.lms.internal.{GenerationFailedException, GenericFatCodegen}

import ppl.dsl.optisdr._

trait SDRStreamOps extends Variables {
  this: OptiSDR =>
  
  // Convert from Rep[Stream[A]] to our Stream ops
  implicit def repToSDRStreamOps[A:Manifest](x: Rep[Stream[A]]) = new SDRStreamOpsCls(x)
  implicit def varToSDRStreamOps[A:Manifest](x: Var[Stream[A]]) = new SDRStreamOpsCls(readVar(x))
  
  // Objects methods
  class SDRStreamOpsCls[A:Manifest](val x: Rep[Stream[A]]) {
    type V[X] = Stream[X]
    type VA = V[A] // temporary for easy compatibility with old stuff
    type Self = Stream[A]
   
    def length()(implicit ctx: SourceContext) = stream_length(x)
   
    // data operations
    def apply(n: Rep[Int])(implicit ctx: SourceContext) = stream_apply(x, n)
    def update(n: Rep[Int], y: Rep[A])(implicit ctx: SourceContext) = stream_update(x,n,y)

    def +(y: Rep[Self])(implicit a: Arith[A], ctx: SourceContext) = stream_plus(x,y)
    def -(y: Rep[Self])(implicit a: Arith[A], ctx: SourceContext) = stream_minus(x,y)
    def *(y: Rep[Self])(implicit a: Arith[A], ctx: SourceContext) = stream_times(x,y)
    def /(y: Rep[Self])(implicit a: Arith[A], ctx: SourceContext) = stream_divide(x,y)
    
    def abs(implicit a: Arith[A], ctx: SourceContext) = stream_abs(x)
    def exp(implicit a: Arith[A], ctx: SourceContext) = stream_exp(x)
    
    def conj(implicit a: SDRArith[A], ctx: SourceContext) = stream_conj(x)
    
    def unary_~()(implicit ba: BitArith[A], ctx: SourceContext) = stream_binarynot(x)
    def &(y: Rep[Stream[A]])(implicit ba: BitArith[A], ctx: SourceContext) = stream_binaryand(x,y)
    def |(y: Rep[Stream[A]])(implicit ba: BitArith[A], ctx: SourceContext) = stream_binaryor(x,y)
    def ^(y: Rep[Stream[A]])(implicit ba: BitArith[A], ctx: SourceContext) = stream_binaryxor(x,y)
    
    def <<(y: Rep[Int])(implicit ba: BitArith[A], ctx: SourceContext) = stream_lshift(x, y)
    def >>(y: Rep[Int])(implicit ba: BitArith[A], ctx: SourceContext) = stream_rshift(x, y)
    def >>>(y: Rep[Int])(implicit ba: BitArith[A], ctx: SourceContext) = stream_rashift(x, y)
    
    // DON'T USE CLONE, WILL BREAK
  }
  
  // Arith
  def stream_plus[A:Manifest:Arith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_minus[A:Manifest:Arith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_times[A:Manifest:Arith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_divide[A:Manifest:Arith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  
  def stream_abs[A:Manifest:Arith](x: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_exp[A:Manifest:Arith](x: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  
  // SDR Arith
  def stream_conj[A:Manifest:SDRArith](x: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  
  // Binary ops
  def stream_binarynot[A:Manifest:BitArith](x: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_binaryand[A:Manifest:BitArith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_binaryor[A:Manifest:BitArith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_binaryxor[A:Manifest:BitArith](x: Rep[Stream[A]], y: Rep[Stream[A]])(implicit ctx: SourceContext) : Rep[Stream[A]]
  
  def stream_lshift[A:Manifest:BitArith](a: Rep[Stream[A]], b: Rep[Int])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_rshift[A:Manifest:BitArith](a: Rep[Stream[A]], b: Rep[Int])(implicit ctx: SourceContext) : Rep[Stream[A]]
  def stream_rashift[A:Manifest:BitArith](a: Rep[Stream[A]], b: Rep[Int])(implicit ctx: SourceContext) : Rep[Stream[A]]
  
  object FakeStreamVector {
    def apply[A:Manifest](xs: Rep[A]*) = fsv_obj_new(xs : _*)
    def ofLength[A:Manifest](length: Rep[Int]) = fsv_obj_oflength(length: Rep[Int])
  }
  
  def stream_length[A:Manifest](x: Rep[Stream[A]])(implicit ctx: SourceContext): Rep[Int]
  def stream_isrow[A:Manifest](x: Rep[Stream[A]])(implicit ctx: SourceContext): Rep[Boolean]
  def stream_apply[A:Manifest](x: Rep[Stream[A]], n: Rep[Int])(implicit ctx: SourceContext): Rep[A]  
  def stream_update[A:Manifest](x: Rep[Stream[A]], n: Rep[Int], y: Rep[A])(implicit ctx: SourceContext): Rep[Unit]  
  
  def fsv_obj_new[A:Manifest](xs: Rep[A]*): Rep[Stream[A]]
  def fsv_obj_oflength[A:Manifest](length: Rep[Int]): Rep[Stream[A]]
}

trait SDRStreamOpsExp extends SDRStreamOps with VariablesExp with BaseFatExp {
  this: OptiSDRExp =>
  
  case class FSVObjNew[A:Manifest](xs: Exp[A]*) extends Def[Stream[A]] {
    def a = manifest[A]
  }
  
  case class FSVObjOfLength[A:Manifest](length: Rep[Int]) extends Def[Stream[A]] {
    def a = manifest[A]
  }
  
  case class FSVLength[A:Manifest](x: Exp[Stream[A]]) extends DefWithManifest[A,Int]
  
  case class FSVApply[A:Manifest](x: Exp[Stream[A]], n: Exp[Int]) 
      extends DefWithManifest[A,A]
    
  case class FSVUpdate[A:Manifest](x: Exp[Stream[A]], n: Exp[Int], y: Exp[A]) 
      extends DefWithManifest[A,Unit]
  
  def stream_length[A:Manifest](x: Exp[Stream[A]])(implicit ctx: SourceContext) = reflectPure(FSVLength(x))
  
  def stream_apply[A:Manifest](x: Exp[Stream[A]], n: Exp[Int])(implicit ctx: SourceContext) = reflectPure(FSVApply(x, n))
  def stream_update[A:Manifest](x: Exp[Stream[A]], n: Exp[Int], y: Exp[A])(implicit ctx: SourceContext) = reflectWrite(x)(FSVUpdate(x, n, y))
  
  def fsv_obj_new[A:Manifest](xs: Exp[A]*) = reflectMutable(FSVObjNew(xs: _*))
  def fsv_obj_oflength[A:Manifest](length: Exp[Int]) = reflectMutable(FSVObjOfLength(length))
  
 abstract class SDRStreamArithmeticMap[A:Manifest:Arith](in: Exp[Stream[A]]) extends DeliteOpMap[A,A,Stream[A]] {
    def alloc = FakeStreamVector.ofLength[A](in.length)
    val size = copyTransformedOrElse(_.size)(in.length)
    
    def m = manifest[A]
    def a = implicitly[Arith[A]]
  }
  
  abstract class SDRStreamArithmeticZipWith[A:Manifest:Arith](inA: Exp[Stream[A]], inB: Exp[Stream[A]]) extends DeliteOpZipWith[A,A,A,Stream[A]] {
    def alloc = FakeStreamVector.ofLength[A](inA.length)
    val size = copyTransformedOrElse(_.size)(inA.length)
    
    def m = manifest[A]
    def a = implicitly[Arith[A]]
  }

  abstract class SDRStreamArithmeticIndexedLoop[A:Manifest:Arith](in: Exp[Stream[A]]) extends DeliteOpIndexedLoop {
    val size = copyTransformedOrElse(_.size)(in.length)

    def m = manifest[A]
    def a = implicitly[Arith[A]]
  }

  abstract class SDRStreamArithmeticReduce[A:Manifest:Arith](in: Exp[Stream[A]]) extends DeliteOpReduce[A] {
    val size = copyTransformedOrElse(_.size)(in.length)
    
    def m = manifest[A]
    def a = implicitly[Arith[A]]
  }
  
   abstract class SDRStreamSDRArithmeticMap[A:Manifest:SDRArith](in: Exp[Stream[A]]) extends DeliteOpMap[A,A,Stream[A]] {
    def alloc = FakeStreamVector.ofLength[A](in.length)
    val size = copyTransformedOrElse(_.size)(in.length)
    
    def m = manifest[A]
    def a = implicitly[SDRArith[A]]
  }

 abstract class SDRStreamBitArithmeticMap[A:Manifest:BitArith](in: Exp[Stream[A]]) extends DeliteOpMap[A,A,Stream[A]] {
    def alloc = FakeStreamVector.ofLength[A](in.length)
    val size = copyTransformedOrElse(_.size)(in.length)
    
    def m = manifest[A]
    def a = implicitly[BitArith[A]]
  }
  
  abstract class SDRStreamBitArithmeticZipWith[A:Manifest:BitArith](inA: Exp[Stream[A]], inB: Exp[Stream[A]]) extends DeliteOpZipWith[A,A,A,Stream[A]] {
    def alloc = FakeStreamVector.ofLength[A](inA.length)
    val size = copyTransformedOrElse(_.size)(inA.length)
    
    def m = manifest[A]
    def a = implicitly[BitArith[A]]
  }

  abstract class SDRStreamBitArithmeticIndexedLoop[A:Manifest:BitArith](in: Exp[Stream[A]]) extends DeliteOpIndexedLoop {
    val size = copyTransformedOrElse(_.size)(in.length)

    def m = manifest[A]
    def a = implicitly[BitArith[A]]
  }

  abstract class SDRStreamBitArithmeticReduce[A:Manifest:BitArith](in: Exp[Stream[A]]) extends DeliteOpReduce[A] {
    val size = copyTransformedOrElse(_.size)(in.length)
    
    def m = manifest[A]
    def a = implicitly[BitArith[A]]
  }
  
  case class SDRStreamPlus[A:Manifest:Arith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a + b
  }

  case class SDRStreamMinus[A:Manifest:Arith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a - b
  }
    
  case class SDRStreamTimes[A:Manifest:Arith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a * b
  }
  
  case class SDRStreamDivide[A:Manifest:Arith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a / b
  }
  
  case class SDRStreamAbs[A:Manifest:Arith](in: Exp[Stream[A]])
    extends SDRStreamArithmeticMap[A](in) {

    def func = e => e.abs
  }
  
  case class SDRStreamExp[A:Manifest:Arith](in: Exp[Stream[A]])
    extends SDRStreamArithmeticMap[A](in) {

    def func = e => e.exp
  }
  
  // SDR Math
  case class SDRStreamConj[A:Manifest:SDRArith](in: Exp[Stream[A]])
    extends SDRStreamSDRArithmeticMap[A](in) {

    def func = e => e.conj
  }
  
  // Bit math
  
  case class SDRStreamBitwiseNegate[A:Manifest:BitArith](in: Exp[Stream[A]])
    extends SDRStreamBitArithmeticMap[A](in) {

    def func = e => ~e
  }
  
  case class SDRStreamBitwiseAnd[A:Manifest:BitArith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamBitArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a & b
  }
  
  case class SDRStreamBitwiseOr[A:Manifest:BitArith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamBitArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a | b
  }
  
  case class SDRStreamBitwiseXor[A:Manifest:BitArith](inA: Exp[Stream[A]], inB: Exp[Stream[A]])
    extends SDRStreamBitArithmeticZipWith[A](inA, inB) {

    def func = (a,b) => a ^ b
  }
  
  case class SDRStreamLShift[A:Manifest:BitArith](in: Exp[Stream[A]], y: Exp[Int])
    extends SDRStreamBitArithmeticMap[A](in) {

    def func = e => e >> y
  }
  
  case class SDRStreamRShift[A:Manifest:BitArith](in: Exp[Stream[A]], y: Exp[Int])
    extends SDRStreamBitArithmeticMap[A](in) {

    def func = e => e << y
  }
  
  case class SDRStreamRAShift[A:Manifest:BitArith](in: Exp[Stream[A]], y: Exp[Int])
    extends SDRStreamBitArithmeticMap[A](in) {

    def func = e => e >>> y
  }
  
  // Arith
  def stream_plus[A:Manifest:Arith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamPlus(x,y))
  def stream_minus[A:Manifest:Arith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamMinus(x,y))
  def stream_times[A:Manifest:Arith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamTimes(x,y))
  def stream_divide[A:Manifest:Arith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamDivide(x,y))
  def stream_abs[A:Manifest:Arith](x: Exp[Stream[A]]) = reflectPure(SDRStreamAbs(x))
  def stream_exp[A:Manifest:Arith](x: Exp[Stream[A]]) = reflectPure(SDRStreamExp(x))
  
  // SDRArith
  def stream_conj[A:Manifest:SDRArith](x: Exp[Stream[A]]) = reflectPure(SDRStreamConj(x))
  
  // Bit arith
  def stream_bitwisenegate[A:Manifest:BitArith](x: Exp[Stream[A]]) = reflectPure(SDRStreamBitwiseNegate(x))
  def stream_bitwiseand[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamBitwiseAnd(x,y))
  def stream_bitwiseor[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamBitwiseOr(x,y))
  def stream_bitwisexor[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Stream[A]]) = reflectPure(SDRStreamBitwiseXor(x,y))
  
  def stream_lshift[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Int]) = reflectPure(SDRStreamLShift(x,y))
  def stream_rshift[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Int]) = reflectPure(SDRStreamRShift(x,y))
  def stream_rashift[A:Manifest:BitArith](x: Exp[Stream[A]], y: Exp[Int]) = reflectPure(SDRStreamRAShift(x,y))
}

trait SDRStreamOpsExpOpt extends SDRStreamOpsExp {
  this: OptiSDRExp =>
}

trait BaseGenStreamOps extends GenericFatCodegen {
  val IR: SDRStreamOpsExp
  import IR._
  
  override def unapplySimpleIndex(e: Def[Any]) = e match {
    // WHAT IS THIS?
    case _ => super.unapplySimpleIndex(e)
  }  
}

trait ScalaGenStreamOps extends BaseGenStreamOps with ScalaGenFat {
  val IR: SDRStreamOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any])(implicit stream: PrintWriter) = rhs match {
    case v@FSVObjNew(xs @ _*) => emitValDef(sym, remap("generated.scala.FakeStreamVector[" + remap(v.a) + "]")+"(" + xs.map(quote).reduceLeft(_+","+_) + ")")        
    case v@FSVObjOfLength(length) => emitValDef(sym, remap("generated.scala.FakeStreamVector.ofLength[" + remap(v.a) + "]")+"(" + quote(length) + ")")
    case FSVLength(x) => emitValDef(sym, quote(x) + "._length")
    case FSVApply(x,n) => emitValDef(sym, quote(x) + "._data(" + quote(n) + ")")
    case FSVUpdate(x,n,y) => emitValDef(sym, quote(x) + "._data(" + quote(n) + ") = " + quote(y))
    
    case _ => super.emitNode(sym, rhs)
  }
}