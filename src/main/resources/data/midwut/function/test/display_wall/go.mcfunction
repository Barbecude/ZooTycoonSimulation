# function midwut:test/display_wall/go
#$data modify storage midwut:main data set value {x:$(x),y:$(y),z:$(z),x2:$(x2),y2:$(y2),z2:$(z2)}
#kill @e[type=minecraft:block_display,tag=midwut.display_wall.0]
execute align xyz run summon text_display ~1 ~ ~-0.5 {Tags:["midwut.display_wall.0"],Passengers:[{id:"minecraft:shulker",Tags:["midwut.display_wall.0"],Silent:1b,Invulnerable:1b,NoAI:1b,Peek:100b,attributes:[{id:"minecraft:scale",base:0.125d}],active_effects:[{id:"minecraft:invisibility",amplifier:0b,duration:-1,show_particles:false}],Passengers:[{id:"interaction",width:0f,height:0.625f,Tags:["midwut.display_wall.0"],Passengers:[{id:"minecraft:shulker",Tags:["midwut.display_wall.0"],Silent:1b,Invulnerable:1b,NoAI:1b,Peek:100b,attributes:[{id:"minecraft:scale",base:0.125d}],active_effects:[{id:"minecraft:invisibility",amplifier:0b,duration:-1,show_particles:false}]}]}]}],alignment:"center"}
execute align xyz run summon minecraft:block_display ~0.4375 ~ ~ {Tags:["midwut.display_wall.0"],block_state:{Name:"minecraft:iron_bars",Properties:{north:"true",south:"true"}},transformation:{\
translation:[0.7176724137931038f,0.0001f,-1.3879310344827587f],\
left_rotation:[0.0f,-0.3713906763541037f,0.0f,0.9284766908852593f],\
scale:[1f,0.9998f,1.5f],\
right_rotation:[0f,0f,0f,1f]}}

# 45 - arctan(2/16)
#data modify entity @n[type=minecraft:block_display,tag=midwut.display_wall.0] tr