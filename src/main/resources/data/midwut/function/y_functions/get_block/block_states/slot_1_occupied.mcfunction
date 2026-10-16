execute if block 2 0 0 #midwut:block_states/slot_1_occupied[slot_1_occupied=false] run return run data modify storage midwut:main output_block.block.Properties.slot_1_occupied set value "false"
execute if block 2 0 0 #midwut:block_states/slot_1_occupied[slot_1_occupied=true] run return run data modify storage midwut:main output_block.block.Properties.slot_1_occupied set value "true"
