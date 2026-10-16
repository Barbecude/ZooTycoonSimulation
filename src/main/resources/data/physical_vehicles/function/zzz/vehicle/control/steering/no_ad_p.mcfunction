scoreboard players operation #w_rot main_score -= @s phys_veh.wheel.steering_angle
execute if score #w_rot main_score matches ..-1 run scoreboard players set #w_rot main_score 0