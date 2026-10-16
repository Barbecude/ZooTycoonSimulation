data modify storage midwut:main Inventory set value []
data modify storage midwut:main Inventory append from entity @s Inventory[{id:"minecraft:stick"}]
data remove storage midwut:main Inventory[{id:"minecraft:stick",tag:{registered:1}}]
data remove storage midwut:main Inventory[{Slot:-106b}]
data modify storage midwut:main Inventory[].id set value "minecraft:air"

function midwut:help/inventory_modifier/add_merge


