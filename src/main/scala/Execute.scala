package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule
import Op1Select._
import Op2Select._


class Execute extends MultiIOModule {

  // Don't touch the test harness
  // val testHarness = IO(
  //   new Bundle {
  //     val registerSetup = Input(new RegisterSetupSignals)
  //     val registerPeek  = Output(UInt(32.W))

  //     val testUpdates   = Output(new RegisterUpdates)
  //   })


  val io = IO(
    new Bundle {

     val PCIn             = Input(UInt(32.W))
     val aluOpIn          = Input(UInt(4.W))
     val controlSignalsIn = Input(new ControlSignals)
     val branchTypeIn     = Input(UInt(3.W))
     val op1SelectIn      = Input(UInt(1.W))
     val op2SelectIn      = Input(UInt(1.W))
     val immTypeIn        = Input(UInt(3.W))
     val val1In           = Input(UInt(32.W))
     val val2In           = Input(UInt(32.W))
     val instructionIn    = Input(new Instruction)
     val immediateIn      = Input(UInt(32.W))


     val PCOut             = Output(UInt(32.W))
     val aluOpOut          = Output(UInt(4.W))
     val controlSignalsOut = Output(new ControlSignals)
     val branchTypeOut     = Output(UInt(3.W))
     val op1SelectOut      = Output(UInt(1.W))
     val op2SelectOut      = Output(UInt(1.W))
     val immTypeOut        = Output(UInt(3.W))
     // val val1Out           = Output(UInt(32.W))
     val val2Out           = Output(UInt(32.W))
     val instructionOut    = Output(new Instruction)
     val immediateOut      = Output(UInt(32.W))
     val ALUres            = Output(UInt(32.W))


     // val inWriteAddress = Input(UInt(32.W))
     // val inWriteData = Input(UInt(32.W))
      /**
        * TODO: Your code here.
        */
    }
  )


  // ALU
  val ALU = Module(new ALU).io

  ALU.op1 := io.val1In
  when (io.op2SelectIn === imm) {
    ALU.op2 := io.immediateIn
  } .otherwise {
    ALU.op2 := io.val2In
  }

  ALU.aluOp := io.aluOpIn

  io.ALUres := ALU.aluResult
  
  
  // io.op2Out := io.op2SeledtIn

  val instructionWire = Wire(new Instruction)
  instructionWire :=  io.instructionIn
  io.instructionOut := instructionWire

   // to change when implementing the jumb and branch and stuff
  io.PCOut             := io.PCIn + io.immediateIn
  io.aluOpOut          := io.aluOpIn
  io.controlSignalsOut := io.controlSignalsIn
  io.branchTypeOut     := io.branchTypeIn
  io.op1SelectOut      := io.op1SelectIn  
  io.op2SelectOut      := io.op2SelectIn
  io.immTypeOut        := io.immTypeIn
  io.val2Out           := io.val2In
  io.immediateOut      := io.immediateIn
}
