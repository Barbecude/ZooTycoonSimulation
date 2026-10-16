scoreboard players operation #transmission_w main_score = @s phys_veh.transmission_w
scoreboard players operation #transmission_w main_score *= @s phys_veh.current_gear
scoreboard players operation #transmission_w main_score *= @s phys_veh.final_gear
execute if entity @s[tag=phys_veh.have_transfers] run function physical_vehicles:zzz/vehicle/other/transfer_mul
scoreboard players operation #transmission_w main_score /= #1000 const


scoreboard players set #engagement main_score 100
scoreboard players operation #engagement main_score -= @s phys_veh.control.clutch
scoreboard players operation #clutch_torque main_score = @s phys_veh.engine_w
scoreboard players operation #clutch_torque main_score -= #transmission_w main_score
execute if score #gear_changing main_score matches 1 run scoreboard players set #clutch_torque main_score 0
execute if score @s phys_veh.current_gear matches 0 run scoreboard players set #clutch_torque main_score 0
scoreboard players operation #clutch_torque main_score *= @s phys_veh.clutch_stiffness
scoreboard players operation #clutch_torque main_score *= #engagement main_score
scoreboard players operation #clutch_torque main_score /= #100 const
scoreboard players operation #clutch_torque main_score < @s phys_veh.clutch_max_torque
scoreboard players operation #clutch_torque main_score > @s phys_veh.clutch_max_torque_neg
