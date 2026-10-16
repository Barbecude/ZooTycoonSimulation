#function midwut:test/ride_test/0
summon zombie ~ ~ ~ {Tags:["midwut.test.ride.0"]}
summon minecraft:block_display ~ ~ ~ {Tags:["midwut.test.ride.1"],block_state:{Name:"ice"}}
ride @n[tag=midwut.test.ride.1] mount @n[tag=midwut.test.ride.0]
execute as @n[tag=midwut.test.ride.1] at @s run tp @s @s

tellraw @a [{"entity": "@n[tag=midwut.test.ride.0]","nbt": "Pos[1]"}]
tellraw @a [{"entity": "@n[tag=midwut.test.ride.1]","nbt": "Pos[1]"}]
