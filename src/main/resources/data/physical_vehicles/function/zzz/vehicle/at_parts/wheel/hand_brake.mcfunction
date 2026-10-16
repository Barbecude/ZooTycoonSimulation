scoreboard players set #wheel_acc main_score 1

scoreboard players operation #temp main_score = @s phys_veh.hand_brake_torque
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation #brakes_torque main_score += #temp main_score

#execute if score @s phys_veh.spin.x matches 1.. run scoreboard players operation #wheel_acc main_score -= #brakes_torque main_score
#execute if score @s phys_veh.spin.x matches ..-1 run scoreboard players operation #wheel_acc main_score += #brakes_torque main_score