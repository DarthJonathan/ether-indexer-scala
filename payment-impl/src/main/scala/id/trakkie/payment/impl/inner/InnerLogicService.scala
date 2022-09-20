package id.trakkie.payment.impl.inner

import id.trakkie.payment.api.request.request
import id.trakkie.payment.api.models.PaymentResultDto

import scala.concurrent.Future

trait InnerLogicService {

  def queryPaymentById(paymentId: String, readSideRead: Boolean = true): Future[PaymentResultDto]

  def CreatePaymentLogic(req: request.CreatePaymentRequest): Future[PaymentResultDto]

  def CancelPaymentLogic(paymentId: String): Future[PaymentResultDto]

}
