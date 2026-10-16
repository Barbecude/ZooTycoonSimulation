execute if entity @s[tag=!phys_veh.wheel] run scoreboard players operation #x main_score = #ang_a main_score
execute if entity @s[tag=phys_veh.wheel] run scoreboard players operation #x main_score = @s phys_veh.
execute if entity @s[tag=!phys_veh.wheel_f] run scoreboard players operation #y main_score = #ang_b main_score
execute if entity @s[tag=phys_veh.wheel_f] run scoreboard players operation #y main_score = #front_w_rot main_score
scoreboard players operation #z main_score = #ang_g main_score

execute if score #y main_score matches 0..1999 run scoreboard players add #x main_score 1000

scoreboard players operation #x main_score /= #1000 const
scoreboard players operation #y main_score /= #1000 const
scoreboard players operation #z main_score /= #1000 const


## берется поворот, убираем 3 знака после ,
## переводим из 360 в 255

scoreboard players operation #x main_score *= #708334 const
scoreboard players operation #y main_score *= #708334 const
scoreboard players operation #z main_score *= #708334 const
scoreboard players operation #x main_score /= #1000000 const
scoreboard players operation #y main_score /= #1000000 const
scoreboard players operation #z main_score /= #1000000 const

## и тут по формуле
scoreboard players operation #x main_score *= #16-bytes const
scoreboard players operation #y main_score *= #8-bytes const

scoreboard players operation #z main_score += #x main_score
scoreboard players operation #z main_score += #y main_score

execute store result entity @s equipment.head.components.minecraft:custom_data.CustomPotionColor int 1 run scoreboard players get #z main_score

