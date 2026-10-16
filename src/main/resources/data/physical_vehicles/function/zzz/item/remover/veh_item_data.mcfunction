data modify entity @s Item.components merge from storage midwut:main data.components
data remove storage midwut:main data.components."minecraft:entity_data"
data remove storage midwut:main data.components."minecraft:custom_name"
data modify entity @s Item.components."minecraft:entity_data".data.phys_veh set from storage midwut:main data.components
tag @s remove get_id