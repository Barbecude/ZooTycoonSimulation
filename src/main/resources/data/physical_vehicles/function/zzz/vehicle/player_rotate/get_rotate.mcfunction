scoreboard players operation #phys_veh.adding_angle.y main_score = #phys_veh.rotation.001.y main_score
scoreboard players operation #phys_veh.adding_angle.x main_score = #phys_veh.rotation.001.x main_score
scoreboard players operation #phys_veh.adding_angle.y main_score -= @s phys_veh.adding_angle.y
scoreboard players operation #phys_veh.adding_angle.x main_score -= @s phys_veh.adding_angle.x

scoreboard players operation @s phys_veh.adding_angle.y = #phys_veh.rotation.001.y main_score
scoreboard players operation @s phys_veh.adding_angle.x = #phys_veh.rotation.001.x main_score
