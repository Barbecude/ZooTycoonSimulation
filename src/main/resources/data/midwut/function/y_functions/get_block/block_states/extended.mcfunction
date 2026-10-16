execute if block 2 0 0 #midwut:block_states/extended[extended=false] run return run data modify storage midwut:main output_block.block.Properties.extended set value "false"
execute if block 2 0 0 #midwut:block_states/extended[extended=true] run return run data modify storage midwut:main output_block.block.Properties.extended set value "true"
