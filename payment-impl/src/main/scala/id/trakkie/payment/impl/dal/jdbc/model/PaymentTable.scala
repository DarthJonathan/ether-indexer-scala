package id.trakkie.payment.impl.dal.jdbc.model

import id.trakkie.payment.api.PaymentStateEnum.PaymentStateEnum
import id.trakkie.payment.impl.dal.jdbc.model.PaymentTable.{schemaName, tableName}
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

object PaymentTable {
  val schemaName = "payment"
  val tableName = "payment_store"
}

class PaymentTable(tag: Tag) extends Table[PaymentModelStore] (tag, Some(schemaName), tableName) {
  def amount = column[Long]("amount")
  def paymentId = column[String]("payment_id", O.PrimaryKey)
  def paymentStatus = column[PaymentStateEnum]("payment_status")
  def orderId = column[String]("order_id")
  def paymentLink = column[String]("payment_link")

  override def * = (amount, paymentId, paymentStatus, orderId, paymentLink) <> (PaymentModelStore.tupled, PaymentModelStore.unapply)
}

case class PaymentModelStore(
                              amount: Long,
                              paymentId: String,
                              paymentStatus: PaymentStateEnum,
                              orderId: String,
                              paymentLink: String
)

