package id.trakkie.payment.impl.inner.impl

import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.api.{PaymentService, models}
import id.trakkie.payment.api.request.request
import id.trakkie.payment.impl.dal.jdbc.PaymentStoreRepository
import id.trakkie.payment.impl.entity.commands.{CancelPayment, CreatePayment, PaymentCommand, QueryPayment}
import id.trakkie.payment.impl.entity.state.PaymentState
import id.trakkie.payment.impl.inner.InnerLogicService
import id.trakkie.payment.impl.utils.StripeUtilities
import org.slf4j.{Logger, LoggerFactory}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

class InnerLogicServiceImpl(
                             clusterSharding: ClusterSharding,
                             paymentRepository: PaymentStoreRepository,
                             persistentEntityRegistry: PersistentEntityRegistry
                           )(implicit ec: ExecutionContext) extends InnerLogicService {

  val logger: Logger = LoggerFactory.getLogger(PaymentService.getClass)

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[PaymentCommand] =
    clusterSharding.entityRefFor(PaymentState.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def CreatePaymentLogic(req: request.CreatePaymentRequest): Future[models.PaymentResultDto] = {
    //Generate PaymentId
    val paymentId = UUID.randomUUID()
    val ref = entityRef(paymentId.toString)
    for {
      paymentSession <- StripeUtilities.generatePaymentLink(req.amount.get)
      paymentResult <- ref.ask[PaymentResultDto] (replyTo =>
        CreatePayment(
          req.amount.get,
          paymentId,
          req.orderId.get,
          paymentSession.getUrl,
          Some(
            Map(
              "SessionId" -> paymentSession.getId
            )
          ),
          replyTo
        )
      )
    } yield {
      paymentResult
    }
  }

  override def queryPaymentById(paymentId: String, readSideRead: Boolean = true): Future[PaymentResultDto] = {
    //Defaults load by read side DB
    if (readSideRead) {
      for {
        paymentResult <- paymentRepository.findByPaymentId(paymentId)
      } yield {
        paymentResult
          .map(r => PaymentResultDto(
            amount = r.amount, paymentId = r.paymentId, paymentStatus = r.paymentStatus, orderId = r.orderId, paymentLink = Some(r.paymentLink)
          ))
          .getOrElse(throw new Exception(s"No payment with payment Id ${paymentId} found!"))
      }
    }else {
      //Load from entity
      val ref = entityRef(paymentId)
      ref.ask[PaymentResultDto](replyTo => QueryPayment(paymentId, replyTo))
    }
  }

  override def CancelPaymentLogic(paymentId: String): Future[PaymentResultDto] = {
    //Load from entity
    val ref = entityRef(paymentId)
    ref.ask[PaymentResultDto](replyTo => CancelPayment(paymentId, replyTo))
  }
}
