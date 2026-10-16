execute if block 2 0 0 #midwut:block_states/slot_0_occupied[slot_0_occupied=false] run return run data modify storage midwut:main output_block.block.Properties.slot_0_occupied set value "false"
execute if block 2 0 0 #midwut:block_states/slot_0_occupied[slot_0_occupied=true] run return run data modify storage midwut:main output_block.block.Properties.slot_0_occupied set value "true"
