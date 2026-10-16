execute if block 2 0 0 #midwut:block_states/face[face=ceiling] run return run data modify storage midwut:main output_block.block.Properties.face set value "ceiling"
execute if block 2 0 0 #midwut:block_states/face[face=floor] run return run data modify storage midwut:main output_block.block.Properties.face set value "floor"
execute if block 2 0 0 #midwut:block_states/face[face=wall] run return run data modify storage midwut:main output_block.block.Properties.face set value "wall"
