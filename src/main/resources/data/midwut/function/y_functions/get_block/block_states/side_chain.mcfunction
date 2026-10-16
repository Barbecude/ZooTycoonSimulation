execute if block 2 0 0 #midwut:block_states/side_chain[side_chain=unconnected] run return run data modify storage midwut:main output_block.block.Properties.side_chain set value "unconnected"
execute if block 2 0 0 #midwut:block_states/side_chain[side_chain=center] run return run data modify storage midwut:main output_block.block.Properties.side_chain set value "center"
execute if block 2 0 0 #midwut:block_states/side_chain[side_chain=left] run return run data modify storage midwut:main output_block.block.Properties.side_chain set value "left"
execute if block 2 0 0 #midwut:block_states/side_chain[side_chain=right] run return run data modify storage midwut:main output_block.block.Properties.side_chain set value "right"
