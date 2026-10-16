summon minecraft:item ~ ~1 ~ {Tags:["midwut.spawn"],Item:{id:"minecraft:structure_void",count:1}}
$item replace entity @e[type=minecraft:item,limit=1,tag=midwut.spawn,distance=..1.1] contents from block ~ ~ ~ container.$(slot)
$item replace block ~ ~ ~ container.$(slot) with minecraft:air
tag @e[type=minecraft:item,limit=1,tag=midwut.spawn,distance=..1.1] remove midwut.spawn
