execute store result score #mass main_score run data get storage midwut:main DataShell.data.phys_veh.shell.mass 10
function physical_vehicles:zzz/shell/hit_vehicle/resistance

#tellraw @a [{score:{name:"#hit_velocity",objective:"main_score"}}]
scoreboard players operation #hit_velocity main_score *= #hit_velocity main_score
scoreboard players operation #hit_velocity main_score *= #mass main_score
scoreboard players operation #hit_velocity main_score /= #2 const

scoreboard players operation #resistance main_score *= #thickness main_score
scoreboard players operation #resistance main_score *= #25 const

scoreboard players operation #hit_velocity main_score -= #resistance main_score
execute if score #hit_velocity main_score matches ..0 run return 0

scoreboard players operation #hit_velocity main_score /= #mass main_score
scoreboard players operation #number main_score = #hit_velocity main_score
function midwut:help/math/sqrt_int
scoreboard players operation #hit_velocity main_score = #out main_score
execute store result storage midwut:main macros.damage float 1 run scoreboard players get #hit_velocity main_score

execute if score #hit_velocity main_score matches 1.. positioned ^ ^ ^0.1 run function physical_vehicles:zzz/shell/hit_vehicle/ray_loop