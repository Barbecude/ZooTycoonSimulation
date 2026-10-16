tag @s add midwut.setblocks
#scoreboard players operation @s main_score *= #2 const
#scoreboard players add @s main_score 1
execute as @a[tag=midwut.setblocks] at @s positioned ~ ~-1 ~ run function midwut:test/setblocks/2
schedule function midwut:test/setblocks/1 1t


