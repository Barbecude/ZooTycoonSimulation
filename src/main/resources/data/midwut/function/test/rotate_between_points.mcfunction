function midwut:help/vector/xyz/entity
scoreboard players operation #Ending.X main_score = #Vec.X main_score
scoreboard players operation #Ending.Y main_score = #Vec.Y main_score
scoreboard players operation #Ending.Z main_score = #Vec.Z main_score

scoreboard players operation #IN:arctan2.X main_score = #Ending.Z main_score
scoreboard players operation #IN:arctan2.Y main_score = #Ending.X main_score
execute store result storage midwut:main trigonometric.in int 1 store result score #Rotation.Y main_score run function midwut:help/math/trig_func/arc/tan2
#execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
#execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
#execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
#execute store result storage midwut:main trigonometric.in int 1 run function midwut:help/math/trig_func/arc/tan2

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #Ending.Y main_score
function midwut:help/math/trig_func/arc/tan2
scoreboard players operation #Rotation.X main_score = #OUT:arctan2 main_score

tellraw @a [{"score":{"name":"#Rotation.Y","objective":"main_score"}}]
tellraw @a [{"score":{"name":"#Rotation.X","objective":"main_score"}}]