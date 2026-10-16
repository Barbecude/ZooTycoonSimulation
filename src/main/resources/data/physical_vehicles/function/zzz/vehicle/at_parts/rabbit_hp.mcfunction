    #======================#
    #  made by Antohs256M  #
    #======================#
#
execute store result score #temp main_score run data get entity @s Health 10
data modify entity @s Health set value 1024.0f
scoreboard players remove #temp main_score 10240
scoreboard players operation #main.hp main_score += #temp main_score

