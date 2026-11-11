package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class IFBarrier extends MultiIOModule {

  // val testHarness = IO(
  //   new Bundle {
  //     val setupSignals = Input(new SetupSignals)
  //     val testReadouts = Output(new TestReadouts)
  //     val regUpdates   = Output(new RegisterUpdates)
  //     val memUpdates   = Output(new MemUpdates)
  //     val currentPC    = Output(UInt(32.W))
  //   }
  // )




  val io = IO(
    new Bundle {
      val PCIn = Input(UInt(32.W))
      // val freezeIn = Input(Bool())
      val instructionIn = Input(new Instruction)
      // whether we already have a nop before or not
      val instructionInNop = Input(Bool())
      val branchTakenIn = Input(Bool())

      val PCOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)

      val instructionOutNop = Output(Bool())
    }
  )

  val regPC = RegInit(0.U(32.W))

  // when (io.freezeIn) {
    // regPC := regPC
  // } .otherwise {
    regPC := io.PCIn
  // }

  io.PCOut := regPC

  io.instructionOutNop := io.instructionInNop
  val instructionWire = Wire(new Instruction)
  instructionWire :=  io.instructionIn
  when (io.branchTakenIn) {
    io.instructionOut := Instruction.NOP
    io.instructionOutNop := true.B
  }.otherwise {
    io.instructionOut := instructionWire
  }
  
  

  // /**
  //   * Setup. You should not change this code
  //   */
  // IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  // ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  // MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  // testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  // testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  // /**
  //   spying stuff
  //   */
  // testHarness.regUpdates := ID.testHarness.testUpdates
  // testHarness.memUpdates := MEM.testHarness.testUpdates
  // testHarness.currentPC  := IF.testHarness.PC


  /**
    TODO: Your code here
    */

  dontTouch(io.PCIn)
  dontTouch(io.PCIn)

}
