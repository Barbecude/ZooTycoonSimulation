tag @s add phys_veh.seat_has_add_rotation

execute store result score #Rotation.Y main_score run data get storage midwut:main Data2.data.phys_veh.add_rotation[0] 1000
execute store result score #Rotation.X main_score run data get storage midwut:main Data2.data.phys_veh.add_rotation[1] 1000

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.X main_score %= #360000 const
execute store result storage midwut:main macros.y float -0.0001 run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #Vec.X main_score store result score #Vec.Z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.Y main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

execute store result storage midwut:main macros.x float -0.00000001 run scoreboard players operation #Vec.X main_score *= #OUT:sin main_score
execute store result storage midwut:main macros.z float 0.00000001 run scoreboard players operation #Vec.Z main_score *= #OUT:cos main_score

function physical_vehicles:zzz/summon_data/zzzzzz_data/data/llama_seat_add_rotation_set with storage midwut:main macros


