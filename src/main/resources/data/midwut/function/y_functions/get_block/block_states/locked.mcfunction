execute if block 2 0 0 #midwut:block_states/locked[locked=false] run return run data modify storage midwut:main output_block.block.Properties.locked set value "false"
execute if block 2 0 0 #midwut:block_states/locked[locked=true] run return run data modify storage midwut:main output_block.block.Properties.locked set value "true"
