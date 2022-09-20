package id.trakkie.payment.impl.entity

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import id.trakkie.payment.api.PaymentStateEnum
import id.trakkie.payment.api.serializable.JsonSerializable

import java.util.UUID

package object events {

  /**
   * Events
   */
  object PaymentEvent {
    val sharding = 5
    val Tag: AggregateEventShards[PaymentEvent] = AggregateEventTag.sharded[PaymentEvent](sharding)
  }

  sealed trait PaymentEvent extends AggregateEvent[PaymentEvent] {
    override def aggregateTag: AggregateEventShards[PaymentEvent] = PaymentEvent.Tag
  }

  class PaymentStatusType extends TypeReference[PaymentStateEnum.type]
  case class PaymentCreatedEvent(
    amount: Long,
    paymentId: UUID,
    @JsonScalaEnumeration(classOf[PaymentStatusType]) paymentStatus: PaymentStateEnum.PaymentStateEnum,
    orderId: UUID,
    paymentUrl: Option[String],
    addInfo: Option[Map[String, String]]
  ) extends PaymentEvent with JsonSerializable

}
