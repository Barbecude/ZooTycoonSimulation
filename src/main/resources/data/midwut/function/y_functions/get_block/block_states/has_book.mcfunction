execute if block 2 0 0 #midwut:block_states/has_book[has_book=false] run return run data modify storage midwut:main output_block.block.Properties.has_book set value "false"
execute if block 2 0 0 #midwut:block_states/has_book[has_book=true] run return run data modify storage midwut:main output_block.block.Properties.has_book set value "true"
