execute if block 2 0 0 #midwut:block_states/slot_2_occupied[slot_2_occupied=false] run return run data modify storage midwut:main output_block.block.Properties.slot_2_occupied set value "false"
execute if block 2 0 0 #midwut:block_states/slot_2_occupied[slot_2_occupied=true] run return run data modify storage midwut:main output_block.block.Properties.slot_2_occupied set value "true"
