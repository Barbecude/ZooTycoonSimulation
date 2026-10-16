
# x = sx * cz
# y = - sx * sz
# z = cx * sz
# w = cx * cz

#

scoreboard players operation #z main_score = @s phys_veh.wheel.angle
scoreboard players operation #z main_score *= @s phys_veh.steering_wheel.multiplier

scoreboard players operation #qx main_score = @s phys_veh.steering_wheel.x_rotation_sin
scoreboard players operation #qy main_score = @s phys_veh.steering_wheel.x_rotation_sin
scoreboard players operation #qz main_score = @s phys_veh.steering_wheel.x_rotation_cos
scoreboard players operation #qw main_score = @s phys_veh.steering_wheel.x_rotation_cos
execute store result storage midwut:main trigonometric.in int 0.0286478012446 run scoreboard players operation #z main_score %= #pi2_6 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result storage midwut:main Data.data.transformation.steering_wheel_rotation[0] float 0.00000001 run scoreboard players operation #qx main_score *= #OUT:cos main_score
execute store result storage midwut:main Data.data.transformation.steering_wheel_rotation[1] float 0.00000001 run scoreboard players operation #qy main_score *= #OUT:sin main_score
execute store result storage midwut:main Data.data.transformation.steering_wheel_rotation[2] float -0.00000001 run scoreboard players operation #qz main_score *= #OUT:sin main_score
execute store result storage midwut:main Data.data.transformation.steering_wheel_rotation[3] float 0.00000001 run scoreboard players operation #qw main_score *= #OUT:cos main_score
