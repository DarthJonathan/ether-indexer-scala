package id.trakkie.payment.impl.processors

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, PersistentEntityRegistry, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide
import id.trakkie.payment.impl.dal.jdbc.PaymentStoreRepository
import id.trakkie.payment.impl.dal.jdbc.model.PaymentModelStore
import id.trakkie.payment.impl.entity.events.{PaymentCreatedEvent, PaymentEvent}
import id.trakkie.payment.impl.processors.PaymentStoreReadSideProcessor.readSideTable

import scala.concurrent.{ExecutionContext, Future}

object PaymentStoreReadSideProcessor {
  val readSideTable = "payment-store-table-v1"
}

class PaymentStoreReadSideProcessor(
   readSide: CassandraReadSide,
   dbHandler: PaymentStoreRepository,
   persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[PaymentEvent] {

  private def tableMigration(): Future[Done] = {
    dbHandler.migrateTable
  }

  /**
    * Handle Events
    */
  private def handleCreated(entityId: String, createPaymentEvt: PaymentCreatedEvent): Future[Unit] = {
    dbHandler.upsertPaymentStore(
      paymentModelStore = PaymentModelStore(
        amount = createPaymentEvt.amount,
        paymentId = createPaymentEvt.paymentId.toString,
        paymentStatus = createPaymentEvt.paymentStatus,
        orderId = createPaymentEvt.orderId.toString,
        paymentLink = createPaymentEvt.paymentUrl.getOrElse("")
      )
    )
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[PaymentEvent] =
    readSide.builder[PaymentEvent](readSideTable)
      .setGlobalPrepare(() => tableMigration())
      .setEventHandler{evt: EventStreamElement[PaymentCreatedEvent] => handleCreated(evt.entityId, evt.event).map(_ => List()) }
      .build()

  override def aggregateTags: Set[AggregateEventTag[PaymentEvent]] = PaymentEvent.Tag.allTags
}
