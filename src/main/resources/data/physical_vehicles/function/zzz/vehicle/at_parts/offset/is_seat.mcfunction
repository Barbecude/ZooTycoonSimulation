function physical_vehicles:zzz/vehicle/at_parts/offset/sit
execute store result storage midwut:main Data.data.Pos[0] double 0.001 run scoreboard players operation #offset.x main_score += #main.pos.x main_score
execute store result storage midwut:main Data.data.Pos[1] double 0.001 run scoreboard players operation #offset.y main_score += #main.pos.y main_score
execute store result storage midwut:main Data.data.Pos[2] double 0.001 run scoreboard players operation #offset.z main_score += #main.pos.z main_score
data modify entity @s Pos set from storage midwut:main Data.data.Pos