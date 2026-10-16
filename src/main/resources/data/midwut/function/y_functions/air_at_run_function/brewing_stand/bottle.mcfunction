playsound minecraft:block.glass.break block @a[distance=..16] ~ ~ ~ 0.75

data modify storage midwut:main Data set value {Item:{},Owner:[I;0,1,2,3],Motion:[0d,-10d,0d]}
data modify storage midwut:main Data.Owner set from entity @a[limit=1,tag=midwut.air_at_run_function.brewing_stand.break_slots] UUID
$data modify storage midwut:main Data.Item set from block ~ ~ ~ Items[{Slot:$(slot)b}]
$item replace block ~ ~ ~ container.$(slot) with air

execute if data storage midwut:main Data.Item{id:"minecraft:splash_potion"} summon minecraft:splash_potion run return run data modify entity @s {} merge from storage midwut:main Data
execute if data storage midwut:main Data.Item{id:"minecraft:lingering_potion"} summon minecraft:lingering_potion run return run data modify entity @s {} merge from storage midwut:main Data
execute unless data storage midwut:main Data.Item{id:"minecraft:potion"} run function midwut:y_functions/air_at_run_function/brewing_stand/another_item
