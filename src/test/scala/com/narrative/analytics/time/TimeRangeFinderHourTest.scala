package com.narrative.analytics.time

class TimeRangeFinderHourTest extends munit.FunSuite {
  val instance = new TimeRangeFinderHour()

  test("range for know time is expected") {
    val known         = 1764854024000L // Thursday, December 4, 2025 1:13:44 PM GMT
    val expectedStart = 1764853200000L // Thursday, December 4, 2025 1:00:00 PM GMT
    val expectedEnd   = 1764856800000L // Thursday, December 4, 2025 2:00:00 PM GMT

    val res = instance.timeRangeFor(known)
    assert(res.startEpoch < known, "start of range is before timestamp")
    assert(res.endEpoch > known, "end of range is after timestamp")

    assert(res.startEpoch == expectedStart, (res.startEpoch, expectedStart))
    assert(res.endEpoch == expectedEnd, (res.endEpoch, expectedEnd))
  }
}

