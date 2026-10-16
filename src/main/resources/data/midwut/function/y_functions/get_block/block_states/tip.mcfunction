execute if block 2 0 0 #midwut:block_states/tip[tip=false] run return run data modify storage midwut:main output_block.block.Properties.tip set value "false"
execute if block 2 0 0 #midwut:block_states/tip[tip=true] run return run data modify storage midwut:main output_block.block.Properties.tip set value "true"
