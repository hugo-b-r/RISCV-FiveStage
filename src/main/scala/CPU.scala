package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  val IFBarrier  = Module(new IFBarrier).io
  val IDBarrier  = Module(new IDBarrier).io
  val EXBarrier  = Module(new EXBarrier).io
  val MEMBarrier = Module(new MEMBarrier).io

  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB = Module(new WriteBack)


  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC



  // IFBarrier first barrier
  IFBarrier.instructionIn := IF.io.instruction
  IFBarrier.PCIn := IF.io.PC

  
  ID.io.PCIn := IFBarrier.PCOut
  ID.io.instructionIn := IFBarrier.instructionOut


  // IDBarrier second barrier
  IDBarrier.PCIn := ID.io.PCOut
  IDBarrier.aluOpIn := ID.io.aluOpOut
  IDBarrier.controlSignalsIn := ID.io.controlSignalsOut
  IDBarrier.branchTypeIn := ID.io.branchTypeOut
  IDBarrier.op1SelectIn := ID.io.op1SelectOut
  IDBarrier.op2SelectIn := ID.io.op2SelectOut
  IDBarrier.immTypeIn := ID.io.immTypeOut
  IDBarrier.val1In := ID.io.val1Out
  IDBarrier.val2In := ID.io.val2Out
  IDBarrier.immediateIn := ID.io.immediateOut
  IDBarrier.instructionIn := ID.io.instructionOut


  EX.io.PCIn := IDBarrier.PCOut
  EX.io.aluOpIn := IDBarrier.aluOpOut
  EX.io.controlSignalsIn := IDBarrier.controlSignalsOut
  EX.io.branchTypeIn := IDBarrier.branchTypeOut
  EX.io.op1SelectIn := IDBarrier.op1SelectOut
  EX.io.op2SelectIn := IDBarrier.op2SelectOut
  EX.io.immTypeIn := IDBarrier.immTypeOut
  EX.io.val1In := IDBarrier.val1Out
  EX.io.val2In := IDBarrier.val2Out
  EX.io.immediateIn := IDBarrier.immediateOut
  EX.io.instructionIn := IDBarrier.instructionOut



  // EXBarrier connections
  EXBarrier.PCIn := EX.io.PCOut
  EXBarrier.resultIn := EX.io.ALUres
  EXBarrier.val2In := EX.io.val2Out
  EXBarrier.instructionIn := EX.io.instructionOut
  EXBarrier.controlSignalsIn := EX.io.controlSignalsOut
  EXBarrier.takeBranchIn := EX.io.takeBranch

  
  MEM.io.aluResIn := EXBarrier.aluResOut
  MEM.io.instructionIn := EXBarrier.instructionOut
  MEM.io.dataAddressIn := EXBarrier.dataAddressOut
  MEM.io.controlSignals := EXBarrier.controlSignalsOut

  MEMBarrier.ALUresIn := MEM.io.aluResOut
  MEMBarrier.readDataIn := MEM.io.readDataOut
  MEMBarrier.instructionIn := MEM.io.instructionOut
  MEMBarrier.controlSignalsIn := MEM.io.controlSignalsOut

  // WB.io.instructionIn := MEMBarrier.instructionOut
  // WB.io.ALUresIn := MEMBarrier.ALUresOut
  // WB.io.dataMemoryIn := MEMBarrier.readDataOut
  // WB.io.controlSignalsIn:= MEMBarrier.controlSignalsOut

  IF.io.controlSignalsIn  := EXBarrier.controlSignalsOut
  IF.io.newPC             := EXBarrier.PCOut
  IF.io.takeBranchIn      := EXBarrier.takeBranchOut
  IF.io.freezeIn          := ID.io.freezeOut
  IF.io.insertBubbles     := (EX.io.takeBranch && (IDBarrier.controlSignalsOut.branch || IDBarrier.controlSignalsOut.jump))

  // branch prediction
  IF.io.address_update                  := EX.io.address_update
  IF.io.was_not_branch                  := EX.io.was_not_branch
  IF.io.effective_target_address_update := EX.io.effective_target_address_update
  IF.io.update_branch_predictor         := EX.io.update_branch_predictor
  IF.io.taken_update_branch_predictor   := EX.io.taken_update_branch_predictor

  IFBarrier.branchTakenIn := EXBarrier.takeBranchOut
  IDBarrier.branchTakenIn := EXBarrier.takeBranchOut
  IDBarrier.freezeIn      := ID.io.freezeOut
  IDBarrier.insertBubbles := IF.io.insertBubbles
    
  ID.io.delayedControlSignals := MEMBarrier.controlSignalsOut
  when (MEMBarrier.controlSignalsOut.memRead) {
    ID.io.delayedWriteData := MEMBarrier.readDataOut
    EX.io.wbValIn          := MEMBarrier.readDataOut
  } .otherwise {
    ID.io.delayedWriteData := MEMBarrier.ALUresOut
    EX.io.wbValIn          := MEMBarrier.ALUresOut
  }
  ID.io.delayedInstruction := MEMBarrier.instructionOut
  ID.io.controlSignalsExIn := EX.io.controlSignalsIn
  ID.io.rdAddressExIn      := EX.io.instructionIn.registerRd


  EX.io.instructionMemIn     := EXBarrier.instructionOut
  EX.io.rdAddressMemIn       := EXBarrier.instructionOut.registerRd
  EX.io.rdAddressWbIn        := MEMBarrier.instructionOut.registerRd
  EX.io.memValIn             := MEM.io.aluResOut
  EX.io.controlSignalsMemIn  := MEM.io.controlSignalsOut
  EX.io.controlSignalsWbIn   := MEMBarrier.controlSignalsOut
  EX.io.addr_in_decode_stage := ID.io.PCIn

  // branch prediction
  
  
  // IFBarrier.freezeIn  := EX.io.freezeOut
  // IDBarrier.freezeIn  := EX.io.freezeOut
  // EXBarrier.freezeIn := EX.io.freezeOut

  // WB
  
  // MEMBarrier connections
}
