#rotate @s ~ 0
execute store result score #py main_score run data get entity @s Rotation[0] 1000
#execute store result score #px main_score run data get entity @s Rotation[1] 1000
#rotate @s ~ ~


# #phys_veh.adding_angle.y main_score
# #phys_veh.adding_angle.x main_score

# #phys_veh.unit_vector.xz.x main_score
# #phys_veh.unit_vector.xz.z main_score

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #py main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #OUT:sin main_score *= #-1 const
#OUT:cos main_score

scoreboard players operation #OUT:sin main_score *= #phys_veh.unit_vector.xz.x main_score
scoreboard players operation #OUT:sin main_score /= #100000 const
scoreboard players operation #OUT:cos main_score *= #phys_veh.unit_vector.xz.z main_score
scoreboard players operation #OUT:cos main_score /= #100000 const

scoreboard players operation #OUT:sin main_score += #OUT:cos main_score

scoreboard players operation #temp main_score = #phys_veh.adding_angle.x main_score
scoreboard players operation #temp2 main_score = #phys_veh.adding_angle.y main_score
execute if score #temp main_score matches 90001..269999 run scoreboard players add #temp2 main_score 180000
execute if score #temp main_score matches 90001.. run scoreboard players remove #temp main_score 360000
execute if score #temp main_score matches ..-90001 run scoreboard players add #temp main_score 360000
scoreboard players operation #temp main_score *= #OUT:sin main_score
scoreboard players operation #temp main_score /= #1000 const
execute store result storage midwut:main macros.x float 0.001 run scoreboard players get #temp main_score

#execute if score #temp2 main_score matches 90001.. run scoreboard players remove #temp2 main_score 360000
execute store result storage midwut:main macros.y float 0.001 run scoreboard players get #temp2 main_score
#execute store result storage midwut:main macros.y float 0.001 run scoreboard players operation #py main_score += #phys_veh.adding_angle.y main_score
#execute store result storage midwut:main macros.x float 0.001 run scoreboard players operation #px main_score += #temp main_score
function midwut:help/rotate_macro/add with storage midwut:main macros
