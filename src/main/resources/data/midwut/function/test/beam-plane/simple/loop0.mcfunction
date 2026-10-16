data modify storage midwut:main temp2 set from entity @s


execute store result score #min.X main_score run data get storage midwut:main temp2.Pos[0] 1000
execute store result score #min.Y main_score run data get storage midwut:main temp2.Pos[1] 1000
execute store result score #min.Z main_score run data get storage midwut:main temp2.Pos[2] 1000

execute store result score #size.X main_score run data get storage midwut:main temp2.transformation.scale[0] 1000
execute store result score #size.Y main_score run data get storage midwut:main temp2.transformation.scale[1] 1000
execute store result score #size.Z main_score run data get storage midwut:main temp2.transformation.scale[2] 1000


scoreboard players operation #max.X main_score = #min.X main_score
scoreboard players operation #max.Y main_score = #min.Y main_score
scoreboard players operation #max.Z main_score = #min.Z main_score
scoreboard players operation #max.X main_score += #size.X main_score
scoreboard players operation #max.Y main_score += #size.Y main_score
scoreboard players operation #max.Z main_score += #size.Z main_score


scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #Vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #Vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #Vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #Vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #Vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #Vec.Z main_score


scoreboard players operation #t_min main_score = #min.X main_score
scoreboard players operation #t_min main_score < #max.X main_score
scoreboard players operation #2 main_score = #min.Y main_score
scoreboard players operation #2 main_score < #max.Y main_score
scoreboard players operation #3 main_score = #min.Z main_score
scoreboard players operation #3 main_score < #max.Z main_score
scoreboard players operation #t_min main_score > #2 main_score
scoreboard players operation #t_min main_score > #3 main_score

scoreboard players operation #t_max main_score = #min.X main_score
scoreboard players operation #t_max main_score > #max.X main_score
scoreboard players operation #2 main_score = #min.Y main_score
scoreboard players operation #2 main_score > #max.Y main_score
scoreboard players operation #3 main_score = #min.Z main_score
scoreboard players operation #3 main_score > #max.Z main_score
scoreboard players operation #t_max main_score < #2 main_score
scoreboard players operation #t_max main_score < #3 main_score


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation @s midwut.test_range = #t_min main_score

scoreboard players operation #test main_score < #t_min main_score



