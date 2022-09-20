package id.trakkie.payment.impl.dal.jdbc

import akka.Done
import id.trakkie.payment.impl.dal.jdbc.model.{PaymentModelStore, PaymentTable}

import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._


class PaymentStoreRepository(database: Database)(implicit ctx: ExecutionContext) {

  val paymentStoreTable = TableQuery[PaymentTable]

  def migrateTable: Future[Done] = {
    database.run(
      paymentStoreTable.schema.createIfNotExists
    ).map(_ => Done)
  }

  def upsertPaymentStore(paymentModelStore: PaymentModelStore): Future[Unit] = {
    database.run(
      paymentStoreTable.insertOrUpdate(paymentModelStore)
    ).flatMap({
      case 0 | 1 => Future.unit
      case _ => Future.failed(new Exception(s"Error in upserting payment store data"))
    })
  }

  def findByPaymentId(paymentId: String): Future[Option[PaymentModelStore]] = {
    database.run (
      paymentStoreTable
        .filter(_.paymentId === paymentId)
        .result
        .headOption
    )
  }

}
