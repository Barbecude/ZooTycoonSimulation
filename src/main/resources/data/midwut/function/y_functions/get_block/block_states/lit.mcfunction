execute if block 2 0 0 #midwut:block_states/lit[lit=false] run return run data modify storage midwut:main output_block.block.Properties.lit set value "false"
execute if block 2 0 0 #midwut:block_states/lit[lit=true] run return run data modify storage midwut:main output_block.block.Properties.lit set value "true"
