execute if score @s phys_veh.friction_contribution_factor matches 0 run return run scoreboard players set #D main_score 100
scoreboard players remove #D main_score 100
scoreboard players operation #D main_score *= @s phys_veh.friction_contribution_factor
scoreboard players operation #D main_score /= #100 const
scoreboard players add #D main_score 100