function physical_vehicles:zzz/vehicle/collision/vertices/y/pn
tag @s add phys_veh.test

execute if score #vp.x main_score matches -2..2 if score #vp.z main_score matches -2..2 run return 1


scoreboard players operation #IN:arctan2.X main_score = #vp.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #vp.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

function physical_vehicles:zzz/vehicle/collision/vertices/y/norm


scoreboard players operation #temp main_score = #vp.y main_score
execute if score #temp main_score matches 1.. run scoreboard players operation #temp main_score *= #-1 const
execute if score #temp main_score matches ..-501 run scoreboard players set #temp main_score -500
scoreboard players operation #friction_coeff main_score *= #temp main_score

scoreboard players operation #t_direction.x main_score *= #friction_coeff main_score
scoreboard players operation #t_direction.z main_score *= #friction_coeff main_score
scoreboard players operation #t_direction.x main_score /= @s phys_veh.mass
scoreboard players operation #t_direction.z main_score /= @s phys_veh.mass
scoreboard players operation #t_direction.x main_score /= @s phys_veh.friction_reduction_ratio
scoreboard players operation #t_direction.z main_score /= @s phys_veh.friction_reduction_ratio

scoreboard players operation #vy.x main_score += #t_direction.x main_score
scoreboard players operation #vy.z main_score += #t_direction.z main_score


scoreboard players operation #x3 main_score = #r.y main_score
scoreboard players operation #x3 main_score *= #t_direction.z main_score
scoreboard players operation #x3 main_score /= #10000 const

scoreboard players operation #y3 main_score = #r.x main_score
scoreboard players operation #y3 main_score *= #t_direction.z main_score
scoreboard players operation #temp main_score = #r.z main_score
scoreboard players operation #temp main_score *= #t_direction.x main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #10000 const

scoreboard players operation #z3 main_score = #r.y main_score
scoreboard players operation #z3 main_score *= #t_direction.x main_score
scoreboard players operation #z3 main_score /= #-10000 const


scoreboard players operation #wy.x main_score += #x3 main_score
scoreboard players operation #wy.y main_score -= #y3 main_score
scoreboard players operation #wy.z main_score += #z3 main_score

scoreboard players add #fr.y main_score 1