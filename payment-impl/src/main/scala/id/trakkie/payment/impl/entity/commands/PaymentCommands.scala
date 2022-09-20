package id.trakkie.payment.impl.entity.commands

import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.ReplyEffect
import id.trakkie.payment.api.PaymentStateEnum
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.impl.entity.events.{PaymentCreatedEvent, PaymentEvent}
import id.trakkie.payment.impl.entity.state.PaymentState
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps

import java.util.UUID

object PaymentCommands {
  def onQueryPayment(state: PaymentState, cmd: QueryPayment): ReplyEffect[PaymentEvent, PaymentState] = {
    Effect.reply(cmd.replyTo) {
      PaymentResultDto(
        amount = state.paymentAmount,
        paymentId = state.paymentId,
        paymentStatus = state.paymentState,
        orderId = state.orderId,
        paymentLink = Some(state.paymentUrl)
      )
    }
  }

  def onCreatePayment(state: PaymentState, cmd: CreatePayment): ReplyEffect[PaymentEvent, PaymentState] = {
    Effect
      .persist(PaymentCreatedEvent(
        amount = cmd.amount,
        paymentId = cmd.paymentId,
        paymentStatus = PaymentStateEnum.CREATED,
        orderId = UUID.fromString(cmd.orderId),
        paymentUrl = Some(cmd.paymentUrl),
        addInfo = cmd.addInfo
      ))
      .thenReply(cmd.replyTo) {
        newEvent => PaymentResultDto(
          amount = newEvent.paymentAmount,
          paymentId = newEvent.paymentId,
          paymentStatus = newEvent.paymentState,
          orderId = newEvent.orderId,
          paymentLink = Some(newEvent.paymentUrl)
        )
      }
  }

  def onCancelPayment(state: PaymentState, cmd: CancelPayment): ReplyEffect[PaymentEvent, PaymentState] = {
    Effect
      .persist(PaymentCreatedEvent(
        amount = state.paymentAmount,
        paymentId = UUID.fromString(cmd.paymentId),
        paymentStatus = PaymentStateEnum.CANCELLED,
        orderId = UUID.fromString(state.orderId),
        paymentUrl = Some(state.paymentUrl),
        addInfo = Some(state.additionalInfo)
      ))
      .thenReply(cmd.replyTo) {
        newEvent => PaymentResultDto(
            amount = newEvent.paymentAmount,
            paymentId = newEvent.paymentId,
            paymentStatus = newEvent.paymentState,
            orderId = newEvent.orderId,
            paymentLink = Some(newEvent.paymentUrl)

          )
      }
  }
}
