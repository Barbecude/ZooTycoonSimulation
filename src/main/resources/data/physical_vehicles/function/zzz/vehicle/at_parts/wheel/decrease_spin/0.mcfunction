
execute if score @s phys_veh.spin.x matches -100..100 run return run scoreboard players set @s phys_veh.spin.x 0
execute store success score #neg main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_spin/10
scoreboard players operation @s phys_veh.spin.x *= #999 const
scoreboard players operation @s phys_veh.spin.x /= #100 const
execute if score #neg main_score matches 1 run scoreboard players operation @s phys_veh.spin.x *= #-1 const