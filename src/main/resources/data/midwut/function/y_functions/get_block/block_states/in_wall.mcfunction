execute if block 2 0 0 #midwut:block_states/in_wall[in_wall=false] run return run data modify storage midwut:main output_block.block.Properties.in_wall set value "false"
execute if block 2 0 0 #midwut:block_states/in_wall[in_wall=true] run return run data modify storage midwut:main output_block.block.Properties.in_wall set value "true"
