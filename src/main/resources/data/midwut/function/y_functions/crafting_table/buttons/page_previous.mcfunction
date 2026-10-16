clear @s *[minecraft:custom_data~{midwut.previous_page:true}]
execute if data storage midwut:main midwut_crafting_table{page:0} run data modify storage midwut:main midwut_crafting_table.page set value -1
execute store result storage midwut:main midwut_crafting_table.page int 1 run data get storage midwut:main midwut_crafting_table.page 0.9999999999
playsound minecraft:ui.button.click ui @s ~ ~ ~ 0.5 1