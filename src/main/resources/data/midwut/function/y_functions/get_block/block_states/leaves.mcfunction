execute if block 2 0 0 #midwut:block_states/leaves[leaves=large] run return run data modify storage midwut:main output_block.block.Properties.leaves set value "large"
execute if block 2 0 0 #midwut:block_states/leaves[leaves=none] run return run data modify storage midwut:main output_block.block.Properties.leaves set value "none"
execute if block 2 0 0 #midwut:block_states/leaves[leaves=small] run return run data modify storage midwut:main output_block.block.Properties.leaves set value "small"
