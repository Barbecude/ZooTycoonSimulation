
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


scoreboard players operation #min main_score = #min.X main_score
scoreboard players operation #min main_score < #max.X main_score
scoreboard players operation #B main_score = #min.Y main_score
scoreboard players operation #B main_score < #max.Y main_score
scoreboard players operation #C main_score = #min.Z main_score
scoreboard players operation #C main_score < #max.Z main_score
scoreboard players operation #min main_score > #B main_score
scoreboard players operation #min main_score > #C main_score

scoreboard players operation #max main_score = #min.X main_score
scoreboard players operation #max main_score > #max.X main_score
scoreboard players operation #B main_score = #min.Y main_score
scoreboard players operation #B main_score > #max.Y main_score
scoreboard players operation #C main_score = #min.Z main_score
scoreboard players operation #C main_score > #max.Z main_score
scoreboard players operation #max main_score < #B main_score
scoreboard players operation #max main_score < #C main_score


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation @s midwut.test_range = #min main_score

scoreboard players operation #test main_score < #min main_score



