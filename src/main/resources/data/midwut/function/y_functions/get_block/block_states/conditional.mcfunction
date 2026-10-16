execute if block 2 0 0 #midwut:block_states/conditional[conditional=false] run return run data modify storage midwut:main output_block.block.Properties.conditional set value "false"
execute if block 2 0 0 #midwut:block_states/conditional[conditional=true] run return run data modify storage midwut:main output_block.block.Properties.conditional set value "true"
