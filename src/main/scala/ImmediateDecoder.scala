package FiveStage
import chisel3._
import chisel3.util.BitPat
import chisel3.util.ListLookup
import chisel3.util.{ Fill, Cat, MuxLookup }


class ImmediateDecoder() extends Module {

  val io = IO(new Bundle {
    val instruction    = Input(new Instruction)
    val immType        = Input(UInt(3.W))

    val immediate      = Output(UInt(32.W))
  })



  val immediateMap: Array[(UInt, UInt)] = Array(
    ImmFormat.ITYPE -> Cat(Fill(20, io.instruction.immediateIType(11.U)), io.instruction.immediateIType),
    ImmFormat.STYPE -> Cat(Fill(20, io.instruction.immediateSType(11.U)), io.instruction.immediateSType),
    ImmFormat.JTYPE -> Cat(Fill(11, io.instruction.immediateSType(20.U)), io.instruction.immediateJType),
    ImmFormat.UTYPE -> io.instruction.immediateUType.asUInt,
    ImmFormat.BTYPE -> Cat(Fill(20, io.instruction.immediateBType(11.U)), io.instruction.immediateBType),
  )



  io.immediate := MuxLookup(
    io.immType,
    0.U(32.W),
    immediateMap
  )

}



object ImmediateDecoder extends App {
  chisel3.Driver.execute(args, () => new ImmediateDecoder)
  // ChiselStage.emitSystemVerilog(new Tile(), Array("--target-dir", "generated"))
}
