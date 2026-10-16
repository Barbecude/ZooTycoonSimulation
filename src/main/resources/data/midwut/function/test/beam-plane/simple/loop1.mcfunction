execute store result score #pos.X main_score run data get storage midwut:main temp2.Pos[0] 1000
execute store result score #pos.Y main_score run data get storage midwut:main temp2.Pos[1] 1000
execute store result score #pos.Z main_score run data get storage midwut:main temp2.Pos[2] 1000

execute store result score #size.X main_score run data get storage midwut:main temp2.transformation.scale[0] 1000
execute store result score #size.Y main_score run data get storage midwut:main temp2.transformation.scale[1] 1000
execute store result score #size.Z main_score run data get storage midwut:main temp2.transformation.scale[2] 1000

scoreboard players set #north main_score 2147483647
scoreboard players set #south main_score 2147483647
scoreboard players set #east main_score 2147483647
scoreboard players set #west main_score 2147483647
scoreboard players set #up main_score 2147483647
scoreboard players set #down main_score 2147483647


scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players operation #max.X main_score += #size.X main_score
scoreboard players operation #max.Y main_score += #size.Y main_score
#scoreboard players set #min.Z main_score 0

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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #north main_score = #t_min main_score







scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #west main_score = #t_min main_score







scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players operation #max.X main_score += #size.X main_score
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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #down main_score = #t_min main_score





scoreboard players operation #pos.X main_score += #size.X main_score
scoreboard players operation #pos.Y main_score += #size.Y main_score
scoreboard players operation #pos.Z main_score += #size.Z main_score

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

#scoreboard players operation #min.X main_score -= #size.X main_score
scoreboard players operation #min.Y main_score -= #size.Y main_score
scoreboard players operation #min.Z main_score -= #size.Z main_score

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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #east main_score = #t_min main_score







scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players operation #min.X main_score -= #size.X main_score
scoreboard players operation #min.Y main_score -= #size.Y main_score
#scoreboard players operation #min.Z main_score -= #size.Z main_score

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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #south main_score = #t_min main_score







scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players operation #min.X main_score -= #size.X main_score
#scoreboard players operation #min.Y main_score -= #size.Y main_score
scoreboard players operation #min.Z main_score -= #size.Z main_score

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


execute if score #t_min main_score matches ..1000 if score #t_max main_score matches 0.. if score #t_min main_score <= #t_max main_score run scoreboard players operation #up main_score = #t_min main_score


scoreboard players set #side main_score 2147483647
scoreboard players operation #side main_score < #north main_score
scoreboard players operation #side main_score < #south main_score
scoreboard players operation #side main_score < #east main_score
scoreboard players operation #side main_score < #west main_score
scoreboard players operation #side main_score < #up main_score
scoreboard players operation #side main_score < #down main_score
execute unless score #side main_score matches 2147483647 if score #side main_score = #north main_score run say north
execute unless score #side main_score matches 2147483647 if score #side main_score = #south main_score run say south
execute unless score #side main_score matches 2147483647 if score #side main_score = #east main_score run say east
execute unless score #side main_score matches 2147483647 if score #side main_score = #west main_score run say west
execute unless score #side main_score matches 2147483647 if score #side main_score = #up main_score run say up
execute unless score #side main_score matches 2147483647 if score #side main_score = #down main_score run say down

