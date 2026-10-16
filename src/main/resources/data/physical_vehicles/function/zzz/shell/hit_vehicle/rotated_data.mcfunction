#data modify storage midwut:main boxes[-1] merge from storage midwut:main boxes[-1].rotated
scoreboard players operation #x2 main_score = #x3 main_score
scoreboard players operation #y2 main_score = #y3 main_score
scoreboard players operation #z2 main_score = #z3 main_score

function physical_vehicles:zzz/shell/hit_vehicle/box

return 1