tag @s remove midwut.motion.launch
execute if entity @s[scores={midwut.motion.x=0,midwut.motion.y=0,midwut.motion.z=0}] run return run function midwut:y_functions/motion/reset
execute if entity @s[tag=midwut.motion.ignore_motion] run function midwut:y_functions/motion/ignore_motion

execute store result storage midwut:main macros.motion_x double 0.0001 run scoreboard players operation #motion_x main_score = @s midwut.motion.x
execute store result storage midwut:main macros.motion_y double 0.0001 run scoreboard players operation #motion_y main_score = @s midwut.motion.y
execute store result storage midwut:main macros.motion_z double 0.0001 run scoreboard players operation #motion_z main_score = @s midwut.motion.z

function midwut:y_functions/motion/0
attribute @s minecraft:explosion_knockback_resistance modifier add midwut:disable_explosion_knockback_resistance -1 add_multiplied_total
function midwut:y_functions/motion/gm_get
gamemode creative
tp ~ ~10000 ~
execute rotated as @s positioned ~ ~10000 ~ run function midwut:y_functions/motion/summon with storage midwut:main macros
tp ~ ~ ~
function midwut:y_functions/motion/gm_set
attribute @s minecraft:explosion_knockback_resistance modifier remove midwut:disable_explosion_knockback_resistance

function midwut:y_functions/motion/reset