package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class Execution extends MultiIOModule {

  // Don't touch the test harness
  // val testHarness = IO(
  //   new Bundle {
  //     val registerSetup = Input(new RegisterSetupSignals)
  //     val registerPeek  = Output(UInt(32.W))

  //     val testUpdates   = Output(new RegisterUpdates)
  //   })


  val io = IO(
    new Bundle {
     val instruction = Input(new Instruction)
     val PCIn = Input(UInt(32.W))
     val op1 = Input(UInt(32.W))
     val op2 = Input(UInt(32.W))
     val PCOut = Output(UInt(32.W))
     val res = Output(UInt(32.W))


     // val inWriteAddress = Input(UInt(32.W))
     // val inWriteData = Input(UInt(32.W))
      /**
        * TODO: Your code here.
        */
    }
  )

  val ALU = Module(new ALU).io

  ALU.aluOp := io.instruction.asUInt()
  ALU.op1 := io.op1
  ALU.op2 := io.op2

  io.res := ALU.aluResult
  


  decoder.instruction := io.instruction

   // to change when implementing the jumb and branch and stuff
  io.PCOut := io.PCIn
}
