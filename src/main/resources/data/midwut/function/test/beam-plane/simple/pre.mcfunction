#data modify storage midwut:main temp3 set from storage midwut:main Blocks

data modify storage midwut:main temp set from entity @s

execute store result score #Rotation.Y main_score run data get storage midwut:main temp.Rotation[0] 1000
execute store result score #Rotation.X main_score run data get storage midwut:main temp.Rotation[1] 1000

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.X main_score %= #360000 const
execute store result score #Vec.Y main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #Vec.X main_score store result score #Vec.Z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #Vec.Y main_score *= #-1 const

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.Y main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #Vec.X main_score *= #OUT:sin main_score
scoreboard players operation #Vec.X main_score /= #-10000 const
scoreboard players operation #Vec.Z main_score *= #OUT:cos main_score
scoreboard players operation #Vec.Z main_score /= #10000 const

execute store result score #raycast.pos.X main_score run data get storage midwut:main temp.Pos[0] 1000
execute store result score #raycast.pos.Y main_score run data get storage midwut:main temp.Pos[1] 1000
execute store result score #raycast.pos.Z main_score run data get storage midwut:main temp.Pos[2] 1000
scoreboard players add #raycast.pos.Y main_score 16200

scoreboard players operation #Vec.X main_score *= #scale main_score
scoreboard players operation #Vec.Y main_score *= #scale main_score
scoreboard players operation #Vec.Z main_score *= #scale main_score
scoreboard players operation #Vec.X main_score /= #1000 const
scoreboard players operation #Vec.Y main_score /= #1000 const
scoreboard players operation #Vec.Z main_score /= #1000 const


scoreboard players set #test main_score 2147483647
execute as @e[tag=block] run function midwut:test/beam-plane/loop0

execute unless score #test main_score matches 2147483647 run data modify storage midwut:main temp2 set from entity @e[tag=block,limit=1,predicate=midwut:check/score_ranges/midwut.test_range_num]
execute unless score #test main_score matches 2147483647 run function midwut:test/beam-plane/loop1

