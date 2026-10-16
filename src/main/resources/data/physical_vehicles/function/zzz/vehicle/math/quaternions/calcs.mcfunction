#> physical_vehicles:zzz/vehicle/math/quaternions/calcs

execute unless entity @s[scores={phys_veh.add_angle.x=0,phys_veh.add_angle.y=0,phys_veh.add_angle.z=0}] run function physical_vehicles:zzz/vehicle/math/quaternions/add_euler

function physical_vehicles:zzz/vehicle/math/quaternions/to_matrix
#m00 main_score
#m01 main_score
#m02 main_score
#m10 main_score
#m11 main_score
#m12 main_score
#m20 main_score
#m21 main_score
#m22 main_score

execute unless score @s phys_veh.wheel.angle matches 0 run return run function physical_vehicles:zzz/vehicle/math/quaternions/add_euler_wheel

data modify storage midwut:main Data.data.transformation.steered_wheel_rotation set from storage midwut:main Data.data.transformation.main_rotation

scoreboard players operation #phys_veh.unit_vector2.3.001.x main_score = #m02_3 main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.y main_score = #m12_3 main_score
scoreboard players operation #phys_veh.unit_vector2.3.001.z main_score = #m22_3 main_score
