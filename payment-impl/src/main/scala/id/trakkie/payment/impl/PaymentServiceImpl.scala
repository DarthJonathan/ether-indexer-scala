package id.trakkie.payment.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.api.{PaymentService, models}
import id.trakkie.payment.api.request.request
import id.trakkie.payment.impl.config.ConfigHolder
import id.trakkie.payment.impl.entity.events.{PaymentCreatedEvent, PaymentEvent}
import id.trakkie.payment.impl.inner.InnerLogicService

import scala.concurrent.{ExecutionContext, Future}

class PaymentServiceImpl(
                        innerLogic: InnerLogicService,
                        persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext) extends PaymentService
    with ConfigHolder {

  override def queryPaymentResult(paymentId: String): ServiceCall[NotUsed, models.PaymentResultDto] = ServiceCall {
    _ => innerLogic.queryPaymentById(paymentId)
  }

  override def createPayment(): ServiceCall[request.CreatePaymentRequest, models.PaymentResultDto] = ServiceCall {
    req => innerLogic.CreatePaymentLogic(req)
  }

  override def cancelPayment(): ServiceCall[request.CreatePaymentRequest, models.PaymentResultDto] = ServiceCall {
    req => innerLogic.CancelPaymentLogic(req.paymentId.get)
  }

  override def paymentReceipt(): Topic[PaymentResultDto] =
    TopicProducer.taggedStreamWithOffset(PaymentEvent.Tag.allTags.toList) { (tag, fromOffset) =>
      persistentEntityRegistry
        .eventStream(tag, fromOffset)
        .map(event => (convertEvent(event), event.offset))
    }

  private def convertEvent(event: EventStreamElement[PaymentEvent]): PaymentResultDto = {
    event.event match {
      case PaymentCreatedEvent(amount, paymentId, paymentStatus, orderId, paymentUrl, _) =>
        PaymentResultDto(
          amount = amount,
          paymentId = paymentId.toString,
          paymentStatus = paymentStatus,
          orderId = orderId.toString,
          paymentLink = paymentUrl
        )
    }
  }
}
