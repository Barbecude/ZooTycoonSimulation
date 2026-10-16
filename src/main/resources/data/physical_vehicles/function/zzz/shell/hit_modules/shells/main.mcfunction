scoreboard players set #power_full main_score 0
execute store result score #chance main_score run data get storage midwut:main module.chance 1000

data modify storage midwut:main macros.chance set from storage midwut:main module.chance
data modify storage midwut:main macros.explosion_fuse set value 2
execute as @e[type=minecraft:llama,distance=..64,tag=phys_veh.llama_seat,predicate=physical_vehicles:same_scores/vehicle_id] on passengers if items entity @s container.* *[minecraft:custom_data~{phys_veh:{type:"shell"}}] run function physical_vehicles:zzz/shell/hit_modules/shells/player
execute as @e[type=#physical_vehicles:storage,distance=..64,tag=physical_vehicle,predicate=physical_vehicles:same_scores/vehicle_id] run function physical_vehicles:zzz/shell/hit_modules/shells/storage_search
execute store result score #ejection_threshold main_score run data get storage midwut:main module.ejection_threshold 10
execute if score #power_full main_score >= #ejection_threshold main_score run particle minecraft:flash{color:[1f,1f,1f,1f]} ~ ~ ~ 0 0 0 0 1 force
#tellraw @a [{score:{name:"#power_full",objective:"main_score"}}]


