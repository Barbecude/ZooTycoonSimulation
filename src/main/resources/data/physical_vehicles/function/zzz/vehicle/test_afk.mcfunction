scoreboard players set #temp main_score 0
execute if score @s phys_veh.velocity.x matches -1..1 if score @s phys_veh.velocity.y matches -2..2 if score @s phys_veh.velocity.z matches -1..1 if score @s phys_veh.add_rotaion.x matches 0 if score @s phys_veh.add_rotaion.y matches 0 if score @s phys_veh.add_rotaion.z matches 0 if score @s phys_veh.add_rotaion.w matches 0 run scoreboard players set #temp main_score 1

execute if score #temp main_score matches 0 run scoreboard players set @s phys_veh.afk_timer 0
execute if score #temp main_score matches 1 run scoreboard players add @s[scores={phys_veh.afk_timer=..48000}] phys_veh.afk_timer 1
scoreboard players operation #temp main_score = @s phys_veh.afk_timer
execute if score @s phys_veh.afk_timer matches ..160 run return run scoreboard players set #temp main_score 0
execute if score @s phys_veh.afk_timer matches ..18000 run return run scoreboard players operation #temp main_score %= #40 const
execute if score @s phys_veh.afk_timer matches 18001..48000 run return run scoreboard players operation #temp main_score %= #200 const
execute if score @s phys_veh.afk_timer matches 48001 if score #temp main_score matches 0 run return run scoreboard players set #temp main_score 1


