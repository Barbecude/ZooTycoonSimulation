execute if block 2 0 0 #midwut:block_states/open[open=false] run return run data modify storage midwut:main output_block.block.Properties.open set value "false"
execute if block 2 0 0 #midwut:block_states/open[open=true] run return run data modify storage midwut:main output_block.block.Properties.open set value "true"
