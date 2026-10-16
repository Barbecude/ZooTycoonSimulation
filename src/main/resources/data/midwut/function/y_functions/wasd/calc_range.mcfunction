
## arctan2(Motion[2],Motion[0]) + Rotation[0]

function midwut:help/math/trig_func/arc/tan2

scoreboard players operation #midwut.temp main_score = #OUT:arctan2 main_score
scoreboard players operation #OUT:arctan2 main_score -= #Rotation.Y main_score
scoreboard players operation #OUT:arctan2 main_score %= #360000 const
scoreboard players operation #Rotation.Y main_score = #midwut.temp main_score

execute store result score #WASD.D main_score if score #OUT:arctan2 main_score matches 23000..157000
execute store result score #WASD.S main_score if score #OUT:arctan2 main_score matches 112000..248000
execute store result score #WASD.A main_score if score #OUT:arctan2 main_score matches 203000..337000
execute store result score #WASD.W main_score if score #OUT:arctan2 main_score matches 293000..360000
execute unless score #WASD.W main_score matches 1 store result score #WASD.W main_score if score #OUT:arctan2 main_score matches 0..67000

scoreboard players set #WASD.All main_score 1
