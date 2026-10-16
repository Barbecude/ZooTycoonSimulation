#execute store result entity @s Motion[0] double 0.000175 run scoreboard players get #X main_score
#execute store result entity @s Motion[1] double 0.000175 run scoreboard players get #Y main_score
#execute store result entity @s Motion[2] double 0.000175 run scoreboard players get #Z main_score
#tag @s remove get_id


#multiply the angle you want by 100 --> 90° = 9000

#rotate the entire statue
scoreboard players set @e[tag=imposter,tag=main] imposter_x 9000

#rotate the neck x
scoreboard players set @e[tag=imposter,tag=main] imposter_x_head 9000

#rotate the neck y
scoreboard players set @e[tag=imposter,tag=main] imposter_y_head 4500

#rotate the right arm
scoreboard players set @e[tag=imposter,tag=main] imposter_x_ra 9000
scoreboard players set @e[tag=imposter,tag=main] imposter_y_ra 9000

#rotate the left arm
scoreboard players set @e[tag=imposter,tag=main] imposter_x_la 9000
scoreboard players set @e[tag=imposter,tag=main] imposter_y_la 9000

#rotate the right leg
scoreboard players set @e[tag=imposter,tag=main] imposter_x_rl 9000
scoreboard players set @e[tag=imposter,tag=main] imposter_y_rl 9000

#rotate the left leg
scoreboard players set @e[tag=imposter,tag=main] imposter_x_ll 9000
scoreboard players set @e[tag=imposter,tag=main] imposter_y_ll 9000

#Delete the nearest statue
#function imposter:kill

#Disable live movement from scorebaord values
#This way you can pose them with the Pose value in nbt
tag @e[sort=nearest,tag=main,limit=1] remove live
    