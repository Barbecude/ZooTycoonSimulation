scoreboard players set #power main_score 0
data modify storage midwut:main Items set value []
data modify storage midwut:main Items append from entity @s Items[{components:{"minecraft:custom_data":{phys_veh:{type:"shell"}}}}]
function physical_vehicles:zzz/shell/hit_modules/shells/items_loop
scoreboard players operation #power_full main_score += #power main_score
execute store result storage midwut:main macros.explosion_power float 0.1 run scoreboard players get #power main_score
#tellraw @a {storage:"midwut:main",nbt:"macros.explosion_power"}
execute positioned as @s run function physical_vehicles:zzz/shell/hit/explode_on_impact_block with storage midwut:main macros
