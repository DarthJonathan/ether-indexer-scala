package id.trakkie.payment.impl.config

import com.typesafe.config.ConfigFactory

trait ConfigHolder {
  val config = ConfigFactory.load()
}
