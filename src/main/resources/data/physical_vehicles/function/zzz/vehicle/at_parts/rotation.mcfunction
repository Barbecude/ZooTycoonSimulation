    #=====================#
    #   Made by Antohs256M   #
    #=====================#
#
data modify entity @s transformation.left_rotation set from storage midwut:main Data.data.transformation.main_rotation
execute if entity @s[tag=phys_veh.have_entity_rotation] run rotate @s ~ ~
#data modify entity @s start_interpolation set value 0


