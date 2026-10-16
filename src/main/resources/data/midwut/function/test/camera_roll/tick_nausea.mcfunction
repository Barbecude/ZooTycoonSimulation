scoreboard players add #nausea_coeff main_score 1
execute if score @s custom_effect.nausea matches ..99 run scoreboard players operation #nausea_coeff main_score < @s custom_effect.nausea
scoreboard players remove @s custom_effect.nausea 1
scoreboard players operation #nausea_coeff main_score < #100 const

scoreboard players set #temp main_score 10000
scoreboard players operation @s camera_roll += #temp main_score
scoreboard players operation #angle main_score = @s camera_roll

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #angle main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric

scoreboard players operation #angle main_score = #OUT:sin main_score
scoreboard players operation #angle main_score *= #2 const
scoreboard players operation #angle main_score *= #nausea_coeff main_score
scoreboard players operation #angle main_score /= #100 const
scoreboard players add #angle main_score 180000
scoreboard players operation #angle main_score %= #360000 const
function midwut:test/camera_roll/calc

execute if score @s custom_effect.nausea matches 0 run tag @s remove custom_effect.nausea