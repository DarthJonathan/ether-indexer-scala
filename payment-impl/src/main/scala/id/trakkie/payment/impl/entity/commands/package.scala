package id.trakkie.payment.impl.entity

import akka.Done
import akka.actor.typed.ActorRef
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.stripe.model.checkout.Session
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.api.serializable.JsonSerializable

import java.util.UUID

package object commands {

  /**
   * Commands
   */
  sealed trait PaymentCommand
    extends JsonSerializable with ReplyType[Done]

  case class QueryPayment(paymentId: String, replyTo: ActorRef[PaymentResultDto])
    extends PaymentCommand

  case class CreatePayment(amount: Long, paymentId: UUID, orderId: String, paymentUrl: String, addInfo: Option[Map[String, String]], replyTo: ActorRef[PaymentResultDto])
    extends PaymentCommand

  case class CancelPayment(paymentId: String, replyTo: ActorRef[PaymentResultDto])
    extends PaymentCommand


}
