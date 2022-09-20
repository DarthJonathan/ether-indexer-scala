package id.trakkie.payment.impl.entity

import akka.cluster.sharding.typed.scaladsl.{EntityRef, EntityTypeKey}
import id.trakkie.payment.api.PaymentStateEnum
import id.trakkie.payment.api.PaymentStateEnum.PaymentStateEnum
import id.trakkie.payment.api.serializable.JsonSerializable
import id.trakkie.payment.impl.entity.commands.{CreatePayment, PaymentCommand, PaymentCommands}
import play.api.libs.json.Json.JsValueWrapper

import java.util.UUID
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}


package object state {

  /**
   * States
   */
  object PaymentState {
    def initial: PaymentState = PaymentState(
      paymentAmount = 0,
      paymentId = UUID.randomUUID().toString,
      paymentState = PaymentStateEnum.CREATED,
      orderId = "",
      paymentUrl = "",
      additionalInfo = null
    )

    val typeKey = EntityTypeKey[PaymentCommand]("PaymentTransaction")

    implicit val format: Format[PaymentState] = Json.format
    implicit val mapReads: Reads[Map[String, String]] = new Reads[Map[String, String]] {
      def reads(jv: JsValue): JsResult[Map[String, String]] =
        JsSuccess(jv.as[Map[String, String]].map { case (k, v) =>
          k -> v
        })
    }

    implicit val mapWrites: Writes[Map[String, String]] = new Writes[Map[String, String]] {
      def writes(map: Map[String, String]): JsValue =
        Json.obj(map.map { case (s, o) =>
          val ret: (String, JsValueWrapper) = s -> JsString(o)
          ret
        }.toSeq: _*)
    }

    implicit val mapFormat: Format[Map[String, String]] = Format(mapReads, mapWrites)
  }

  case class PaymentState(
    paymentAmount: Long,
    paymentId: String,
    paymentState: PaymentStateEnum.PaymentStateEnum,
    orderId: String,
    paymentUrl: String,
    additionalInfo: Map[String, String]
  ) extends JsonSerializable

}
