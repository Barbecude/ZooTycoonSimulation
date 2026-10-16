execute if score @s phys_veh.spin.x matches 0..99 run scoreboard players set @s phys_veh.spin.x 100
execute if score @s phys_veh.spin.x matches -99..-1 run scoreboard players set @s phys_veh.spin.x -100
scoreboard players operation @s phys_veh.spin.x *= #mul main_score
scoreboard players operation @s phys_veh.spin.x /= #100 const
execute if score @s phys_veh.spin.x matches ..-1 if score #ww main_score matches 1.. run return run scoreboard players operation @s phys_veh.spin.x *= #-1 const
execute if score @s phys_veh.spin.x matches 1.. if score #ww main_score matches ..-1 run return run scoreboard players operation @s phys_veh.spin.x *= #-1 const
