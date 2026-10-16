execute if block 2 0 0 #midwut:block_states/vault_state[vault_state=active] run return run data modify storage midwut:main output_block.block.Properties.vault_state set value "active"
execute if block 2 0 0 #midwut:block_states/vault_state[vault_state=ejecting] run return run data modify storage midwut:main output_block.block.Properties.vault_state set value "ejecting"
execute if block 2 0 0 #midwut:block_states/vault_state[vault_state=inactive] run return run data modify storage midwut:main output_block.block.Properties.vault_state set value "inactive"
execute if block 2 0 0 #midwut:block_states/vault_state[vault_state=unlocking] run return run data modify storage midwut:main output_block.block.Properties.vault_state set value "unlocking"
