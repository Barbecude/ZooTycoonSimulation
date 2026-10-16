execute if entity @s[gamemode=!creative,advancements={midwut:performed/click_on_custom_block={flint_mainhand=true}}] run item modify entity @s weapon.mainhand midwut:damage/flint_and_steel_add_1
execute if entity @s[gamemode=!creative,advancements={midwut:performed/click_on_custom_block={flint_offhand=true}}] run item modify entity @s weapon.offhand midwut:damage/flint_and_steel_add_1

execute store result score #distance main_score run attribute @s minecraft:block_interaction_range get 10
scoreboard players add #distance main_score 5
execute anchored eyes positioned ^ ^ ^ run function midwut:y_functions/custom_block_fixs/raycast

advancement revoke @s only midwut:performed/click_on_custom_block