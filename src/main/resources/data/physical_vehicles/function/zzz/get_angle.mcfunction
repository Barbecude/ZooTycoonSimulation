rotate @s ~ 0
execute if entity @s[y_rotation=45..135] run return run data modify storage midwut:main side set value "west"
execute if entity @s[y_rotation=135..225] run return run data modify storage midwut:main side set value "north"
execute if entity @s[y_rotation=225..315] run return run data modify storage midwut:main side set value "east"
data modify storage midwut:main side set value "south"