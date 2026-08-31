package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
     val instruction = Input(new Instruction)
     val inWriteAddress = Input(UInt(32.W))
     val inWriteData = Input(UInt(32.W))
      /**
        * TODO: Your code here.
        */
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  registers.io.readAddress1 := decoder.op1Select
  registers.io.readAddress2 := decoder.op2Select
  registers.io.writeEnable  := decoder.controlSignals.regWrite
  registers.io.writeAddress := io.inWriteAddress
  registers.io.writeData    := io.inWriteData

  decoder.instruction := io.instruction
}
