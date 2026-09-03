package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util.MuxLookup


/**
  * This module is already done. Have one on me
  *  
  * When a write and read conflicts this might result in stale data.
  * This caveat must be handled using a bypass.
 */
class ALU() extends MultiIOModule {


  // Don't touch the test harness
  // val testHarness = IO(
  //   new Bundle {
  //     val setup        = Input(new RegisterSetupSignals)
  //     val testUpdates  = Output(new RegisterUpdates)
  //   }
  // )


  // You shouldn't really touch these either
  val io = IO(
    new Bundle {
      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val aluOp = Input(UInt(4.W))
      // val aluOp = Input(UInt(32.W))
      val aluResult = Output(UInt(32.W))
    })

    import ALUOps._
  val ALUopMap = Array(
  // val ALUopMap = Array(

      ADD    -> (io.op1 + io.op2),
      SUB    -> (io.op1 - io.op2)

    )
   io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)
}
