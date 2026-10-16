scoreboard objectives add main_score dummy
scoreboard objectives add timer dummy
scoreboard objectives add timer2 dummy

kill @e[tag=test_anim]
summon minecraft:item_display ~ ~ ~ {item:{id:"minecraft:potion",count:1,tag:{CustomPotionColor:2}},Tags:["model","test_anim"],teleport_duration:1}
summon pillager ~ ~ ~ {Tags:["body","test_anim"],active_effects:[{id:"invisibility",duration:-1,amplifier:-1b,show_particles:0b}],Silent:1b}