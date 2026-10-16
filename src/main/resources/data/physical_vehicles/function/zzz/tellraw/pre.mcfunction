
    #======================#
    #  made by Antohs256M  #
    #======================#
#
execute store result score #phys_veh.commandBlockOutput main_score run gamerule minecraft:command_block_output
execute if score #phys_veh.commandBlockOutput main_score matches 1 run gamerule minecraft:command_block_output false
execute in midwut:main run setblock -28 65 28 command_block{Command:"execute store result score #phys_veh.custom_tellraws main_score run function #physical_vehicles:tellraw",auto:1b,TrackOutput:false} destroy

schedule function physical_vehicles:tellraw/2t 2t



