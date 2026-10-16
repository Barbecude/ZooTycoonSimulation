scoreboard players operation #temp main_score = #DO main_score
scoreboard players operation #temp main_score *= #100 const
scoreboard players operation #temp main_score /= #IN:arctan2.X main_score

scoreboard players operation #fx main_score *= #temp main_score
scoreboard players operation #fx main_score /= #100 const
scoreboard players operation #fy main_score *= #temp main_score
scoreboard players operation #fy main_score /= #100 const
scoreboard players operation #fz main_score *= #temp main_score
scoreboard players operation #fz main_score /= #100 const