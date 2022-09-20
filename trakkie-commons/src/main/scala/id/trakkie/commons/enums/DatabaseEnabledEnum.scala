package id.trakkie.commons.enums

import slick.jdbc.PostgresProfile.api._

abstract class DatabaseEnumeration extends Enumeration {
  implicit val enumerationMapper = MappedColumnType.base[Value, String](_.toString, this.withName)
}
