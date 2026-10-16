scoreboard players set #midwut.temp main_score 0
execute if score #IN:arctan2.X main_score matches 0 if score #IN:arctan2.Y main_score matches 0 run function midwut:y_functions/wasd/zeroing_output
execute if score #midwut.temp main_score matches 0 run function midwut:y_functions/wasd/calc_range
