execute if block 2 0 0 #midwut:block_states/signal_fire[signal_fire=false] run return run data modify storage midwut:main output_block.block.Properties.signal_fire set value "false"
execute if block 2 0 0 #midwut:block_states/signal_fire[signal_fire=true] run return run data modify storage midwut:main output_block.block.Properties.signal_fire set value "true"
