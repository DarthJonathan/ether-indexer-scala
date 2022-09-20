package id.trakkie.payment.impl.entity

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, ReplyEffect}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger, AkkaTaggerAdapter, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import id.trakkie.payment.api.models.PaymentResultDto
import id.trakkie.payment.impl.entity.commands.PaymentCommands.{onCancelPayment, onCreatePayment, onQueryPayment}
import id.trakkie.payment.impl.entity.commands.{CancelPayment, CreatePayment, PaymentCommand, QueryPayment}
import id.trakkie.payment.impl.entity.events.{PaymentEventHandler, PaymentCreatedEvent, PaymentEvent}
import id.trakkie.payment.impl.entity.state.PaymentState

object PaymentManagerEntity {
  def create(entityContext: EntityContext[PaymentCommand]): Behavior[PaymentCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

    create(persistenceId)
      .withTagger(
        AkkaTaggerAdapter.fromLagom(entityContext, PaymentEvent.Tag)
      )
  }

  private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
    .withEnforcedReplies[PaymentCommand, PaymentEvent, PaymentState](
      persistenceId = persistenceId,
      emptyState = PaymentState.initial,
      commandHandler = (state, cmd) => applyCommand(state, cmd),
      eventHandler = (state, evt) => applyEvent(evt)
    )

  //Command Processor
  def applyCommand(state: PaymentState, cmd: PaymentCommand): ReplyEffect[PaymentEvent, PaymentState] = {
    cmd match {
      case x: QueryPayment => onQueryPayment(state, x)
      case x: CreatePayment => onCreatePayment(state, x)
      case x: CancelPayment => onCancelPayment(state, x)
    }
  }

  //Event Processor
  def applyEvent(event: PaymentEvent): PaymentState = {
    event match {
      case PaymentCreatedEvent(amount, paymentId, paymentStatus, orderId, paymentUrl, addInfo) =>
        PaymentEventHandler.handleCreatePaymentEvent(amount, paymentId, paymentStatus, orderId, paymentUrl, addInfo)
    }
  }
}

object PaymentSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
    JsonSerializer[PaymentResultDto],
  )
}

