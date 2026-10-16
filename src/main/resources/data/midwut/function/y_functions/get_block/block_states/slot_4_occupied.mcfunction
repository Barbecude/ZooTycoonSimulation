execute if block 2 0 0 #midwut:block_states/slot_4_occupied[slot_4_occupied=false] run return run data modify storage midwut:main output_block.block.Properties.slot_4_occupied set value "false"
execute if block 2 0 0 #midwut:block_states/slot_4_occupied[slot_4_occupied=true] run return run data modify storage midwut:main output_block.block.Properties.slot_4_occupied set value "true"
