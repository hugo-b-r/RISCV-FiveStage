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
      val wbValIn             = Input(UInt(32.W))
      val memValIn            = Input(UInt(32.W))
      val controlSignalsMemIn = Input(new ControlSignals)
      val controlSignalsWbIn  = Input(new ControlSignals)


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

      // branch prediction
      val addr_in_decode_stage            = Input(UInt(32.W))
      val address_update                  = Output(UInt(32.W))
      val was_not_branch                  = Output(Bool())
      val effective_target_address_update = Output(UInt(32.W))
      val update_branch_predictor         = Output(Bool())
      val taken_update_branch_predictor   = Output(Bool())

      val instr_in_decode_nop             = Input(Bool())


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
  forwarder.wbValIn           := io.wbValIn
  forwarder.memValIn          := io.memValIn
  forwarder.controlSignalsIn  := io.controlSignalsIn
  forwarder.controlSignalsMemIn := io.controlSignalsMemIn
  forwarder.controlSignalsWbIn  := io.controlSignalsWbIn

  forwarder.val1In := io.val1In
  forwarder.val2In := io.val2In

  // io.freezeOut := forwarder.freezeOut

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

  when (!io.controlSignalsIn.branch && !io.controlSignalsIn.jump) {
    io.taken_update_branch_predictor := false.B
  } .otherwise {
    when (io.branchTypeIn === branchType.beq) {
      io.taken_update_branch_predictor := ALU.aluResult === 0.U
    } .elsewhen(io.branchTypeIn === branchType.neq) {
      io.taken_update_branch_predictor := ALU.aluResult =/= 0.U
    } .elsewhen(io.branchTypeIn === branchType.lt) {
      io.taken_update_branch_predictor := ALU.aluResult.asSInt < 0.S
    } .elsewhen(io.branchTypeIn === branchType.gte) {
      io.taken_update_branch_predictor := ALU.aluResult.asSInt >= 0.S
    } .elsewhen(io.branchTypeIn === branchType.ltu) {
      io.taken_update_branch_predictor := ALU.aluResult.asUInt < 0.U
    } .elsewhen(io.branchTypeIn === branchType.gteu) {
      io.taken_update_branch_predictor := ALU.aluResult.asUInt >= 0.U
    } .elsewhen(io.branchTypeIn === branchType.jump) {
      io.taken_update_branch_predictor := true.B
    } .otherwise {
      io.taken_update_branch_predictor := false.B
    }
  }



  // the value for the next pc if the current instruction is a branch and its condition is verified
  val pcValueIfBranchAndCondVerified = Wire(UInt(32.W))
  // the value for the next pc if the current instruction is a branch
  // val pcValueIfBranch = Wire(UInt(32.W))
    when(io.controlSignalsIn.jump && io.immTypeIn === ImmFormat.ITYPE) { // JALR
      // use the readValue 1 to create the next PC value
      pcValueIfBranchAndCondVerified := (forwarder.aluOp1Out + io.immediateIn) & "hfffffffe".U
    } .elsewhen (io.controlSignalsIn.jump || io.controlSignalsIn.branch)  {
      pcValueIfBranchAndCondVerified := io.PCIn + io.immediateIn
    } .otherwise {
      pcValueIfBranchAndCondVerified := io.PCIn
    }

  io.takeBranch := io.addr_in_decode_stage =/= io.PCOut && !io.instr_in_decode_nop && (io.controlSignalsIn.branch || io.controlSignalsIn.jump) 
  // when (io.addr_in_decode_stage =/= io.PCOut) {
  //   // if not properly fetched, need to stall
  //   io.takeBranch := io.taken_update_branch_predictor
  // } .otherwise {
  //   // if the instruction has been correctly fetched, no need to stall
  //   io.takeBranch := false.B
  // }
  //

  ALU.aluOp := io.aluOpIn

  io.ALUres := ALU.aluResult
  
  
  // io.op2Out := io.op2SeledtIn

  val instructionWire = Wire(new Instruction)
  instructionWire :=  io.instructionIn
  io.instructionOut := instructionWire

  // handling PC operations
  //
  when (io.taken_update_branch_predictor) {
    // branch
    io.PCOut := pcValueIfBranchAndCondVerified
  } .otherwise {
    // no branch
    io.PCOut := io.PCIn + 4.U
  }

  io.aluOpOut          := io.aluOpIn
  io.controlSignalsOut := io.controlSignalsIn
  io.branchTypeOut     := io.branchTypeIn
  io.op1SelectOut      := io.op1SelectIn  
  io.op2SelectOut      := io.op2SelectIn
  io.immTypeOut        := io.immTypeIn
  io.val2Out           := forwarder.aluOp2Out
  io.immediateOut      := io.immediateIn

  // update at the index of the current instruction address
  io.address_update                  := io.PCIn
  io.was_not_branch                  := !io.controlSignalsIn.branch && !io.controlSignalsIn.jump
  io.effective_target_address_update := io.PCOut
  io.update_branch_predictor         := io.addr_in_decode_stage =/= io.PCOut || io.controlSignalsIn.branch || io.controlSignalsIn.jump

}
