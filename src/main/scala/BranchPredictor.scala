package FiveStage

import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util.Cat

class BranchPredictor(val m: Int) extends MultiIOModule {

  val io = IO(new Bundle {
    val address_prediction              = Input(UInt(32.W))
    val predicted_taken                 = Output(Bool())
    val target_address                  = Output(UInt(32.W))

    val address_update                  = Input(UInt(32.W))
    val effective_target_address_update = Input(UInt(32.W))
    val update                          = Input(Bool())
    val taken_update                    = Input(Bool())
    val was_not_branch                  = Input(Bool())
  })

  val TAG_BITS    = 30
  val TARGET_BITS = 32
  val BTB_WIDTH   = 1 + TAG_BITS + TARGET_BITS

  // Mem (not SyncReadMem) so reads are combinational — the pipeline needs the target same-cycle
  val bimodal_mem = Mem(1 << m, UInt(2.W))
  val btb_mem     = Mem(1 << m, UInt(BTB_WIDTH.W))

  // lower m bits of the word-aligned address
  val pcIndex             = io.address_prediction(m + 1, 2)
  val branchToUpdateIndex = io.address_update(m + 1, 2)

  val btb_read = btb_mem.read(pcIndex)

  val btb_write_data = Wire(UInt(BTB_WIDTH.W))
  when(io.was_not_branch) {
    btb_write_data := 0.U(BTB_WIDTH.W)
  }.otherwise {
    btb_write_data := Cat(1.U(1.W), io.address_update(31, 2), io.effective_target_address_update)
  }

  // read-during-write bypass: if update hits the same index we're predicting from
  val btb_final = Wire(UInt(BTB_WIDTH.W))
  btb_final := btb_read
  when(io.update && (pcIndex === branchToUpdateIndex)) {
    btb_final := btb_write_data
  }

  val btb_valid  = btb_final(BTB_WIDTH - 1)
  val btb_tag    = btb_final(TARGET_BITS + TAG_BITS - 1, TARGET_BITS)
  val btb_target = btb_final(TARGET_BITS - 1, 0)

  val pc_tag = io.address_prediction(31, 2)

  io.predicted_taken := bimodal_mem.read(pcIndex)(1)

  // fall through to next instruction if BTB misses or tag doesn't match
  io.target_address := Mux(
    btb_valid && io.predicted_taken && (btb_tag === pc_tag),
    btb_target,
    io.address_prediction + 4.U
  )

  when(io.update) {
    // saturating counter
    val current = bimodal_mem.read(branchToUpdateIndex)
    when(io.taken_update) {
      bimodal_mem.write(branchToUpdateIndex, Mux(current === 3.U, 3.U, current + 1.U))
    }.otherwise {
      bimodal_mem.write(branchToUpdateIndex, Mux(current === 0.U, 0.U, current - 1.U))
    }
    btb_mem.write(branchToUpdateIndex, btb_write_data)
  }
}

object BranchPredictor extends App {
  chisel3.Driver.execute(args, () => new BranchPredictor(10))
}
