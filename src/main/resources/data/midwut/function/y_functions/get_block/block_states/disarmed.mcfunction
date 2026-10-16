execute if block 2 0 0 #midwut:block_states/disarmed[disarmed=false] run return run data modify storage midwut:main output_block.block.Properties.disarmed set value "false"
execute if block 2 0 0 #midwut:block_states/disarmed[disarmed=true] run return run data modify storage midwut:main output_block.block.Properties.disarmed set value "true"
