gamerule maxCommandChainLength 2147483647


# scores
scoreboard objectives add main_score dummy
scoreboard objectives add const dummy
scoreboard objectives add midwut.damage dummy
scoreboard objectives add midwut.return_hp dummy
scoreboard objectives add main_score dummy
scoreboard objectives add phys_thread.num dummy
scoreboard objectives add phys_thread.end_num dummy

scoreboard objectives add midwut.gamemode dummy
scoreboard objectives add midwut.player_id dummy
scoreboard objectives add midwut.test_range dummy
scoreboard objectives add midwut.mined.iron_trapdoor mined:iron_trapdoor
scoreboard objectives add midwut.mined.structure_void mined:structure_void
scoreboard objectives add midwut.used.dolphin_spawn_egg used:dolphin_spawn_egg
scoreboard objectives add midwut.dropped.poisonous_potato dropped:poisonous_potato
scoreboard objectives add midwut.crafting_table.id dummy
scoreboard objectives add midwut.crafting_table.page dummy
scoreboard objectives add midwut.crafting_table.previous_page dummy
scoreboard objectives add midwut.health health

scoreboard objectives add midwut.uuid.0 dummy
scoreboard objectives add midwut.uuid.1 dummy
scoreboard objectives add midwut.uuid.2 dummy
scoreboard objectives add midwut.uuid.3 dummy
scoreboard objectives add midwut.motion.x dummy
scoreboard objectives add midwut.motion.y dummy
scoreboard objectives add midwut.motion.z dummy

# teams
team add no_coll
team modify no_coll collisionRule never
team modify no_coll seeFriendlyInvisibles false

team add friendly
team modify friendly friendlyFire false
team modify friendly seeFriendlyInvisibles false

# storages
execute unless score #pi_8 const = #pi_8 const run function midwut:y_functions/add/constants/all

execute unless data storage midwut:main macros.temp run data modify storage midwut:main macros set value {temp:0}


# versions
#function midwut:y_functions/get_pack_format/get
#execute if score #mc_pack_format main_score matches 11.. run schedule function midwut:y_functions/load/schedule 1t
schedule function midwut:y_functions/load/schedule 1t

# schedules
schedule function midwut:tick 1t
