package com.narrative.analytics.models


import sttp.tapir.Codec
import sttp.tapir.Codec.PlainCodec


/** Possible events to register */
enum Event {
  case Click, Impression
}


object Event {

  /** Codec to convert to/from Event */
  given PlainCodec[Event] = Codec.derivedEnumeration[String, Event](
    (_: String) match {
      case "click"      => Some(Click)
      case "impression" => Some(Impression)
      case _            => None
    },
    _.toString().toLowerCase()
  )

}
