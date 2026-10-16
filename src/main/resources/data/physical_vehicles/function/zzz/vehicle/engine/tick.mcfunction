
scoreboard players operation #engine_friction_torque main_score = @s phys_veh.engine_w
execute if score @s[tag=phys_veh.ignition] phys_veh.engine_w >= @s phys_veh.engine_min_w run scoreboard players operation #engine_friction_torque main_score -= @s phys_veh.engine_idle_w
scoreboard players operation #engine_friction_torque2 main_score = #engine_friction_torque main_score
scoreboard players operation #sign main_score = #engine_friction_torque main_score
scoreboard players operation #engine_friction_torque main_score *= @s phys_veh.engine_w_friction.b
scoreboard players operation #engine_friction_torque2 main_score *= #engine_friction_torque2 main_score
scoreboard players operation #engine_friction_torque2 main_score *= @s phys_veh.engine_w_friction.c
execute if score #sign main_score matches ..-1 run scoreboard players operation #engine_friction_torque2 main_score *= #-1 const
scoreboard players operation #engine_friction_torque main_score += #engine_friction_torque2 main_score
scoreboard players operation #engine_friction_torque main_score /= #100 const
execute if score #sign main_score matches 1.. run scoreboard players operation #engine_friction_torque main_score += @s phys_veh.engine_w_friction.a
execute if score #sign main_score matches ..-1 run scoreboard players operation #engine_friction_torque main_score -= @s phys_veh.engine_w_friction.a



scoreboard players operation #engine_w_acc main_score = @s phys_veh.engine_torque
scoreboard players operation #engine_w_acc main_score -= #clutch_torque main_score
scoreboard players operation #engine_w_acc main_score -= #engine_friction_torque main_score
function physical_vehicles:zzz/vehicle/engine/engine_w_acc_spin
scoreboard players operation #engine_w_acc main_score *= @s phys_veh.engine_moment_of_inertia
scoreboard players operation #engine_w_acc main_score /= #tick_rate main_score
scoreboard players operation #engine_w_acc main_score /= #1000 const
scoreboard players operation @s phys_veh.engine_w += #engine_w_acc main_score
execute if score @s phys_veh.engine_w matches ..-1 run scoreboard players set @s phys_veh.engine_w 0