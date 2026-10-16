#> midwut:y_functions/get_block/blockstate_and_item/main
data modify storage midwut:main output_block set value {block:{Name:"this is a bug, please report it"},item:{id:"this is a bug, please report it",count:1}}
function midwut:y_functions/get_block/blockstate_and_item/set
execute if data block 2 0 0 {} run function midwut:y_functions/get_block/item/data
execute if block 2 0 0 #midwut:has_blockstate run function midwut:y_functions/get_block/block_states/main