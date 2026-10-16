execute if entity @s[tag=phys_veh.tracked_control] run return fail
scoreboard players operation #wheel_angle main_score *= @s phys_veh.wheel.steering_angle
scoreboard players operation @s phys_veh.wheel.angle += #wheel_angle main_score
scoreboard players operation @s phys_veh.wheel.angle < @s phys_veh.wheel.angle.right
scoreboard players operation @s phys_veh.wheel.angle > @s phys_veh.wheel.angle.left
