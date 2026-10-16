scoreboard players operation #xn0 main_score = #xn main_score
scoreboard players operation #a2 main_score = #a main_score
scoreboard players operation #a2 main_score /= #xn main_score
scoreboard players operation #xn main_score += #a2 main_score
scoreboard players operation #xn main_score /= #2 const

scoreboard players add #t main_score 1
execute unless score #xn main_score = #xn0 main_score if score #t main_score matches ..15 run function physical_vehicles:zzz/vehicle/math/sqrt/loop

