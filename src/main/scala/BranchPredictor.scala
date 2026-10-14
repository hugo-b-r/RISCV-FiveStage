package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util.MuxLookup


class BranchPredictor(val m: Int) extends MultiIOModule {



  // You shouldn't really touch these either
  val io = IO(
    new Bundle {
      val address_prediction = Input(UInt(32.W))
      val predicted_taken = Output(Bool())

      val address_update = Input(UInt(32.W))
      val update = Input(Bool())
      val taken_update = Input(Bool())
      val was_not_branch = Input(Bool())

    })

  val bimodal_mem = RegInit(VecInit(Seq.fill(1 << m)(0.U(m.W))))
  val btb_mem = RegInit(VecInit(Seq.fill(1 << m)(0.U(m.W))))

  val branchToUpdateIndex = io.address_update(m+2, 2)

  when (io.update) {
    when (io.taken_update) {
      when(bimodal_mem(branchToUpdateIndex) =/= 3.U) {
        bimodal_mem(branchToUpdateIndex) := (bimodal_mem(branchToUpdateIndex) + 1.U)
      }
    } .otherwise {
      when(bimodal_mem(branchToUpdateIndex) =/= 0.U) {
        bimodal_mem(branchToUpdateIndex) := (bimodal_mem(branchToUpdateIndex) - 1.U)
      }
    }
  }

  // return high order bit which do the all difference
  val pcIndex = io.address_prediction(m+2, 2)
  io.predicted_taken := bimodal_mem(pcIndex)(1)
    
    // reading    
}


object BranchPredictor extends App {
  chisel3.Driver.execute(args, () => new BranchPredictor(1024))
  // ChiselStage.emitSystemVerilog(new Tile(), Array("--target-dir", "generated"))
}
