execute if score #y main_score matches ..-1 run scoreboard players add #y main_score 360

execute if score #y main_score matches 0..44 run setblock ~ ~ ~ piston[facing=south]
execute if score #y main_score matches 45..134 run setblock ~ ~ ~ piston[facing=west]
execute if score #y main_score matches 135..224 run setblock ~ ~ ~ piston[facing=north]
execute if score #y main_score matches 225..314 run setblock ~ ~ ~ piston[facing=east]
execute if score #y main_score matches 315..360 run setblock ~ ~ ~ piston[facing=south]


