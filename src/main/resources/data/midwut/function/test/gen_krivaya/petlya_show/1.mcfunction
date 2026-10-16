scoreboard players remove #temp main_score 1

summon marker ~ ~ ~ {Tags:["petlya_show.particle"]}

execute if score #temp main_score matches 1.. rotated ~ ~-0.3 positioned ^ ^ ^0.1 run function midwut:test/petlya_show/1