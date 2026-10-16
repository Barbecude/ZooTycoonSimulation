execute if block 2 0 0 #midwut:block_states/tilt[tilt=full] run return run data modify storage midwut:main output_block.block.Properties.tilt set value "full"
execute if block 2 0 0 #midwut:block_states/tilt[tilt=none] run return run data modify storage midwut:main output_block.block.Properties.tilt set value "none"
execute if block 2 0 0 #midwut:block_states/tilt[tilt=partial] run return run data modify storage midwut:main output_block.block.Properties.tilt set value "partial"
execute if block 2 0 0 #midwut:block_states/tilt[tilt=unstable] run return run data modify storage midwut:main output_block.block.Properties.tilt set value "unstable"
