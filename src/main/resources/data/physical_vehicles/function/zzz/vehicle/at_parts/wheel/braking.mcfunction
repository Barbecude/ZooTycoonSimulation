execute if entity @s[tag=phys_veh.tracked_control,tag=phys_veh.have_on_board_clutch_lock] unless score #wheel_angle main_score matches 0 run return fail
scoreboard players set #wheel_acc main_score 1

scoreboard players operation #temp main_score = @s phys_veh.brakes_max_torque
scoreboard players operation #temp main_score *= #phys_veh.control.brake main_score
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation #brakes_torque main_score += #temp main_score
#execute if score @s phys_veh.spin.x matches 1.. run scoreboard players operation #wheel_acc main_score -= #brakes_torque main_score
#execute if score @s phys_veh.spin.x matches ..-1 run scoreboard players operation #wheel_acc main_score += #brakes_torque main_score
return 1