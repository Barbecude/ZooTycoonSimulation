scoreboard players set @s phys_veh.normalize_timer 0
execute unless function physical_vehicles:zzz/vehicle/at_parts/cannon/get_shell/main run return fail

scoreboard players operation #object_id main_score = @s phys_veh.id.storage
data modify entity @s data.phys_veh.cannon.item set from storage midwut:main item
function physical_vehicles:zzz/vehicle/at_parts/cannon/remove with storage midwut:main item

tag @s add phys_veh.shell_cherged

execute as @e[type=#physical_vehicles:seat,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_id,limit=1,x=0] on passengers if entity @s[type=minecraft:player] run title @s actionbar ""