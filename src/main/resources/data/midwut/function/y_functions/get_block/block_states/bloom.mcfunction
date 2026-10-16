execute if block 2 0 0 #midwut:block_states/bloom[bloom=false] run return run data modify storage midwut:main output_block.block.Properties.bloom set value "false"
execute if block 2 0 0 #midwut:block_states/bloom[bloom=true] run return run data modify storage midwut:main output_block.block.Properties.bloom set value "true"
