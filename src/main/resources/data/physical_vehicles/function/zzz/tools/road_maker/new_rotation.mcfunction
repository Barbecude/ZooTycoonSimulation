data modify storage midwut:main Rotation set from entity @s Rotation
execute unless entity @s[y_rotation=0.00001,x_rotation=-0.00001] store result score @s phys_veh.add_angle.y run data get storage midwut:main Rotation[0] -1000
execute unless entity @s[y_rotation=0.00001,x_rotation=-0.00001] store result score @s phys_veh.add_angle.x run data get storage midwut:main Rotation[1] 1000
execute unless score @s phys_veh.spin.z matches 1 run scoreboard players operation @s phys_veh.add_angle.z = @s phys_veh.spin.z
scoreboard players set @s phys_veh.spin.z 1

rotate @s 0.00001 -0.00001

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation @s phys_veh.add_angle.x %= #360000 const
execute store result score #Vec.Y main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #Vec.X main_score store result score #Vec.Z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #Vec.Y main_score /= #-10 const

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation @s phys_veh.add_angle.y %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #Vec.X main_score *= #OUT:sin main_score
scoreboard players operation #Vec.X main_score /= #100000 const
scoreboard players operation #Vec.Z main_score *= #OUT:cos main_score
scoreboard players operation #Vec.Z main_score /= #100000 const

scoreboard players operation @s phys_veh.velocity.x = #Vec.X main_score
scoreboard players operation @s phys_veh.velocity.y = #Vec.Y main_score
scoreboard players operation @s phys_veh.velocity.z = #Vec.Z main_score



execute store result storage midwut:main trigonometric.in int 0.5 run scoreboard players operation @s phys_veh.add_angle.x %= #360000 const
execute store result score #qw main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #qx main_score = #qw main_score
scoreboard players operation #qy main_score = #qw main_score
scoreboard players operation #qz main_score = #qw main_score
execute store result score #sx main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main trigonometric.in int 0.5 run scoreboard players operation @s phys_veh.add_angle.y %= #360000 const
execute store result score #cy main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score #sy main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result storage midwut:main trigonometric.in int 0.5 run scoreboard players operation @s phys_veh.add_angle.z %= #360000 const
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

scoreboard players operation @s phys_veh.quaternion.x = #qx main_score
scoreboard players operation @s phys_veh.quaternion.y = #qy main_score
scoreboard players operation @s phys_veh.quaternion.z = #qz main_score
scoreboard players operation @s phys_veh.quaternion.w = #qw main_score