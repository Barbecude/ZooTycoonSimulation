execute if block 2 0 0 #midwut:block_states/hanging[hanging=false] run return run data modify storage midwut:main output_block.block.Properties.hanging set value "false"
execute if block 2 0 0 #midwut:block_states/hanging[hanging=true] run return run data modify storage midwut:main output_block.block.Properties.hanging set value "true"
