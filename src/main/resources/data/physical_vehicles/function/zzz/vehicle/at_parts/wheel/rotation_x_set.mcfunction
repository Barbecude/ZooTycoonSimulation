execute store result storage midwut:main trigonometric.in int 2.86478012446 run scoreboard players operation @s phys_veh.add_angle.x %= #pi2_4 const
execute store result storage midwut:main Data.data.transformation.wheel_x_rotation[0] float 0.0001 run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main Data.data.transformation.wheel_x_rotation[3] float 0.0001 run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
data modify entity @s[tag=!phys_veh.objmc_model] transformation.right_rotation set from storage midwut:main Data.data.transformation.wheel_x_rotation
