scoreboard objectives add custom_effect.nausea dummy
scoreboard objectives add camera_roll dummy
$effect give @s minecraft:nausea $(s) 10 true
$scoreboard players set @s custom_effect.nausea $(s)
scoreboard players operation @s custom_effect.nausea *= #20 const
tag @s add custom_effect.nausea
tag @s add custom_effect.nausea.set