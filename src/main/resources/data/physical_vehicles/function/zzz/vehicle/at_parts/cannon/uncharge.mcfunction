scoreboard players set @s phys_veh.normalize_timer 0
scoreboard players operation #object_id main_score = @s phys_veh.id.storage
data modify storage midwut:main item set from entity @s data.phys_veh.cannon.item
function physical_vehicles:zzz/vehicle/at_parts/cannon/shell_clear
data modify storage midwut:main Items set from entity @e[type=#physical_vehicles:storage,limit=1,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_id] Items
execute in midwut:main run data modify block 0 0 0 Items set from storage midwut:main Items
execute in midwut:main run data modify block 0 1 0 Items[] set from storage midwut:main item
execute in midwut:main run loot insert 0 0 0 mine 0 1 0 minecraft:shears[minecraft:custom_data={drop:true}]
execute in midwut:main run data modify storage midwut:main Items set from block 0 0 0 Items
data modify entity @e[type=#physical_vehicles:storage,limit=1,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_id] Items set from storage midwut:main Items
