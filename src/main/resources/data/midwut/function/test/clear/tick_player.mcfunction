data modify storage midwut:main temp set from entity @s Inventory
data remove storage midwut:main temp[{Slot:-106b}]
execute store result score #test main_score if data storage midwut:main temp[{id:"minecraft:stick"}]
execute store result score #test2 main_score if data storage midwut:main temp[{id:"minecraft:stick",tag:{registered:1}}]
execute unless score #test main_score = #test2 main_score run function midwut:test/clear_ign_offhand/change
