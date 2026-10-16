scoreboard players set #version main_score 100
scoreboard players operation #test main_score = #version main_score
scoreboard players operation #test main_score %= #100 const
scoreboard players operation #version main_score = #version main_score
scoreboard players operation #version main_score /= #100 const

execute as @a run function physical_vehicles:tellraw/main

execute if score #phys_veh.commandBlockOutput main_score matches 1 run gamerule minecraft:command_block_output true