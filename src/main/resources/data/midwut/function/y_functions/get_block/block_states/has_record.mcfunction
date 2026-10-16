execute if block 2 0 0 #midwut:block_states/has_record[has_record=false] run return run data modify storage midwut:main output_block.block.Properties.has_record set value "false"
execute if block 2 0 0 #midwut:block_states/has_record[has_record=true] run return run data modify storage midwut:main output_block.block.Properties.has_record set value "true"
