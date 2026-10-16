summon minecraft:item ~ ~0.5 ~ {Tags:["midwut.spawn"],Item:{id:"minecraft:structure_void",count:1}}
$data modify entity @e[type=minecraft:item,limit=1,tag=midwut.spawn,distance=..1.1] Item set from block ~ ~ ~ Items[{Slot:$(slot)b}]
$data remove block ~ ~ ~ Items[{Slot:$(slot)b}]
tag @e[type=minecraft:item,limit=1,tag=midwut.spawn,distance=..1.1] remove midwut.spawn
