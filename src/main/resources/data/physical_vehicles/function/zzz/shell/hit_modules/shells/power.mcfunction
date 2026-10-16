execute store result score #temp main_score run data get storage midwut:main Items[-1].count
execute store result score #temp2 main_score run data get storage midwut:main Items[-1].components."minecraft:custom_data".phys_veh.explosion_power 3
scoreboard players operation #temp main_score *= #temp2 main_score
scoreboard players operation #power main_score += #temp main_score

function physical_vehicles:zzz/shell/hit_modules/shells/container_slot with storage midwut:main Items[-1]
function physical_vehicles:zzz/shell/hit_modules/shells/horse_slot with storage midwut:main Items[-1]
