execute if block 2 0 0 #midwut:block_states/waterlogged[waterlogged=false] run return run data modify storage midwut:main output_block.block.Properties.waterlogged set value "false"
execute if block 2 0 0 #midwut:block_states/waterlogged[waterlogged=true] run return run data modify storage midwut:main output_block.block.Properties.waterlogged set value "true"
