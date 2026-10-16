scoreboard players add #roll_360 main_score 2000
scoreboard players operation #roll_360 main_score %= #360000 const
scoreboard players operation #angle main_score = #roll_360 main_score

scoreboard players add #angle main_score 180000
scoreboard players operation #angle main_score %= #360000 const
function midwut:test/camera_roll/calc