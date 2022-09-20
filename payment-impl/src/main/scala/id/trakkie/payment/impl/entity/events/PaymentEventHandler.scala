package id.trakkie.payment.impl.entity.events

import id.trakkie.payment.api.PaymentStateEnum.PaymentStateEnum
import id.trakkie.payment.impl.entity.state.PaymentState

import java.util.UUID

object PaymentEventHandler {

  def handleCreatePaymentEvent(amount: Long, paymentId: UUID, paymentStatus: PaymentStateEnum, orderId: UUID, paymentUrl: Option[String], addInfo: Option[Map[String, String]]): PaymentState = {
    PaymentState(
      paymentAmount = amount,
      paymentId = paymentId.toString,
      paymentState = paymentStatus,
      orderId = orderId.toString,
      paymentUrl = paymentUrl.orNull,
      additionalInfo = addInfo.orNull
    )
  }

}
