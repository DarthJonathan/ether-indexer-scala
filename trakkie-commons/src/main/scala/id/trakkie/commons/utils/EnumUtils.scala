package id.trakkie.commons.utils

import play.api.libs.json._

object EnumUtils {
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = Reads {
    case JsString(value) => {
      try {
        JsSuccess(enum.withName(value).asInstanceOf[E#Value])
      } catch {
        case _: NoSuchElementException =>
          JsError(s"Expected enum ${enum.getClass}, does not contain '$value'")
      }
    }
    case _ => JsError("String value expected!")
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = Writes(value => JsString(value.toString))

  def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}
