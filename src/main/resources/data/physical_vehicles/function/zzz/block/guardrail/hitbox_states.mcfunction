execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.left":"north"}}}] positioned ~0.40625 ~-0.125 ~-0.499 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.left":"south"}}}] positioned ~-0.40625 ~-0.125 ~0.499 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.left":"east"}}}] positioned ~0.499 ~-0.125 ~0.40625 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.left":"west"}}}] positioned ~-0.499 ~-0.125 ~-0.40625 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.right":"north"}}}] positioned ~-0.40625 ~-0.125 ~-0.499 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.right":"south"}}}] positioned ~0.40625 ~-0.125 ~0.499 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.right":"east"}}}] positioned ~0.499 ~-0.125 ~-0.40625 if entity @s[dx=0] run return 0
execute if data storage midwut:main custom_block[{components:{"minecraft:custom_data":{"phys_veh.guardrail.right":"west"}}}] positioned ~-0.499 ~-0.125 ~0.40625 if entity @s[dx=0] run return 0
return 1