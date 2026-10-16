execute if score @s phys_veh.id.input matches 0 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input0
execute if score @s phys_veh.id.input matches 1 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input1
execute if score @s phys_veh.id.input matches 2 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input2
execute if score @s phys_veh.id.input matches 3 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input3
execute if score @s phys_veh.id.input matches 4 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input4


execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players operation @s phys_veh.add_angle.x %= #pi2_4 const
execute store result score #qw main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score #qx main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric

execute store result score #phys_veh.angle_x_changed main_score run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/changed_pre

scoreboard players operation #q2x main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2w main_score = @s phys_veh.quaternion.w

scoreboard players operation #q2x main_score *= #qx main_score
scoreboard players operation #q2w main_score *= #qw main_score

scoreboard players operation #temp main_score = @s phys_veh.quaternion.x
scoreboard players operation #temp main_score *= #qw main_score
scoreboard players operation #q2x main_score += #temp main_score
scoreboard players operation @s phys_veh.quaternion.x *= #qx main_score
scoreboard players operation #q2w main_score -= @s phys_veh.quaternion.x

scoreboard players operation #q2y main_score = @s phys_veh.quaternion.y
scoreboard players operation #q2y main_score *= #qw main_score
scoreboard players operation @s phys_veh.quaternion.y *= #qx main_score
scoreboard players operation #q2z main_score = @s phys_veh.quaternion.y

scoreboard players operation #qx main_score *= @s phys_veh.quaternion.z
scoreboard players operation #q2y main_score += #qx main_score
scoreboard players operation @s phys_veh.quaternion.z *= #qw main_score
scoreboard players operation #q2z main_score -= @s phys_veh.quaternion.z

scoreboard players operation #q2x main_score /= #10000 const
scoreboard players operation #q2y main_score /= #10000 const
scoreboard players operation #q2z main_score /= #-10000 const
scoreboard players operation #q2w main_score /= #10000 const

execute store result storage midwut:main left_rotation[0] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.x = #q2x main_score
execute store result storage midwut:main left_rotation[1] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.y = #q2y main_score
execute store result storage midwut:main left_rotation[2] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.z = #q2z main_score
execute store result storage midwut:main left_rotation[3] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.w = #q2w main_score
