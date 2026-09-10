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


     val instructionMemIn    = Input(new Instruction)
     val rdAddressMemIn      = Input(UInt(5.W))
     val rdAddressWbIn       = Input(UInt(5.W))
     val aluValWbIn          = Input(UInt(32.W))
     val memResWbIn          = Input(UInt(32.W))
     val aluResMemIn         = Input(UInt(32.W))
     val val2MemIn           = Input(UInt(32.W))


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
     val takeBranch        = Output(Bool())
     val freezeOut         = Output(Bool())


     // val inWriteAddress = Input(UInt(32.W))
     // val inWriteData = Input(UInt(32.W))
      /**
        * TODO: Your code here.
        */
    }
  )

  val forwarder = Module(new Forwarder).io
  
  // ALU
  val ALU = Module(new ALU).io

  forwarder.instructionMemIn  := io.instructionMemIn
  forwarder.rdAddressMemIn    := io.rdAddressMemIn
  forwarder.rdAddressWbIn     := io.rdAddressWbIn
  forwarder.rs1AddressExIn    := io.instructionIn.registerRs1
  forwarder.rs2AddressExIn    := io.instructionIn.registerRs2
  forwarder.aluValWbIn        := io.aluValWbIn
  forwarder.memResWbIn        := io.memResWbIn
  forwarder.aluResMemIn       := io.aluResMemIn
  forwarder.val2MemIn         := io.val2MemIn

  forwarder.val1In := io.val1In
  forwarder.val2In := io.val2In

  io.freezeOut := forwarder.freezeOut

  when (io.op1SelectIn === Op1Select.rs1) {
    ALU.op1 := forwarder.aluOp1Out
  } .elsewhen(io.op1SelectIn === Op1Select.PC) {
    ALU.op1 := io.PCIn
  } .otherwise {
    ALU.op1 := 0.U
  }

  
  when (io.controlSignalsIn.jump && io.aluOpIn === Op2Select.DC) {
    ALU.op2 := 4.U
  } .elsewhen (io.op2SelectIn === Op2Select.rs2) {
    ALU.op2 := forwarder.aluOp2Out
  } .elsewhen (io.op2SelectIn === Op2Select.imm) {
    ALU.op2 := io.immediateIn
  } .otherwise {
    ALU.op2 := 0.U
  }

  when (!io.controlSignalsIn.branch) {
    io.takeBranch := false.B
  } .otherwise {
    when (io.branchTypeIn === branchType.beq) {
      io.takeBranch := ALU.aluResult === 0.U
    } .elsewhen(io.branchTypeIn === branchType.neq) {
      io.takeBranch := ALU.aluResult =/= 0.U
    } .elsewhen(io.branchTypeIn === branchType.lt) {
      io.takeBranch := ALU.aluResult.asSInt < 0.S
    } .elsewhen(io.branchTypeIn === branchType.gte) {
      io.takeBranch := ALU.aluResult.asSInt >= 0.S
    } .elsewhen(io.branchTypeIn === branchType.ltu) {
      io.takeBranch := ALU.aluResult.asUInt < 0.U
    } .elsewhen(io.branchTypeIn === branchType.gteu) {
      io.takeBranch := ALU.aluResult.asUInt >= 0.U
    } .otherwise {
      io.takeBranch := false.B
    }
  }

  ALU.aluOp := io.aluOpIn

  io.ALUres := ALU.aluResult
  
  
  // io.op2Out := io.op2SeledtIn

  val instructionWire = Wire(new Instruction)
  instructionWire :=  io.instructionIn
  io.instructionOut := instructionWire

  // handling PC operations
  when(io.controlSignalsIn.jump && io.immTypeIn === ImmFormat.ITYPE) { 
    // use the readValue 1 to create the next PC value
    io.PCOut := io.val1In + io.immediateIn
  } .elsewhen (io.controlSignalsIn.jump || io.controlSignalsIn.branch)  {
    io.PCOut := io.PCIn + io.immediateIn
  } .otherwise {
    io.PCOut := io.PCIn
  }

  io.aluOpOut          := io.aluOpIn
  io.controlSignalsOut := io.controlSignalsIn
  io.branchTypeOut     := io.branchTypeIn
  io.op1SelectOut      := io.op1SelectIn  
  io.op2SelectOut      := io.op2SelectIn
  io.immTypeOut        := io.immTypeIn
  io.val2Out           := io.val2In
  io.immediateOut      := io.immediateIn
}
