execute if block 2 0 0 #midwut:block_states/persistent[persistent=false] run return run data modify storage midwut:main output_block.block.Properties.persistent set value "false"
execute if block 2 0 0 #midwut:block_states/persistent[persistent=true] run return run data modify storage midwut:main output_block.block.Properties.persistent set value "true"
