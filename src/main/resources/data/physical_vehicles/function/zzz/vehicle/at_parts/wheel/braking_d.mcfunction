execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return fail
#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return run scoreboard players set #wheel_acc main_score -1
scoreboard players set #wheel_acc main_score 1

scoreboard players operation #temp main_score = @s phys_veh.brakes_max_torque
scoreboard players operation #temp main_score *= #phys_veh.control.brake_d main_score
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation #brakes_torque main_score += #temp main_score