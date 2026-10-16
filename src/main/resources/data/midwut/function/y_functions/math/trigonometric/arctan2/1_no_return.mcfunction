scoreboard players operation #OUT:arctan2 main_score *= #1000 const
execute store result storage midwut:main trigonometric.in int 10 run scoreboard players operation #OUT:arctan2 main_score /= #midwut.temp main_score
function midwut:y_functions/math/trigonometric/arctan2/1_pre with storage midwut:main trigonometric
execute if score #IN:arctan2.X main_score matches 1.. if score #IN:arctan2.Y main_score matches 1.. run scoreboard players operation #OUT:arctan2 main_score *= #-1 const
execute if score #IN:arctan2.X main_score matches ..-1 if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #OUT:arctan2 main_score *= #-1 const
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players add #OUT:arctan2 main_score 180000
execute if score #OUT:arctan2 main_score matches ..-1 run scoreboard players add #OUT:arctan2 main_score 360000