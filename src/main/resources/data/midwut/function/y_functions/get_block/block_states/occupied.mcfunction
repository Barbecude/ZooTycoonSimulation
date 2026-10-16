execute if block 2 0 0 #midwut:block_states/occupied[occupied=false] run return run data modify storage midwut:main output_block.block.Properties.occupied set value "false"
execute if block 2 0 0 #midwut:block_states/occupied[occupied=true] run return run data modify storage midwut:main output_block.block.Properties.occupied set value "true"
