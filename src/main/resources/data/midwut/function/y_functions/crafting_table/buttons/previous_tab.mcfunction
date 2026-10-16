clear @s *[minecraft:custom_data~{midwut.previous_tab:true}]
data modify storage midwut:main midwut_crafting_table.tab set from storage midwut:main midwut_crafting_table.tabs[-1]
data remove storage midwut:main midwut_crafting_table.tabs[-1]
playsound minecraft:ui.button.click ui @s ~ ~ ~ 0.5 1