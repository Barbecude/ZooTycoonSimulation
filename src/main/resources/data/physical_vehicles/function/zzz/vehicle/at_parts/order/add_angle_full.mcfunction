execute if score @s phys_veh.id.input matches 0 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input0
execute if score @s phys_veh.id.input matches 1 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input1
execute if score @s phys_veh.id.input matches 2 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input2
execute if score @s phys_veh.id.input matches 3 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input3
execute if score @s phys_veh.id.input matches 4 run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/input4

execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players operation @s phys_veh.add_angle.x %= #pi2_4 const
execute store result score #qw main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #qx main_score = #qw main_score
scoreboard players operation #qy main_score = #qw main_score
scoreboard players operation #qz main_score = #qw main_score
execute store result score #sx main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players operation @s phys_veh.add_angle.y %= #pi2_4 const
execute store result score #cy main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score #sy main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players operation @s phys_veh.add_angle.z %= #pi2_4 const
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric

scoreboard players operation #qx main_score *= #sy main_score
scoreboard players operation #qx main_score /= #10000 const
scoreboard players operation #qx main_score *= #OUT:sin main_score
scoreboard players operation #qy main_score *= #sy main_score
scoreboard players operation #qy main_score /= #10000 const
scoreboard players operation #qy main_score *= #OUT:cos main_score
scoreboard players operation #qz main_score *= #cy main_score
scoreboard players operation #qz main_score /= #10000 const
scoreboard players operation #qz main_score *= #OUT:sin main_score
scoreboard players operation #qw main_score *= #cy main_score
scoreboard players operation #qw main_score /= #10000 const
scoreboard players operation #qw main_score *= #OUT:cos main_score
scoreboard players operation #temp main_score = #sx main_score
scoreboard players operation #temp main_score *= #cy main_score
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation #temp main_score *= #OUT:cos main_score
scoreboard players operation #qx main_score += #temp main_score
scoreboard players operation #temp main_score = #sx main_score
scoreboard players operation #temp main_score *= #cy main_score
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation #temp main_score *= #OUT:sin main_score
scoreboard players operation #qy main_score -= #temp main_score
scoreboard players operation #temp main_score = #sx main_score
scoreboard players operation #temp main_score *= #sy main_score
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation #temp main_score *= #OUT:cos main_score
scoreboard players operation #qz main_score -= #temp main_score
scoreboard players operation #temp main_score = #sx main_score
scoreboard players operation #temp main_score *= #sy main_score
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation #temp main_score *= #OUT:sin main_score
scoreboard players operation #qw main_score += #temp main_score
scoreboard players operation #qx main_score /= #10000 const
scoreboard players operation #qy main_score /= #10000 const
scoreboard players operation #qz main_score /= #10000 const
scoreboard players operation #qw main_score /= #10000 const



scoreboard players operation #q2x main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2y main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2z main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2w main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2x main_score *= #qx main_score
scoreboard players operation #q2y main_score *= #qy main_score
scoreboard players operation #q2z main_score *= #qz main_score
scoreboard players operation #q2w main_score *= #qw main_score

scoreboard players operation #temp main_score = @s phys_veh.quaternion.x
scoreboard players operation #temp main_score *= #qw main_score
scoreboard players operation #q2x main_score += #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.x
scoreboard players operation #temp main_score *= #qz main_score
scoreboard players operation #q2y main_score -= #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.x
scoreboard players operation #temp main_score *= #qy main_score
scoreboard players operation #q2z main_score += #temp main_score
scoreboard players operation @s phys_veh.quaternion.x *= #qx main_score
scoreboard players operation #q2w main_score -= @s phys_veh.quaternion.x

scoreboard players operation #temp main_score = @s phys_veh.quaternion.y
scoreboard players operation #temp main_score *= #qz main_score
scoreboard players operation #q2x main_score += #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.y
scoreboard players operation #temp main_score *= #qw main_score
scoreboard players operation #q2y main_score += #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.y
scoreboard players operation #temp main_score *= #qx main_score
scoreboard players operation #q2z main_score -= #temp main_score
scoreboard players operation @s phys_veh.quaternion.y *= #qy main_score
scoreboard players operation #q2w main_score -= @s phys_veh.quaternion.y

scoreboard players operation #temp main_score = @s phys_veh.quaternion.z
scoreboard players operation #temp main_score *= #qy main_score
scoreboard players operation #q2x main_score -= #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.z
scoreboard players operation #temp main_score *= #qx main_score
scoreboard players operation #q2y main_score += #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.z
scoreboard players operation #temp main_score *= #qw main_score
scoreboard players operation #q2z main_score += #temp main_score
scoreboard players operation @s phys_veh.quaternion.z *= #qz main_score
scoreboard players operation #q2w main_score -= @s phys_veh.quaternion.z

scoreboard players operation #q2x main_score /= #10000 const
scoreboard players operation #q2y main_score /= #10000 const
scoreboard players operation #q2z main_score /= #10000 const
scoreboard players operation #q2w main_score /= #10000 const

execute store result storage midwut:main left_rotation[0] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.x = #q2x main_score
execute store result storage midwut:main left_rotation[1] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.y = #q2y main_score
execute store result storage midwut:main left_rotation[2] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.z = #q2z main_score
execute store result storage midwut:main left_rotation[3] float 0.0001 run scoreboard players operation @s phys_veh.quaternion.w = #q2w main_score
