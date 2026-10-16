execute store result score #temp main_score run data get storage midwut:main custom_block[0].Slot
execute if score #temp main_score matches 0 run return run execute positioned ~-0.01 ~-0.01 ~-0.5 if entity @s[dx=0.02,dy=0.02]
execute if score #temp main_score matches 1 run return run execute positioned ~0.5 ~-0.01 ~-0.01 if entity @s[dy=0.02,dz=0.02]
execute if score #temp main_score matches 2 run return run execute positioned ~-0.01 ~-0.01 ~0.5 if entity @s[dx=0.02,dy=0.02]
return run execute positioned ~-0.5 ~-0.01 ~-0.01 if entity @s[dy=0.02,dz=0.02]

