execute if score @s phys_veh.current_gear matches 0 run return run scoreboard players set #engine_transmission_torque main_score 0

scoreboard players operation #engine_transmission_torque main_score = #clutch_torque main_score
scoreboard players operation #engine_transmission_torque main_score /= #100 const
scoreboard players operation #engine_transmission_torque main_score *= @s phys_veh.current_gear
scoreboard players operation #engine_transmission_torque main_score *= @s phys_veh.final_gear
execute if entity @s[tag=phys_veh.have_transfers] run function physical_vehicles:zzz/vehicle/other/transfer_mul_ett
#tellraw @a [{score:{name:"#engine_transmission_torque",objective:"main_score"}}]
scoreboard players operation #engine_transmission_torque main_score /= #100 const
scoreboard players operation #engine_transmission_torque main_score *= @s phys_veh.gearbox_n
scoreboard players operation #engine_transmission_torque main_score /= #tick_rate main_score
scoreboard players operation #engine_transmission_torque main_score /= @s phys_veh.count_driving_wheels
