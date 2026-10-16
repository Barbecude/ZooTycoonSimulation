execute if block 2 0 0 #midwut:block_states/enabled[enabled=false] run return run data modify storage midwut:main output_block.block.Properties.enabled set value "false"
execute if block 2 0 0 #midwut:block_states/enabled[enabled=true] run return run data modify storage midwut:main output_block.block.Properties.enabled set value "true"
