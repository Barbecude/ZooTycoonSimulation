# v = (angle + 180) * 0xffff / 360

# p0 = v % 256 / 255
# p1 = v // 256 / 255

# print(f"execute at @a run particle minecraft:entity_effect{{color:[0.004, 0, {p0}, 1]}} ~ ~ ~ 0 0 0 0 10 force")
# print(f"execute at @a run particle minecraft:entity_effect{{color:[0.004, 0.004, {p1}, 1]}} ~ ~ ~ 0 0 0 0 10 force")
scoreboard players operation #angle main_score /= #36 const
scoreboard players operation #angle main_score *= #65535 const
scoreboard players operation #angle main_score /= #10000 const
scoreboard players operation #c1 main_score = #angle main_score
execute store result storage midwut:main macors.c0 double 0.003921569 run scoreboard players operation #angle main_score /= #256 const
execute store result storage midwut:main macors.c1 double 0.003921569 run scoreboard players operation #c1 main_score %= #256 const
function midwut:test/camera_roll/apply with storage midwut:main macors