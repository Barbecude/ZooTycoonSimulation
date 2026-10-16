execute if block 2 0 0 #midwut:block_states/attached[attached=false] run return run data modify storage midwut:main output_block.block.Properties.attached set value "false"
execute if block 2 0 0 #midwut:block_states/attached[attached=true] run return run data modify storage midwut:main output_block.block.Properties.attached set value "true"
