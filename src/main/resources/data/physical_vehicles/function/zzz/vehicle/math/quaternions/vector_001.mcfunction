#> physical_vehicles:zzz/vehicle/math/quaternions/vector_001

execute store result storage midwut:main macros.x float 0.001 run scoreboard players operation #phys_veh.unit_vector.3.001.x main_score = #m02_3 main_score
execute store result storage midwut:main macros.y float 0.001 run scoreboard players operation #phys_veh.unit_vector.3.001.y main_score = #m12_3 main_score
execute store result storage midwut:main macros.z float 0.001 run scoreboard players operation #phys_veh.unit_vector.3.001.z main_score = #m22_3 main_score
function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing with storage midwut:main macros
#data modify storage midwut:main Data.Rotation set from entity @s Rotation

#execute store result score #phys_veh.rotation.001.y main_score run data get storage midwut:main Data.Rotation[0] 1000
#execute store result score #phys_veh.rotation.001.x main_score run data get storage midwut:main Data.Rotation[1] 1000
#scoreboard players operation #phys_veh.rotation.001.y main_score %= #360000 const
#scoreboard players operation #phys_veh.rotation.001.x main_score %= #360000 const

#execute store result storage midwut:main trigonometric.in int 1 run scoreboard players get #phys_veh.rotation.001.y main_score
#execute store result score #phys_veh.unit_vector.xz.x main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
#execute store result score #phys_veh.unit_vector.xz.z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

#scoreboard players operation #phys_veh.unit_vector.xz.x main_score *= #-1 const
#scoreboard players operation #phys_veh.seat_vector.x main_score = #phys_veh.unit_vector.xz.x main_score
#scoreboard players operation #phys_veh.seat_vector.z main_score = #phys_veh.unit_vector.xz.z main_score
#scoreboard players operation #phys_veh.seat_vector.x main_score *= #3 const
#scoreboard players operation #phys_veh.seat_vector.z main_score *= #3 const
#scoreboard players operation #phys_veh.seat_vector.x main_score /= #100 const
#scoreboard players operation #phys_veh.seat_vector.z main_score /= #100 const
#scoreboard players operation #phys_veh.seat_vector.x main_score /= #44 const
#scoreboard players operation #phys_veh.seat_vector.z main_score /= #44 const

#scoreboard players operation #phys_veh.unit_vector.xz.x main_score /= #-10 const
#scoreboard players operation #phys_veh.unit_vector.xz.z main_score /= #10 const