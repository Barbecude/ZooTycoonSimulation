execute store result score #x main_score run data get storage midwut:main temp[-1].rotated.box[0] 1000
execute store result score #y main_score run data get storage midwut:main temp[-1].rotated.box[1] 1000
execute store result score #z main_score run data get storage midwut:main temp[-1].rotated.box[2] 1000
execute store result storage midwut:main temp[-1].rotated.box[0] int 1 run scoreboard players operation #x main_score -= #mass_center.x main_score
execute store result storage midwut:main temp[-1].rotated.box[1] int 1 run scoreboard players operation #y main_score -= #mass_center.y main_score
execute store result storage midwut:main temp[-1].rotated.box[2] int 1 run scoreboard players operation #z main_score -= #mass_center.z main_score
execute store result score #x main_score run data get storage midwut:main temp[-1].rotated.box[3] 1000
execute store result score #y main_score run data get storage midwut:main temp[-1].rotated.box[4] 1000
execute store result score #z main_score run data get storage midwut:main temp[-1].rotated.box[5] 1000
execute store result storage midwut:main temp[-1].rotated.box[3] int 1 run scoreboard players operation #x main_score -= #mass_center.x main_score
execute store result storage midwut:main temp[-1].rotated.box[4] int 1 run scoreboard players operation #y main_score -= #mass_center.y main_score
execute store result storage midwut:main temp[-1].rotated.box[5] int 1 run scoreboard players operation #z main_score -= #mass_center.z main_score