package id.trakkie.payment.impl

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraPersistenceComponents, ReadSideCassandraPersistenceComponents, WriteSideCassandraPersistenceComponents}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomServer, LagomServerComponents}
import com.softwaremill.macwire.wire
import id.trakkie.payment.api.PaymentService
import id.trakkie.payment.impl.dal.jdbc.PaymentStoreRepository
import id.trakkie.payment.impl.entity.state.PaymentState
import id.trakkie.payment.impl.entity.{PaymentManagerEntity, PaymentSerializerRegistry}
import id.trakkie.payment.impl.processors.PaymentStoreReadSideProcessor
import play.api.{Configuration, Environment}
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.slick.{ReadSideSlickPersistenceComponents, SlickPersistenceComponents, SlickReadSide}
import id.trakkie.payment.impl.inner.impl.InnerLogicServiceImpl
import play.api.db.{ConnectionPool, HikariCPComponents}
import play.api.inject.ApplicationLifecycle
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext


trait SlickComponents { app: LagomApplication =>

  def persistentEntityRegistry: PersistentEntityRegistry


  lazy val slickComponent: ReadSideSlickPersistenceComponents = new ReadSideSlickPersistenceComponents with HikariCPComponents {
    override def jsonSerializerRegistry: JsonSerializerRegistry = app.optionalJsonSerializerRegistry.get
    override def actorSystem: ActorSystem = app.actorSystem
    override def coordinatedShutdown: CoordinatedShutdown = app.coordinatedShutdown
    override def executionContext: ExecutionContext = app.executionContext
    override def environment: Environment = app.environment
    override def configuration: Configuration = app.configuration
    override def applicationLifecycle: ApplicationLifecycle = app.applicationLifecycle
    override def materializer: Materializer = app.materializer
    override def persistentEntityRegistry: PersistentEntityRegistry = app.persistentEntityRegistry
  }

  lazy val slickReadSide: SlickReadSide = slickComponent.slickReadSide
  lazy val db: JdbcBackend.Database = slickComponent.db
}

abstract class PaymentApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with SlickComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = PaymentSerializerRegistry

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[PaymentService](wire[PaymentServiceImpl])

  //Inner Service
  lazy val innerLogicService = wire[InnerLogicServiceImpl]

  //Databases
  lazy val paymentStoreTable = wire[PaymentStoreRepository]

  readSide.register(wire[PaymentStoreReadSideProcessor])

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(PaymentState.typeKey)(
      entityContext => PaymentManagerEntity.create(entityContext)
    )
  )
}