#> midwut:y_functions/get_block/blockstate/main
#data modify storage midwut:main output_block set value {block:{block_state:{Name:"this is a bug, please report it"}}}
data modify storage midwut:main output_block set value {block:{Name:"this is a bug, please report it"}}
function midwut:y_functions/get_block/blockstate/set
execute if block 2 0 0 #midwut:has_blockstate run function midwut:y_functions/get_block/block_states/main
#data modify storage midwut:main output_block.block.BlockState set from storage midwut:main output_block.block
#data modify storage midwut:main output_block.block.TileEntityData set from block 2 0 0

