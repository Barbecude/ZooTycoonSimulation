execute if block 2 0 0 #midwut:block_states/inverted[inverted=false] run return run data modify storage midwut:main output_block.block.Properties.inverted set value "false"
execute if block 2 0 0 #midwut:block_states/inverted[inverted=true] run return run data modify storage midwut:main output_block.block.Properties.inverted set value "true"
