package com.narrative.analytics.models


class TimeRangeTest extends munit.FunSuite {

  test("range is created with correct values") {
    val s  = System.currentTimeMillis()
    val e  = s + 10
    val tr = TimeRange(s, e)
    assert(tr.startEpoch == s)
    assert(tr.endEpoch == e)
  }

  test("creation fails if end is after start") {
    val s  = System.currentTimeMillis()
    val e  = s + 10

    intercept[AssertionError] {
      TimeRange(e, s)
    }
  }

  test("safe creation works in either order") {
    val s  = System.currentTimeMillis()
    val e  = s + 10
    val t1 = TimeRange.safe(s, e)
    val t2 = TimeRange.safe(e, s)
    assert(t1 == t2)
  }
}
