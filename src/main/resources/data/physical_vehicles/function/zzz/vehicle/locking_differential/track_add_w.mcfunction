execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return 1

scoreboard players operation #engine_w_a main_score = @s phys_veh.engine_w
scoreboard players operation #transmission_w main_score = @s phys_veh.current_gear
scoreboard players operation #transmission_w main_score *= @s phys_veh.final_gear
execute if entity @s[tag=phys_veh.have_transfers] run function physical_vehicles:zzz/vehicle/other/transfer_mul
scoreboard players operation #engine_w_a main_score *= #1000 const
scoreboard players operation #engine_w_a main_score /= #transmission_w main_score
#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return run scoreboard players operation @s phys_veh.transmission_w += #engine_w_a main_score
#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return run scoreboard players operation @s phys_veh.transmission_w += #engine_w_a main_score

scoreboard players operation #engine_w_d main_score = #engine_w_a main_score
scoreboard players operation #engine_w_a main_score *= @s phys_veh.control.brake_a
scoreboard players operation #engine_w_d main_score *= @s phys_veh.control.brake_d
scoreboard players operation #engine_w_a main_score /= #100 const
scoreboard players operation #engine_w_d main_score /= #100 const

scoreboard players operation @s phys_veh.transmission_w += #engine_w_a main_score
scoreboard players operation @s phys_veh.transmission_w += #engine_w_d main_score