#> this
execute if block ~ ~ ~ minecraft:soul_campfire[facing=north,lit=false] align xyz run return run function midwut:y_functions/air_at_run_function/custom_block_instead_soul_campfire
execute if block ~ ~ ~ minecraft:soul_campfire align xyz run return run execute positioned ~-0.01 ~0.4374 ~-0.01 if entity @s[dx=0.02,dz=0.02]
execute if block ~ ~ ~ #minecraft:slabs run return run execute unless block ~ ~ ~ #minecraft:slabs[type=double] align xyz run function midwut:y_functions/air_at_run_function/slabs
execute if block ~ ~ ~ minecraft:snow align xyz run return run function midwut:y_functions/air_at_run_function/snow/main
execute if block ~ ~ ~ #minecraft:stairs align xyz run return run function midwut:y_functions/air_at_run_function/stairs/main
execute if block ~ ~ ~ #minecraft:trapdoors align xyz run return run function midwut:y_functions/air_at_run_function/trapdoors/main
execute if block ~ ~ ~ minecraft:grindstone align xyz run return run function midwut:y_functions/air_at_run_function/grindstone/main
execute if block ~ ~ ~ #minecraft:anvil align xyz run return run function midwut:y_functions/air_at_run_function/anvils/main
execute if block ~ ~ ~ #minecraft:cauldrons align xyz run return run function midwut:y_functions/air_at_run_function/cauldrons/main
execute if block ~ ~ ~ minecraft:bell align xyz run return run function midwut:y_functions/air_at_run_function/bell/main
execute if block ~ ~ ~ #minecraft:lightning_rods align xyz run return run function midwut:y_functions/air_at_run_function/lightning_rod/main
execute if block ~ ~ ~ minecraft:decorated_pot align xyz run return run function midwut:y_functions/air_at_run_function/decorated_pot/main
execute if block ~ ~ ~ #minecraft:lanterns align xyz run return run function midwut:y_functions/air_at_run_function/lanterns/main
execute if block ~ ~ ~ #minecraft:chains align xyz run return run function midwut:y_functions/air_at_run_function/chain
execute if block ~ ~ ~ minecraft:lectern align xyz run return run function midwut:y_functions/air_at_run_function/lectern/main
execute if block ~ ~ ~ #midwut:chests align xyz run return run function midwut:y_functions/air_at_run_function/chests/main
execute if block ~ ~ ~ #minecraft:beds align xyz run return run function midwut:y_functions/air_at_run_function/beds/main
execute if block ~ ~ ~ #midwut:heads/all align xyz run return run function midwut:y_functions/air_at_run_function/heads/main
execute if block ~ ~ ~ #minecraft:bars align xyz run return run function midwut:y_functions/air_at_run_function/iron_bars
execute if block ~ ~ ~ #minecraft:walls align xyz run return run function midwut:y_functions/air_at_run_function/walls
execute if block ~ ~ ~ #minecraft:fences align xyz run return run function midwut:y_functions/air_at_run_function/fences/main
execute if block ~ ~ ~ #minecraft:fence_gates align xyz run return run function midwut:y_functions/air_at_run_function/fence_gates/main
execute if block ~ ~ ~ #midwut:glass/all_panes align xyz run return run function midwut:y_functions/air_at_run_function/glass_panes/main
execute if block ~ ~ ~ minecraft:hopper align xyz run return run function midwut:y_functions/air_at_run_function/hopper/main
execute if block ~ ~ ~ minecraft:ladder align xyz run return run function midwut:y_functions/air_at_run_function/ladder
execute if block ~ ~ ~ #minecraft:doors align xyz run return run function midwut:y_functions/air_at_run_function/doors/main
execute if block ~ ~ ~ minecraft:big_dripleaf align xyz run return run function midwut:y_functions/air_at_run_function/big_dripleaf
execute if block ~ ~ ~ minecraft:brewing_stand align xyz run return run function midwut:y_functions/air_at_run_function/brewing_stand/main
execute if block ~ ~ ~ minecraft:end_portal_frame align xyz run return run function midwut:y_functions/air_at_run_function/end_portal_frame/main
execute if block ~ ~ ~ #minecraft:candles align xyz run return run function midwut:y_functions/air_at_run_function/candles/main
execute if block ~ ~ ~ #minecraft:wall_hanging_signs align xyz run return run function midwut:y_functions/air_at_run_function/wall_hanging_signs