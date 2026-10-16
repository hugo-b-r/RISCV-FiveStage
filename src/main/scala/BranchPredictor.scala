package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util.MuxLookup
import chisel3.util.Cat


class BranchPredictor(val m: Int) extends MultiIOModule {



  // You shouldn't really touch these either
  val io = IO(
    new Bundle {
      val address_prediction = Input(UInt(32.W))
      val predicted_taken = Output(Bool())

      val address_update = Input(UInt(32.W))
      val effective_target_address_update = Input(UInt(32.W))
      val update = Input(Bool())
      val taken_update = Input(Bool())
      val was_not_branch = Input(Bool())

      val target_address = Output(UInt(32.W))

    })

  val bimodal_mem = RegInit(VecInit(Seq.fill(1 << m)(0.U(m.W)))) // 1 validity  + 32 tag address + 32 target address
  val btb_mem = RegInit(VecInit(Seq.fill(1 << m)(0.U(65.W))))

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
  
  val btb_hit = btb_mem(pcIndex)(64).asBool()
  val btb_tag_address = btb_mem(pcIndex)(63, 32)
  val btb_target_address = btb_mem(pcIndex)(31, 0)

  when (btb_hit && io.predicted_taken && btb_tag_address === io.address_prediction) {
    io.target_address := btb_target_address
  } .otherwise {
    io.target_address := io.address_prediction + 4.U
  }

  when (io.update) {
    when (io.was_not_branch) {
      btb_mem(branchToUpdateIndex) := Cat(0.U, btb_mem(branchToUpdateIndex)(64)) // remove first bit = validity bit
    } .otherwise {
      btb_mem(branchToUpdateIndex) := Cat(1.U, io.address_update, io.effective_target_address_update)
    }
  }
}


object BranchPredictor extends App {
  chisel3.Driver.execute(args, () => new BranchPredictor(1024))
  // ChiselStage.emitSystemVerilog(new Tile(), Array("--target-dir", "generated"))
}
