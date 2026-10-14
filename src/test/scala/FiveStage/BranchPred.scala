package FiveStage

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._
import scala.io.AnsiColor._


class BranchPredictorSpec extends FlatSpec with Matchers {

  import BranchPredictorTests._
  it should "just work" in {
    
    val m = 10

    for (n <- 1 to 80) {
      print("#")
    }
    print("\n")
    print(s"${REVERSED}${BOLD}Running test 5 times to test if no side effects on memory\n${RESET}")
    for (n <- 1 to 80) {
      print("#")
    }
    print("\n")

    wrapTester(
      for (n <- 1 to 5) {
        chisel3.iotesters.Driver(() => new BranchPredictor(m)) { c =>
          new test1(c)
        } should be(true)
      }
    )
  }
}


object BranchPredictorTests {

  class test1(c: BranchPredictor) extends PeekPokeTester(c) {
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, false.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)

    step(1)
    poke(c.io.address_prediction, 1)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)

    step(1)
    poke(c.io.address_prediction, 1)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 1)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    // step(1)
    expect(c.io.predicted_taken, true.B)
  }
}

