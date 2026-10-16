execute if block 2 0 0 #midwut:block_states/can_summon[can_summon=false] run return run data modify storage midwut:main output_block.block.Properties.can_summon set value "false"
execute if block 2 0 0 #midwut:block_states/can_summon[can_summon=true] run return run data modify storage midwut:main output_block.block.Properties.can_summon set value "true"
