package id.trakkie.payment.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.api.request.request.CreatePaymentRequest

object PaymentService {
  val PAYMENT_RECEIPT_TOPIC = "new_payment_v1"
}
trait PaymentService extends Service {

  def queryPaymentResult(paymentId: String): ServiceCall[NotUsed, PaymentResultDto]

  def createPayment(): ServiceCall[CreatePaymentRequest, PaymentResultDto]

  def cancelPayment(): ServiceCall[CreatePaymentRequest, PaymentResultDto]

  def paymentReceipt(): Topic[PaymentResultDto]

  override def descriptor: Descriptor = {
    import Service._

    named("payment")
      .withCalls(
        restCall(Method.GET, "/api/v1/payment/query?paymentId", queryPaymentResult _),
        restCall(Method.POST,"/api/v1/payment/create", createPayment _),
        restCall(Method.POST,"/api/v1/payment/cancel", cancelPayment _)
      )
      .withTopics(
        topic(PaymentService.PAYMENT_RECEIPT_TOPIC, paymentReceipt _)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[PaymentResultDto](_.paymentId)
          )
      )
      .withAutoAcl(true)
  }
}