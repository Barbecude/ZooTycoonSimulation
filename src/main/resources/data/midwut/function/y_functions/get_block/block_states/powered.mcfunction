execute if block 2 0 0 #midwut:block_states/powered[powered=false] run return run data modify storage midwut:main output_block.block.Properties.powered set value "false"
execute if block 2 0 0 #midwut:block_states/powered[powered=true] run return run data modify storage midwut:main output_block.block.Properties.powered set value "true"
