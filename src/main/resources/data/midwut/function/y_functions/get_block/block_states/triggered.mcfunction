execute if block 2 0 0 #midwut:block_states/triggered[triggered=false] run return run data modify storage midwut:main output_block.block.Properties.triggered set value "false"
execute if block 2 0 0 #midwut:block_states/triggered[triggered=true] run return run data modify storage midwut:main output_block.block.Properties.triggered set value "true"
