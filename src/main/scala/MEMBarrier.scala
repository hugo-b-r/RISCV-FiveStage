package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class MEMBarrier extends MultiIOModule {

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
      val ALUresIn         = Input(UInt(32.W))
      val readDataIn       = Input(UInt(32.W))
      val instructionIn    = Input(new Instruction)
      val controlSignalsIn = Input(new ControlSignals)
      val freezeIn         = Input(Bool())

      val ALUresOut         = Output(UInt(32.W))
      val readDataOut       = Output(UInt(32.W))
      val instructionOut    = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
    }
  )

  // val regData = RegInit(0.U(32.W))
  val instruction = RegInit(0.U.asTypeOf(new Instruction))
  val controlSignals = RegInit(0.U.asTypeOf(new ControlSignals))
  val aluRes = RegInit(0.U(32.W))

  import lookup._
  import Op1Select._
  import Op2Select._
  import branchType._
  import ImmFormat._

  // val N = 0.asUInt(1.W)
  // val Y = 1.asUInt(1.W)
  // val NOP = List(N, N, N, N, N, branchType.DC, rs1, rs2, ImmFormat.DC, ALUOps.DC)
  
  // regData := io.readDataIn
  when (io.freezeIn) {
    instruction := 0.U.asTypeOf(new Instruction)
  } .otherwise {
    instruction := io.instructionIn
  }
  controlSignals := io.controlSignalsIn
  aluRes := io.ALUresIn

  io.ALUresOut := aluRes
  io.instructionOut := instruction
  io.controlSignalsOut := controlSignals


  io.readDataOut := io.readDataIn


  
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
}
