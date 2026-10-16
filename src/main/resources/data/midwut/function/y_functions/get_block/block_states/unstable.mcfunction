execute if block 2 0 0 #midwut:block_states/unstable[unstable=false] run return run data modify storage midwut:main output_block.block.Properties.unstable set value "false"
execute if block 2 0 0 #midwut:block_states/unstable[unstable=true] run return run data modify storage midwut:main output_block.block.Properties.unstable set value "true"
