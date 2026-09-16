package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class IDBarrier extends MultiIOModule {

  // val testHarness = IO(
  //   new Bundle {
  //     val setupSignals = Input(new SetupSignals)
  //     val testReadouts = Output(new TestReadouts)
  //     val regUpdates   = Output(new RegisterUpdates)
  //     val memUpdates   = Output(new MemUpdates)
  //     val currentPC    = Output(UInt(32.W))
  //   }
  // )

  val NB_BUBBLES_INS_BRANCH = 2.U


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
     val freezeIn         = Input(Bool())
     val branchTakenIn    = Input(Bool())
     

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
    }
  )
  val nbBubblesInserted   = RegInit(0.U(2.W))
  val regPC               = RegInit(0.U(32.W))
  val regAluOp            = RegInit(0.U(4.W))
  val regControlSignals   = RegInit(0.U.asTypeOf(new ControlSignals))
  val branchType          = RegInit(0.U(3.W))
  val op1Select           = RegInit(0.U(32.W))
  val op2Select           = RegInit(0.U(32.W))
  val immType             = RegInit(0.U(3.W))  
  val val1                = RegInit(0.U(32.W))
  val val2                = RegInit(0.U(32.W))
  val immediate           = RegInit(0.U(32.W))
  val instruction         = RegInit(0.U.asTypeOf(new Instruction))

  when (io.freezeIn) {
    regAluOp               := ALUOps.DC
    regControlSignals      := 0.U.asTypeOf(new ControlSignals)
    branchType             := 7.asUInt(3.W)
    op1Select              := Op1Select.DC
    op2Select              := Op2Select.DC
    instruction            := Instruction.NOP
    nbBubblesInserted      := nbBubblesInserted
  } .elsewhen(io.branchTakenIn || (nbBubblesInserted < NB_BUBBLES_INS_BRANCH && nbBubblesInserted > 0.U)) {
    regAluOp               := ALUOps.DC
    regControlSignals      := ControlSignals.nop
    branchType             := 7.asUInt(3.W)
    op1Select              := Op1Select.DC
    op2Select              := Op2Select.DC
    instruction            := Instruction.NOP
    nbBubblesInserted      := nbBubblesInserted+1.U
    
  } .elsewhen(nbBubblesInserted === NB_BUBBLES_INS_BRANCH) {
    regAluOp               := ALUOps.DC
    regControlSignals      := 0.U.asTypeOf(new ControlSignals)
    branchType             := 7.asUInt(3.W)
    op1Select              := Op1Select.DC
    op2Select              := Op2Select.DC
    instruction            := Instruction.NOP
    nbBubblesInserted      := 0.U
    
  } .otherwise {
    regAluOp               := io.aluOpIn
    regControlSignals      := io.controlSignalsIn
    branchType             := io.branchTypeIn
    op1Select              := io.op1SelectIn
    op2Select              := io.op2SelectIn
    instruction            := io.instructionIn
  }
  regPC                  := io.PCIn
  val1                   := io.val1In
  val2                   := io.val2In
  immType                := io.immTypeIn
  immediate              := io.immediateIn
 
  io.PCOut               := regPC
  io.aluOpOut            := regAluOp
  io.controlSignalsOut   := regControlSignals
  io.branchTypeOut       := branchType
  io.op1SelectOut        := op1Select
  io.op2SelectOut        := op2Select
  io.immTypeOut          := immType
  io.val1Out             := val1
  io.val2Out             := val2
  io.immediateOut        := immediate
  io.instructionOut      := instruction

  



  // val aluOpReg = RegInit(0.U(32.W))
  // aluOpReg := io.aluOpIn
  // io.aluOpOut := aluOpReg

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


  /**
    TODO: Your code here
    */

}
