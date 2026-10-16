execute if score @s phys_veh.engine_w < @s phys_veh.engine_min_w run return run scoreboard players set @s phys_veh.engine_torque 0
execute if score @s phys_veh.control.throttle matches 0 run return run scoreboard players set @s phys_veh.engine_torque 0

scoreboard players operation #d main_score = @s phys_veh.engine_w
scoreboard players operation #d main_score *= #100 const
scoreboard players operation #d main_score /= @s phys_veh.engine_max_torque_w
execute if score @s phys_veh.engine_torque.a matches 2.. run function physical_vehicles:zzz/vehicle/engine/engine_torque_pre
scoreboard players operation #d main_score /= @s phys_veh.engine_torque.a
scoreboard players set #temp main_score -100
scoreboard players operation #temp main_score /= @s phys_veh.engine_torque.a
scoreboard players add #temp main_score 100
scoreboard players operation #d main_score += #temp main_score
scoreboard players operation #d main_score *= @s phys_veh.engine_max_torque_w
scoreboard players operation @s phys_veh.engine_torque = @s phys_veh.engine_w
scoreboard players operation @s phys_veh.engine_torque *= @s phys_veh.engine_max_torque
scoreboard players operation @s phys_veh.engine_torque *= #1000 const
scoreboard players operation @s phys_veh.engine_torque /= #d main_score
scoreboard players operation @s phys_veh.engine_torque *= @s phys_veh.control.throttle
scoreboard players operation @s phys_veh.engine_torque /= #10 const

