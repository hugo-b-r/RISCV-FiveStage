package FiveStage

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._
import scala.io.AnsiColor._


class BranchPredictorSpec extends FlatSpec with Matchers {

  val m = 10
  import BranchPredictorTests._
  it should "just predict right taken not taken value" in {
    

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
  it should "predict correct target address" in {
    wrapTester(
        chisel3.iotesters.Driver(() => new BranchPredictor(m)) { c =>
          new test2(c)
        } should be(true)
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
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    // step(1)
    expect(c.io.predicted_taken, true.B)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, false.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    // step(1)
    expect(c.io.predicted_taken, false.B)
  }


  class test2(c: BranchPredictor) extends PeekPokeTester(c) {
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, false.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)
    expect(c.io.target_address, 4.U)


    step(1)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.effective_target_address_update, 8)
    expect(c.io.predicted_taken, false.B)
    expect(c.io.target_address, 4.U)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, true.B)
    poke(c.io.was_not_branch, false.B)
    expect(c.io.predicted_taken, false.B)
    expect(c.io.target_address, 4.U)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, true.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    // step(1)
    expect(c.io.predicted_taken, true.B)
    expect(c.io.target_address, 8.U)

    step(1)
    poke(c.io.address_prediction, 0)
    poke(c.io.address_update, 0)
    poke(c.io.update, false.B)
    poke(c.io.taken_update, false.B)
    poke(c.io.was_not_branch, false.B)
    // step(1)
    expect(c.io.predicted_taken, false.B)
    expect(c.io.target_address, 4.U)
  }
}

