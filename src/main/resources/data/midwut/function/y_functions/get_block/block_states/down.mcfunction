execute if block 2 0 0 #midwut:block_states/down[down=false] run return run data modify storage midwut:main output_block.block.Properties.down set value "false"
execute if block 2 0 0 #midwut:block_states/down[down=true] run return run data modify storage midwut:main output_block.block.Properties.down set value "true"
