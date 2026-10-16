execute if score @s phys_veh.spin.x matches -20..20 run return run scoreboard players set @s phys_veh.spin.x 0
execute store result score #sign main_score if score @s phys_veh.spin.x matches 1..

scoreboard players operation #brakes_torque main_score *= #1000 const
scoreboard players operation #brakes_torque main_score /= @s phys_veh.moment_of_inertia.x
execute if score #sign main_score matches 1 run scoreboard players operation @s phys_veh.spin.x -= #brakes_torque main_score
execute if score #sign main_score matches 0 run scoreboard players operation @s phys_veh.spin.x += #brakes_torque main_score

execute if score @s phys_veh.spin.x matches 1.. if score #sign main_score matches 0 run return run scoreboard players set @s phys_veh.spin.x 0
execute if score @s phys_veh.spin.x matches ..-1 if score #sign main_score matches 1 run return run scoreboard players set @s phys_veh.spin.x 0