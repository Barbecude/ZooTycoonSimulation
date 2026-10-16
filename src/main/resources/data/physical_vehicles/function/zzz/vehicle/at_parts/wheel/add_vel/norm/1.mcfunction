
scoreboard players operation #IN:arctan2.X main_score = #norm.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #norm.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #norm.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #norm.x main_score *= #1000 const
scoreboard players operation #norm.y main_score *= #1000 const
scoreboard players operation #norm.z main_score *= #1000 const
scoreboard players operation #norm.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #norm.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #norm.z main_score /= #IN:arctan2.X main_score
