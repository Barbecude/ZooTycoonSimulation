# function midwut:test/camera_roll/tick
scoreboard players add #camera_roll main_score 10000
scoreboard players operation #angle main_score = #camera_roll main_score

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #angle main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric

scoreboard players operation #angle main_score = #OUT:sin main_score
scoreboard players operation #angle main_score *= #2 const
scoreboard players add #angle main_score 180000
scoreboard players operation #angle main_score %= #360000 const
function midwut:test/camera_roll/calc