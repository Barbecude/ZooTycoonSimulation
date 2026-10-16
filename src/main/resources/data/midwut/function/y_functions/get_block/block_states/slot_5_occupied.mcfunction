execute if block 2 0 0 #midwut:block_states/slot_5_occupied[slot_5_occupied=false] run return run data modify storage midwut:main output_block.block.Properties.slot_5_occupied set value "false"
execute if block 2 0 0 #midwut:block_states/slot_5_occupied[slot_5_occupied=true] run return run data modify storage midwut:main output_block.block.Properties.slot_5_occupied set value "true"
