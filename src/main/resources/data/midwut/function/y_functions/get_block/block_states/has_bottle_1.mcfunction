execute if block 2 0 0 #midwut:block_states/has_bottle_1[has_bottle_1=false] run return run data modify storage midwut:main output_block.block.Properties.has_bottle_1 set value "false"
execute if block 2 0 0 #midwut:block_states/has_bottle_1[has_bottle_1=true] run return run data modify storage midwut:main output_block.block.Properties.has_bottle_1 set value "true"
