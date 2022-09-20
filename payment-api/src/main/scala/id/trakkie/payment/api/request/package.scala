package id.trakkie.payment.api.request

import id.trakkie.payment.api.serializable.JsonSerializable
import play.api.libs.json.{Format, Json}

package object request {

  case class CreatePaymentRequest(
    amount: Option[Long],
    orderId: Option[String],
    paymentId: Option[String]
  ) extends JsonSerializable

  object CreatePaymentRequest {
    implicit val format:Format[CreatePaymentRequest] = Json.format
  }

}
