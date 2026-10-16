execute if block 2 0 0 #midwut:block_states/crafting[crafting=false] run return run data modify storage midwut:main output_block.block.Properties.crafting set value "false"
execute if block 2 0 0 #midwut:block_states/crafting[crafting=true] run return run data modify storage midwut:main output_block.block.Properties.crafting set value "true"
