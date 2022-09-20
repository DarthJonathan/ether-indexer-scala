package id.trakkie.payment.api

import id.trakkie.commons.enums.DatabaseEnumeration
import id.trakkie.commons.utils.EnumUtils
import id.trakkie.payment.api.serializable.JsonSerializable
import play.api.libs.json.{Format, JsResult, JsValue, Json}

object PaymentStateEnum extends DatabaseEnumeration with JsonSerializable{
 val CREATED, WAITING_PAYMENT, PAID, FAILED, CHALLENGE, CANCELLED = Value

 type PaymentStateEnum = Value

 implicit val format: Format[PaymentStateEnum] = EnumUtils.enumFormat(PaymentStateEnum)
}

package object models {

 final case class PaymentResultDto(
   amount: Double,
   paymentId: String,
   paymentStatus: PaymentStateEnum.PaymentStateEnum,
   orderId: String,
   paymentLink: Option[String]
 ) extends JsonSerializable {}

 object PaymentResultDto {
  implicit val format: Format[PaymentResultDto] = Json.format
 }

}