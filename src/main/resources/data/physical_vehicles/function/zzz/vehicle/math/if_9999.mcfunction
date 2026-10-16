scoreboard players operation #IN:arctan2.Y main_score = #xz main_score
scoreboard players operation #IN:arctan2.X main_score = #t10 main_score
execute store result score #ang_b main_score run function midwut:help/math/trig_func/arc/tan2
#scoreboard players operation #ang_b main_score *= #-1 const
scoreboard players operation #IN:arctan2.Y main_score = #t1 main_score
scoreboard players operation #IN:arctan2.X main_score = #t5 main_score
execute store result score #ang_g main_score run function midwut:help/math/trig_func/arc/tan2
#scoreboard players operation #ang_g main_score *= #-1 const
