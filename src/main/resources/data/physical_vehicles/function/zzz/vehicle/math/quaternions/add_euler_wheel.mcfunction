scoreboard players operation #wheel_angle main_score = @s phys_veh.wheel.angle
execute store result storage midwut:main trigonometric.in int 5.72956024892 run scoreboard players operation #wheel_angle main_score %= #pi2_4 const
execute store result score #phys_veh.wheel.steering.cos main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score #phys_veh.wheel.steering.sin main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players get #wheel_angle main_score
execute store result score #q3w main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score #q3y main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
scoreboard players operation #phys_veh.wheel.steering.cos main_score /= #10 const
scoreboard players operation #phys_veh.wheel.steering.sin main_score /= #10 const

scoreboard players operation #q2y main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2w main_score = @s phys_veh.quaternion.w
scoreboard players operation #q2y main_score *= #q3y main_score
scoreboard players operation #q2w main_score *= #q3w main_score

scoreboard players operation #q2x main_score = @s phys_veh.quaternion.x
scoreboard players operation #q2x main_score *= #q3w main_score
scoreboard players operation #q2z main_score = @s phys_veh.quaternion.x
scoreboard players operation #q2z main_score *= #q3y main_score

scoreboard players operation #temp main_score = @s phys_veh.quaternion.y
scoreboard players operation #temp main_score *= #q3w main_score
scoreboard players operation #q2y main_score += #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.y
scoreboard players operation #temp main_score *= #q3y main_score
scoreboard players operation #q2w main_score -= #temp main_score

scoreboard players operation #temp main_score = @s phys_veh.quaternion.z
scoreboard players operation #temp main_score *= #q3y main_score
scoreboard players operation #q2x main_score -= #temp main_score
scoreboard players operation #temp main_score = @s phys_veh.quaternion.z
scoreboard players operation #temp main_score *= #q3w main_score
scoreboard players operation #q2z main_score += #temp main_score


execute store result storage midwut:main Data.data.transformation.steered_wheel_rotation[0] float 0.0001 run scoreboard players operation #q2x main_score /= #10000 const
execute store result storage midwut:main Data.data.transformation.steered_wheel_rotation[1] float 0.0001 run scoreboard players operation #q2y main_score /= #10000 const
execute store result storage midwut:main Data.data.transformation.steered_wheel_rotation[2] float 0.0001 run scoreboard players operation #q2z main_score /= #10000 const
execute store result storage midwut:main Data.data.transformation.steered_wheel_rotation[3] float 0.0001 run scoreboard players operation #q2w main_score /= #10000 const

data modify entity @s data.transformation.steered_wheel_rotation set from storage midwut:main Data.data.transformation.steered_wheel_rotation


scoreboard players operation #x22 main_score = #q2x main_score
scoreboard players operation #x22 main_score *= #q2x main_score
scoreboard players operation #x22 main_score /= #50000 const
scoreboard players operation #y22 main_score = #q2y main_score
scoreboard players operation #y22 main_score *= #q2y main_score
scoreboard players operation #y22 main_score /= #50000 const

scoreboard players set #phys_veh.unit_vector2.3.001.z main_score 1000
scoreboard players operation #phys_veh.unit_vector2.3.001.z main_score -= #x22 main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.z main_score -= #y22 main_score

scoreboard players operation #phys_veh.unit_vector2.3.001.y main_score = #q2z main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.y main_score *= #q2y main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.x main_score = #q2z main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.x main_score *= #q2x main_score
scoreboard players operation #xw main_score = #q2x main_score
scoreboard players operation #xw main_score *= #q2w main_score
scoreboard players operation #yw main_score = #q2y main_score
scoreboard players operation #yw main_score *= #q2w main_score

scoreboard players operation #phys_veh.unit_vector2.3.001.x main_score += #yw main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.x main_score /= #50000 const
scoreboard players operation #phys_veh.unit_vector2.3.001.y main_score -= #xw main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.y main_score /= #50000 const
