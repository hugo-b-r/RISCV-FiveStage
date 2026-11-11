package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.{MultiIOModule, _}


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
     val instructionIn         = Input(new Instruction)
     val PCIn                  = Input(UInt(32.W))
     val delayedControlSignals = Input(new ControlSignals)
     val delayedWriteData      = Input(UInt(32.W))
     val delayedInstruction    = Input(new Instruction)
     val controlSignalsExIn    = Input(new ControlSignals)
     val rdAddressExIn         = Input(UInt(5.W))
     
     val PCOut             = Output(UInt(32.W))
     val aluOpOut          = Output(UInt(4.W))
     val controlSignalsOut = Output(new ControlSignals)
     val branchTypeOut     = Output(UInt(3.W))
     val op1SelectOut      = Output(UInt(1.W))
     val op2SelectOut      = Output(UInt(1.W))
     val immTypeOut        = Output(UInt(3.W))
     val val1Out           = Output(UInt(32.W))
     val val2Out           = Output(UInt(32.W))
     val instructionOut    = Output(new Instruction)
     val immediateOut      = Output(UInt(32.W))
     val freezeOut         = Output(Bool())
     val instrInNop        = Input(Bool())
     val instrIsNop        = Output(Bool())


     // val address_prediction = Input(UInt(32.W))
     // val predicted_taken = Output(Bool())
     // val target_address  = Output(UInt(32.W))
     // val address_update = Input(UInt(32.W))
     // val update_branch_predictor = Input(Bool())
     // val taken_update = Input(Bool())
     
    
     // val inWriteAddress = Input(UInt(32.W))
     // val inWriteData = Input(UInt(32.W))
      /**
        * TODO: Your code here.
        */
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io
  val immediateDecoder = Module(new ImmediateDecoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  registers.io.readAddress1 := io.instructionIn.registerRs1
  registers.io.readAddress2 := io.instructionIn.registerRs2
  registers.io.writeEnable  := io.delayedControlSignals.regWrite
  registers.io.writeAddress := io.delayedInstruction.registerRd
  registers.io.writeData    := io.delayedWriteData

  io.controlSignalsOut := decoder.controlSignals
  io.branchTypeOut := decoder.branchType
  io.op1SelectOut := decoder.op1Select
  io.op2SelectOut := decoder.op2Select
  io.immTypeOut := decoder.immType
  io.aluOpOut := decoder.ALUop
  // registers.io.writeAddress := io.inWriteAddress
  // registers.io.writeData    := io.inWriteData
  //
  // Forwarding
  when (io.delayedInstruction.registerRd === io.instructionIn.registerRs1 && io.delayedControlSignals.regWrite && io.instructionIn.registerRs1 =/= 0.U) {
    // forwarding of wb data
    io.val1Out := io.delayedWriteData
  } .otherwise {
    io.val1Out := registers.io.readData1
  }
  when (io.delayedInstruction.registerRd === io.instructionIn.registerRs2 && io.delayedControlSignals.regWrite && io.instructionIn.registerRs2 =/= 0.U) {
    // forwarding of wb data
    io.val2Out := io.delayedWriteData
  } .otherwise {
    io.val2Out := registers.io.readData2
  }

  io.freezeOut := io.controlSignalsExIn.memRead && (io.rdAddressExIn === io.instructionIn.registerRs1 || io.rdAddressExIn === io.instructionIn.registerRs2)
  
  decoder.instruction := io.instructionIn
  io.instructionOut := io.instructionIn

  io.PCOut := io.PCIn


  immediateDecoder.instruction := io.instructionIn
  immediateDecoder.immType := decoder.immType
  io.immediateOut := immediateDecoder.immediate



  io.instrIsNop := io.instrInNop


  dontTouch(io.PCIn)
  dontTouch(io.PCOut)
}
