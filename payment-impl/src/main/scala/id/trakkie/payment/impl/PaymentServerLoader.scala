package id.trakkie.payment.impl

import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import id.trakkie.payment.api.PaymentService
import id.trakkie.payment.impl.config.ConfigHolder
import kamon.Kamon

class PaymentServerLoader extends LagomApplicationLoader with ConfigHolder{

  //Init Kamon
  Kamon.init()

  override def load(context: LagomApplicationContext): LagomApplication =
    new PaymentApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PaymentApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PaymentService])
}


