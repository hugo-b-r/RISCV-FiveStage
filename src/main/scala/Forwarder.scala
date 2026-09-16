package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util.MuxLookup
import chisel3.util.BitPat
import lookup._
import chisel3.experimental.dontTouch


/**
  * This module implement the forwarder
  *  
 */
class Forwarder() extends MultiIOModule {


  val io = IO(
    new Bundle {
      val val1In              = Input(UInt(32.W))
      val val2In              = Input(UInt(32.W))
      val controlSignalsIn    = Input(new ControlSignals)
      val controlSignalsMemIn = Input(new ControlSignals)
      val controlSignalsWbIn  = Input(new ControlSignals)
      val instructionMemIn    = Input(new Instruction)
      val rdAddressMemIn      = Input(UInt(5.W))
      val rdAddressWbIn       = Input(UInt(5.W))
      val rs1AddressExIn      = Input(UInt(5.W))
      val rs2AddressExIn      = Input(UInt(5.W))
      val memValIn            = Input(UInt(32.W))
      val wbValIn             = Input(UInt(32.W))

      val aluOp1Out = Output(UInt(32.W))
      val aluOp2Out = Output(UInt(32.W))
      // val freezeOut = Output(Bool())

      val dbg1 = Output(UInt(32.W))
      val dbg2 = Output(UInt(32.W))


    })

      // dontTouch(io.memResWbIn)
      // dontTouch(io.aluValWbIn)
      // dontTouch(io.aluResMemIn)
      // dontTouch(io.val2MemIn)
    val choiceDebug1 = Wire(UInt(32.W))
    val choiceDebug2 = Wire(UInt(32.W))

    io.dbg1 := choiceDebug1
    io.dbg2 := choiceDebug2
    dontTouch(io.dbg1)
    dontTouch(io.dbg2)

    when (io.controlSignalsMemIn.regWrite &&
      io.rdAddressMemIn =/= 0.U &&
      io.rs1AddressExIn === io.rdAddressMemIn) {
        io.aluOp1Out := io.memValIn
        choiceDebug1 := 1.U
    } .elsewhen(io.controlSignalsWbIn.regWrite
      && io.rdAddressWbIn  =/= 0.U
      && io.rdAddressMemIn =/= io.rs1AddressExIn
      && io.rdAddressWbIn  === io.rs1AddressExIn) {
        io.aluOp1Out := io.wbValIn
        choiceDebug1 := 2.U
    } .otherwise {
      io.aluOp1Out := io.val1In
      choiceDebug1 := 3.U
    }
    when (io.controlSignalsMemIn.regWrite &&
      io.rdAddressMemIn =/= 0.U &&
      io.rs2AddressExIn === io.rdAddressMemIn) {
        io.aluOp2Out := io.memValIn
        choiceDebug2 := 1.U
    } .elsewhen(io.controlSignalsWbIn.regWrite
      && io.rdAddressWbIn  =/= 0.U
      && io.rdAddressMemIn =/= io.rs2AddressExIn
      && io.rdAddressWbIn  === io.rs2AddressExIn) {
        io.aluOp2Out := io.wbValIn
        choiceDebug2 := 2.U
    } .otherwise {
      io.aluOp2Out := io.val2In
      choiceDebug2 := 3.U
    }

    
    // val opcode = Wire(UInt(32.W))
    // opcode := io.instructionMemIn.opcode
    // dontTouch(opcode)

    // val freezeWire = Wire(Bool())
    // val freezeReg = Reg(Bool())
    // dontTouch(freezeWire)
    // when(io.controlSignalsMemIn.memRead
    // && (io.rdAddressMemIn === io.rs1AddressExIn
    //   || io.rdAddressMemIn === io.rs2AddressExIn
    // )) {
    //   freezeWire := true.B
    //   // freezeWire := true.B
    // } .otherwise {
    //   freezeWire := false.B
    // }
    // freezeReg := freezeWire
    // io.freezeOut := freezeWire
    // // println(io.instructionMemIn.opcode)
      
}
