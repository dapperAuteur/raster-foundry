package com.azavea.rf.tool.ast

import java.util.UUID


/** The ur-type for a recursive representation of MapAlgebra operations */
sealed trait MapAlgebraAST extends Product with Serializable {
  def id: UUID
  def args: List[MapAlgebraAST]
  def label: Option[String]
  def evaluable: Boolean
  def unbound: List[MapAlgebraAST]
  def find(id: UUID): Option[MapAlgebraAST]
}

object MapAlgebraAST {
  /** Map Algebra operations (nodes in this tree) */
  abstract class Operation(val symbol: String) extends MapAlgebraAST {
    def evaluable: Boolean = (args.length >= 1) && (args.foldLeft(true)(_ && _.evaluable))
    def unbound: List[MapAlgebraAST] =
      args.foldLeft(List[MapAlgebraAST]())({ case (list, mapAlgebra) =>
        list ++ mapAlgebra.unbound
      }).distinct

    def find(id: UUID): Option[MapAlgebraAST] =
      if (this.id == id)
        Some(this)
      else {
        val matches = this.args.map(_.find(id)).flatten
        require(matches.length < 2, s"Ambiguous IDs ($matches) on Map Algebra AST ($this)")
        matches.headOption
      }
  }

  case class Addition(args: List[MapAlgebraAST], id: UUID, label: Option[String])
      extends Operation("+")

  case class Subtraction(args: List[MapAlgebraAST], id: UUID, label: Option[String])
      extends Operation("-")

  case class Multiplication(args: List[MapAlgebraAST], id: UUID, label: Option[String])
      extends Operation("*")

  case class Division(args: List[MapAlgebraAST], id: UUID, label: Option[String])
      extends Operation("/")

  case class Masking(args: List[MapAlgebraAST], id: UUID, label: Option[String])
      extends Operation("mask")

  case class IntReclassification(args: List[MapAlgebraAST], id: UUID, label: Option[String], classBreaks: IntClassBreaks)
      extends Operation("intReclassify")

  case class DoubleReclassification(args: List[MapAlgebraAST], id: UUID, label: Option[String], classBreaks: DoubleClassBreaks)
      extends Operation("doubleReclassify")

  /** Map Algebra sources (leaves) */
  sealed abstract class Source[+T](val `type`: String) extends MapAlgebraAST {
    def value: Option[T]
    def args: List[MapAlgebraAST] = List.empty
    def evaluable = value.isDefined
    def unbound: List[MapAlgebraAST] = if (evaluable) List.empty else List(this)
    def find(id: UUID): Option[MapAlgebraAST] =
      if (this.id == id) Some(this)
      else None
  }

  case class RFMLRasterSource(id: UUID, label: Option[String], value: Option[RFMLRaster])
      extends Source[RFMLRaster]("raster")

  object RFMLRasterSource {
    def empty: RFMLRasterSource = RFMLRasterSource(UUID.randomUUID(), None, None)
  }

  /** TODO: Add other source types (or treat of them as hyperparameters - e.g. ClassBreaks, above) */

}

