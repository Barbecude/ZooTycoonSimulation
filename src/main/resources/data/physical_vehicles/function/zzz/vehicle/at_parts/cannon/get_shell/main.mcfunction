scoreboard players operation #object_id main_score = @s phys_veh.id.storage
data modify storage midwut:main Items set value []
data modify storage midwut:main Items append from entity @e[type=#physical_vehicles:storage,limit=1,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_id] Items[{components:{"minecraft:custom_data":{phys_veh:{type:"shell"}}}}]
data remove storage midwut:main item
execute if data storage midwut:main Items[0] run function physical_vehicles:zzz/vehicle/at_parts/cannon/get_shell/loop

return run execute if data storage midwut:main item
