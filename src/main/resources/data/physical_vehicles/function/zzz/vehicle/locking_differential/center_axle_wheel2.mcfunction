execute unless score @s phys_veh.spin.x matches -100..100 run scoreboard players add #c main_score 1
scoreboard players add #c2 main_score 1
scoreboard players operation #w main_score += @s phys_veh.spin.x