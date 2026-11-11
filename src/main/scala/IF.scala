package FiveStage
import chisel3._
import chisel3.experimental.{MultiIOModule, dontTouch}

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )


  /**
    * TODO: Add input signals for handling events such as jumps

    * TODO: Add output signal for the instruction. 
    * The instruction is of type Bundle, which means that you must
    * use the same syntax used in the testHarness for IMEM setup signals
    * further up.
    */
  val io = IO(
    new Bundle {
      val newPC = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val takeBranchIn = Input(Bool())
      val freezeIn = Input(Bool())
      val insertBubbles = Input(Bool())
      
      val PC = Output(UInt(32.W))
      val instruction = Output(new Instruction)
      val instructionNop = Output(Bool())

      val address_update = Input(UInt(32.W))
      val was_not_branch = Input(Bool())
      val effective_target_address_update = Input(UInt(32.W))
      val update_branch_predictor = Input(Bool())
      val taken_update_branch_predictor = Input(Bool())
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)
  val prevPC = RegInit(UInt(32.W), 0.U)
  val branchPredictor  = Module(new BranchPredictor(7)).io


  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress


  /**
    * TODO: Your code here.
    * 
    * You should expand on or rewrite the code below.
    */
  prevPC := PC
  when (io.freezeIn) {
    io.PC := prevPC
  } .otherwise {
    io.PC := PC
  }
  IMEM.io.instructionAddress := io.PC

  when ((io.controlSignalsIn.branch || io.controlSignalsIn.jump) && io.takeBranchIn) {
    PC := io.newPC
  } .otherwise {
    when (io.freezeIn)  {
      PC := PC
    } .otherwise {
      PC := branchPredictor.target_address
    }
  }
  val instruction = Wire(new Instruction)
  instruction := io.instruction
  when (io.insertBubbles) {
    io.instruction := Instruction.NOP
  }.otherwise {
    io.instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  }


  // emit nops during setup as we don't want to use the circuit, only load data into memory
  io.instructionNop := testHarness.IMEMsetup.setup



  /**
    * Setup. You should not change this code.
    */
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }
  dontTouch(io.PC)
  dontTouch(io.newPC)


  when (testHarness.IMEMsetup.setup) {
    branchPredictor.address_prediction := 0.U
    branchPredictor.address_update := 0.U
    branchPredictor.was_not_branch := true.B
    branchPredictor.effective_target_address_update:= 0.U
    branchPredictor.update := false.B
branchPredictor.taken_update := false.B
  } .otherwise {
    branchPredictor.address_prediction := io.PC
    branchPredictor.address_update := io.address_update
    branchPredictor.was_not_branch := io.was_not_branch 
    branchPredictor.effective_target_address_update:= io.effective_target_address_update
    branchPredictor.update := io.update_branch_predictor
    branchPredictor.taken_update :=  io.taken_update_branch_predictor
  }
}
