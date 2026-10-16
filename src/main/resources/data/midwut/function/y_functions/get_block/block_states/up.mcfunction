execute if block 2 0 0 #midwut:block_states/up[up=false] run return run data modify storage midwut:main output_block.block.Properties.up set value "false"
execute if block 2 0 0 #midwut:block_states/up[up=true] run return run data modify storage midwut:main output_block.block.Properties.up set value "true"
