execute as @e[type=frog,tag=midwut.frog_mc_life,limit=3] at @s run tp @s ~ ~0.075 ~
#execute as @e[type=frog,tag=midwut.frog_mc_life,limit=3] at @s run data modify entity @s Motion set value [0.0345d,0d,0d]
#execute at @e[type=frog,tag=midwut.frog_mc_life,tag=main,limit=1] rotated ~ 0 run tp @a[tag=ft] ^ ^-1 ^1.25 ~-180 ~

execute if entity @e[type=frog,tag=midwut.frog_mc_life,limit=1] run schedule function midwut:test/frogs/tick 5s