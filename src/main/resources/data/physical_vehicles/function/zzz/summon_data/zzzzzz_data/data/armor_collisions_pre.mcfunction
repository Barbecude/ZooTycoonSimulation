data modify storage midwut:main colls set value []
data modify storage midwut:main temp set from storage midwut:main Data.data.phys_veh.armor_collisions
function physical_vehicles:zzz/summon_data/zzzzzz_data/data/armor_collisions
data modify entity @s data.phys_veh.armor_collisions set from storage midwut:main colls
