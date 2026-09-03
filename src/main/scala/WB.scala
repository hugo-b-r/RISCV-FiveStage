package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class WriteBack() extends MultiIOModule {


  val io = IO(
    new Bundle {
    val instructionIn  = Input(new Instruction)
    val ALUresIn         = Input(UInt(32.W))
    val dataMemoryIn   = Input(UInt(32.W))
    val controlSignalsIn = Input(new ControlSignals)
    
    val instructionOut = Output(new Instruction)
    val dataOut        = Output(UInt(32.W))
    val controlSignalsOut = Output(new ControlSignals)
  })

  io.instructionOut := io.instructionIn
  when (io.controlSignalsOut.memRead) {
    io.dataOut := io.dataMemoryIn
  } .otherwise {
    io.dataOut := io.ALUresIn
  }

  io.controlSignalsOut := io.controlSignalsIn
  

}
