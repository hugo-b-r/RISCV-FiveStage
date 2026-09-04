package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
    val instructionIn  = Input(new Instruction)
    val aluResIn         = Input(UInt(32.W))
    val dataAddressIn    = Input(UInt(32.W))
    val controlSignals = Input(new ControlSignals)

    val controlSignalsOut = Output(new ControlSignals)
    val instructionOut = Output(new Instruction)
    val aluResOut = Output(UInt(32.W))
    val readDataOut = Output(UInt(32.W))
  })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  DMEM.io.dataIn      := io.dataAddressIn
  DMEM.io.dataAddress := io.aluResIn 

  // do something based on the instruction
  DMEM.io.writeEnable := io.controlSignals.memWrite

  io.readDataOut := DMEM.io.dataOut

  val instructionWire = Wire(new Instruction)
  instructionWire := io.instructionIn
  io.instructionOut := instructionWire

  io.aluResOut := io.aluResIn

  io.controlSignalsOut := io.controlSignals
}
