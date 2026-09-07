package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class EXBarrier extends MultiIOModule {

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
      val resultIn = Input(UInt(32.W))
      val val2In = Input(UInt(32.W))
      val instructionIn = Input(new Instruction)
      val controlSignalsIn = Input(new ControlSignals)
      val takeBranchIn = Input(Bool())

      val PCOut = Output(UInt(32.W))
      val aluResOut = Output(UInt(32.W))
      val dataAddressOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
      val takeBranchOut = Output(Bool())
    }
  )

  val regPC = RegInit(0.U(32.W))
  val result = RegInit(0.U(32.W))
  val op2 = RegInit(0.U(32.W))
  val instruction = RegInit(0.U.asTypeOf(new Instruction))
  val regControlSignals = RegInit(0.U.asTypeOf(new ControlSignals))
  val takeBranch = RegInit(false.B)
  
  regPC := io.PCIn
  result := io.resultIn
  op2 := io.val2In
  instruction := io.instructionIn
  regControlSignals := io.controlSignalsIn
  takeBranch := io.takeBranchIn
  
  io.PCOut := regPC
  io.aluResOut := result
  io.dataAddressOut := op2
  io.instructionOut := instruction
  io.controlSignalsOut := regControlSignals
  io.takeBranchOut := takeBranch
  
  

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

}
