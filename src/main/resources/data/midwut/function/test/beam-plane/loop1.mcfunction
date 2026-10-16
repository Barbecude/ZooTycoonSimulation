## #vec.X main_score
## #vec.Y main_score
## #vec.Z main_score
## scoreboard players set #pos.X main_score
## scoreboard players set #pos.Y main_score
## scoreboard players set #pos.Z main_score


#scoreboard players operation #vec.X main_score *= #5 const
#scoreboard players operation #vec.Y main_score *= #5 const
#scoreboard players operation #vec.Z main_score *= #5 const


scoreboard players set #north main_score 2147483647
scoreboard players set #south main_score 2147483647
scoreboard players set #east main_score 2147483647
scoreboard players set #west main_score 2147483647
scoreboard players set #up main_score 2147483647
scoreboard players set #down main_score 2147483647


#### north

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players add #max.X main_score 1000
scoreboard players add #max.Y main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #north main_score = #min main_score





#### west

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players add #max.Y main_score 1000
scoreboard players add #max.Z main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #west main_score = #min main_score





#### down

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players add #max.X main_score 1000
scoreboard players add #max.Z main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #down main_score = #min main_score





scoreboard players add #pos.X main_score 1000
scoreboard players add #pos.Y main_score 1000
scoreboard players add #pos.Z main_score 1000

#### east

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players remove #min.Y main_score 1000
scoreboard players remove #min.Z main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #east main_score = #min main_score





#### south

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players remove #min.X main_score 1000
scoreboard players remove #min.Y main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #south main_score = #min main_score





#### up

scoreboard players operation #min.X main_score = #pos.X main_score
scoreboard players operation #min.Y main_score = #pos.Y main_score
scoreboard players operation #min.Z main_score = #pos.Z main_score
scoreboard players operation #max.X main_score = #pos.X main_score
scoreboard players operation #max.Y main_score = #pos.Y main_score
scoreboard players operation #max.Z main_score = #pos.Z main_score

scoreboard players remove #min.X main_score 1000
scoreboard players remove #min.Z main_score 1000

scoreboard players operation #min.X main_score -= #raycast.pos.X main_score
scoreboard players operation #min.X main_score *= #1000 const
scoreboard players operation #min.X main_score /= #vec.X main_score
scoreboard players operation #max.X main_score -= #raycast.pos.X main_score
scoreboard players operation #max.X main_score *= #1000 const
scoreboard players operation #max.X main_score /= #vec.X main_score

scoreboard players operation #min.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #min.Y main_score *= #1000 const
scoreboard players operation #min.Y main_score /= #vec.Y main_score
scoreboard players operation #max.Y main_score -= #raycast.pos.Y main_score
scoreboard players operation #max.Y main_score *= #1000 const
scoreboard players operation #max.Y main_score /= #vec.Y main_score

scoreboard players operation #min.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #min.Z main_score *= #1000 const
scoreboard players operation #min.Z main_score /= #vec.Z main_score
scoreboard players operation #max.Z main_score -= #raycast.pos.Z main_score
scoreboard players operation #max.Z main_score *= #1000 const
scoreboard players operation #max.Z main_score /= #vec.Z main_score


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


execute if score #min main_score matches ..1000 if score #max main_score matches 0.. if score #min main_score <= #max main_score run scoreboard players operation #up main_score = #min main_score


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

