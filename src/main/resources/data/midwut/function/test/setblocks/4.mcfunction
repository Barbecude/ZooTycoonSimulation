
execute as @a[tag=midwut.setblocks] at @s positioned ~ ~-1 ~ run function midwut:test/setblocks/2
execute if score #setblocks main_score matches 1.. run schedule function midwut:test/setblocks/1 1t