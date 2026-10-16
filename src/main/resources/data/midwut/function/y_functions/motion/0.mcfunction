scoreboard players operation #IN:arctan2.X main_score = #motion_z main_score
scoreboard players operation #IN:arctan2.Y main_score = #motion_x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #motion_y main_score
scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute store result storage midwut:main macros.angle int 1 run function midwut:help/math/trig_func/arc/tan2
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #Vec.Length main_score = #IN:arctan2.X main_score


scoreboard players operation #Vec.Length main_score /= #8000 const

execute if score #motion_y main_score matches 0 run return run data modify storage midwut:main macros.angle set value 0

execute anchored eyes positioned ^ ^ ^ run function midwut:y_functions/motion/eye_level

scoreboard players operation #IN:arctan2.X main_score %= #8000 const

scoreboard players set #d12 main_score 10000
scoreboard players operation #d12 main_score -= #IN:arctan2.X main_score
scoreboard players operation #d12 main_score *= #12 const

data modify storage midwut:main trigonometric.in set from storage midwut:main macros.angle
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #OUT:cos main_score *= #eye_level main_score
scoreboard players operation #OUT:sin main_score *= #eye_level main_score
scoreboard players operation #OUT:cos main_score *= #100 const
scoreboard players operation #cos main_score = #OUT:cos main_score
scoreboard players operation #sin main_score = #OUT:sin main_score

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #OUT:cos main_score /= #d12 main_score

execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arcsin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #OUT:cos main_score *= #d12 main_score
scoreboard players operation #OUT:cos main_score /= #100 const

execute store result storage midwut:main macros.d double -0.000001 run scoreboard players operation #OUT:cos main_score += #OUT:sin main_score


scoreboard players set #d12 main_score 24000

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #cos main_score /= #d12 main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arcsin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #OUT:cos main_score *= #d12 main_score
scoreboard players operation #OUT:cos main_score /= #100 const

execute store result storage midwut:main macros.full_d double -0.000001 run scoreboard players operation #OUT:cos main_score += #OUT:sin main_score