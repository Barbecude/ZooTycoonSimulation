scoreboard players set #ang_g main_score 0
scoreboard players operation #IN:arctan2.Y main_score = #t2 main_score
scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
scoreboard players operation #IN:arctan2.X main_score = #t0 main_score
execute store result score #ang_b main_score run function midwut:help/math/trig_func/arc/tan2
#scoreboard players operation #ang_b main_score *= #-1 const
