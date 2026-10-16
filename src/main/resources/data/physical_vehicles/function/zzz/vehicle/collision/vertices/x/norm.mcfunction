execute if score #IN:arctan2.X main_score matches ..499 run scoreboard players set #IN:arctan2.X main_score 500
scoreboard players operation #t_direction.y main_score = #vp.y main_score
scoreboard players operation #t_direction.z main_score = #vp.z main_score
scoreboard players operation #t_direction.y main_score *= #1000 const
scoreboard players operation #t_direction.z main_score *= #1000 const
scoreboard players operation #t_direction.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #t_direction.z main_score /= #IN:arctan2.X main_score
#tellraw @a [{score:{name:"#t_direction.y",objective:"main_score"}},"  ",{score:{name:"#t_direction.z",objective:"main_score"}}]