execute if block 2 0 0 #midwut:block_states/cracked[cracked=false] run return run data modify storage midwut:main output_block.block.Properties.cracked set value "false"
execute if block 2 0 0 #midwut:block_states/cracked[cracked=true] run return run data modify storage midwut:main output_block.block.Properties.cracked set value "true"
