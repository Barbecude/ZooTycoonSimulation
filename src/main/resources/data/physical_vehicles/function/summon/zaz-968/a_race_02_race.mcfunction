# Main
summon minecraft:text_display ~ ~1.0 ~ {data:{phys_veh:{rolling_in_air:23d,mass:800,gravity:[0d,-11.03d,0d],bounding_box:[0.7d, 0.7d, 1.8d],mass_center:[0.0d,-0.625d,-0.062d],steering_angle:[35d,35d,2.6f,22.5d,12.8d],base_radius:0.328d,aligning_torque_divider:10,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.1",duration:4s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:vehicle.zaz-968m.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:vehicle.zaz-968m.ignition_starting",duration:4s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:vehicle.zaz-968m.engine",duration:2s,base_volume:5.0f,base_rpm:6500,multiplier:150},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_removed",volume:1f,pitch:1f}\
],\
gears:[-3.9d, 0.0d, 4.5d, 2.8d, 1.9d, 1.0d],\
engine:{friction: [2.0d, 0.03d, 0.0002d], idle_rpm: 800, min_rpm: 350, redline_rpm: 7200, torque_curve_coeff: 3, max_torque: 140, max_torque_rpm: 4500, moment_of_inertia_reversed: 14d, rpm_scaling_factor: 100},\
transmission:{max_torque: 220, stiffness: 65, final_gear: 3.9d, gearbox_efficiency: 0.93d},\
collisions:[\
[0.875d, -0.562d, 2.0d],\
[0.0d, -0.562d, 2.0d],\     
[-0.875d, -0.562d, 2.0d],\  
[0.875d, -0.062d, 1.875d],\ 
[0.0d, -0.062d, 1.875d],\   
[-0.875d, -0.062d, 1.875d],\
[0.875d, -0.062d, 0.875d],\ 
[0.0d, -0.062d, 0.875d],\   
[-0.875d, -0.062d, 0.875d],\
[0.875d, 0.438d, 0.375d],\  
[0.0d, 0.438d, 0.188d],\    
[-0.875d, 0.438d, 0.375d],\ 
[0.875d, 0.438d, -0.375d],\ 
[-0.875d, 0.438d, -0.375d],\
[0.875d, 0.438d, -1.062d],\ 
[0.0d, 0.438d, -0.75d],\    
[-0.875d, 0.438d, -1.062d],\
[0.875d, -0.062d, -1.312d],\
[0.0d, -0.062d, -1.312d],\
[-0.875d, -0.062d, -1.312d],\
[0.875d, -0.062d, -2.188d],\
[0.0d, -0.062d, -2.188d],\
[-0.875d, -0.062d, -2.188d],\
[0.875d, -0.562d, -2.25d],\
[0.0d, -0.562d, -2.25d],\
[-0.875d, -0.562d, -2.25d],\
[0.875d, -0.062d, 0.0d],\
[0.875d, -0.062d, -0.75d],\
[0.812d, -0.75d, 0.406d],\
[-0.812d, -0.75d, 0.406d],\
[0.812d, -0.75d, -0.469d],\
[0.0d, -0.75d, -0.469d],\
[0.0d, -0.75d, 0.406d],\
[-0.812d, -0.75d, -0.469d],\
[0.0d, -0.688d, -1.883d],\
[0.812d, -0.781d, 1.219d],\
[0.0d, -0.75d, 1.219d],\
[-0.812d, -0.781d, 1.219d],\
[0.812d, -0.781d, -1.219d],\
[0.0d, -0.75d, -1.219d],\
[-0.812d, -0.781d, -1.219d],\
],collision_box:[-0.875d, -0.875d, -2.188d, 0.875d, 0.438d, 1.938d],coeff_e:0.07d,friction_reduction_ratio:20.0d,\
armor_collisions:[{box:[-0.875d, -0.875d, -2.188d, 0.875d, 0.438d, 1.938d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles","phys_veh.debug._back_last_location"],teleport_duration:2,interpolation_duration:2}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,-0.625d,-1.625d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.zaz-968a","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.zaz-968a","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.45d,-0.75d,0.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zaz-968a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.45d,-0.75d,0.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zaz-968a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.45d,-0.75d,-0.6d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zaz-968a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.45d,-0.75d,-0.6d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zaz-968a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,0.125d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10040}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.model","phys_veh.have_tint"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[2.0f,2.0f,2.0f]},teleport_duration:1,interpolation_duration:1,Passengers:[\
{id:"minecraft:text_display",data:{phys_veh:{offset:[0.883d,-0.672d,0.203d]}},background:0,Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.ride_temp","phys_veh.have_text","phys_veh.transaltion_offset","phys_veh.disable.offset"],teleport_duration:1,interpolation_duration:1,transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.5f,1.5f,1.5f],right_rotation:{axis:[0f,1f,0f],angle:1.5708f}}},\
{id:"minecraft:text_display",data:{phys_veh:{offset:[-0.883d,-0.672d,0.172d]}},background:0,Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.ride_temp","phys_veh.have_text","phys_veh.transaltion_offset","phys_veh.disable.offset"],teleport_duration:1,interpolation_duration:1,transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.5f,1.5f,1.5f],right_rotation:{axis:[0f,1f,0f],angle:-1.5708f}}}\
]}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.438d,-0.187d,0.526d]}},transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[2.0f,2.0f,2.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10041}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.steering_wheel","phys_veh.have_tint"]}

data modify storage midwut:main text set value {"text":0,"color":"#b6ada1","bold":true}
execute store result storage midwut:main text.text int 1 if entity @e[type=minecraft:text_display,tag=phys_veh.main,x=0]
data modify storage midwut:main text.text set string storage midwut:main text.text
execute as @e[type=minecraft:text_display,tag=phys_veh.have_text,tag=get_id,limit=2,x=0] run data modify entity @s text set from storage midwut:main text

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:11,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1.1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:200,inertia_radius:0.296d,radius:0.328d,tread_height:0.094d,suspension:{strength:2200,damping:2800,height:0.188d,max_delta:0.1d},offset:[0.718d,-0.687d,1.219d],longitudinal:{stiffness:1.9d,shape_factor:1.6d,curvature:0.1d,peak_factor:1.1d},lateral:{stiffness:1.6d,shape_factor:1.55d,curvature:-0.1d,peak_factor:1.05d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.3d,peak_factor:0.32d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10042}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.have_tint","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:11,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1.1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:200,inertia_radius:0.296d,radius:0.328d,tread_height:0.094d,suspension:{strength:2200,damping:2800,height:0.188d,max_delta:0.1d},offset:[-0.718d,-0.687d,1.219d],longitudinal:{stiffness:1.9d,shape_factor:1.6d,curvature:0.1d,peak_factor:1.1d},lateral:{stiffness:1.6d,shape_factor:1.55d,curvature:-0.1d,peak_factor:1.05d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.3d,peak_factor:0.32d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10042}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.have_tint","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:11,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1.1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:100,hand_brake_torque:300,inertia_radius:0.296d,radius:0.328d,tread_height:0.094d,suspension:{strength:2200,damping:2800,height:0.188d,max_delta:0.1d},offset:[-0.718d,-0.687d,-1.219d],longitudinal:{stiffness:1.9d,shape_factor:1.6d,curvature:0.1d,peak_factor:1.1d},lateral:{stiffness:1.6d,shape_factor:1.55d,curvature:-0.1d,peak_factor:1.05d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10042}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.have_tint","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:11,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1.1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:100,hand_brake_torque:300,inertia_radius:0.296d,radius:0.328d,tread_height:0.094d,suspension:{strength:2200,damping:2800,height:0.188d,max_delta:0.1d},offset:[0.718d,-0.687d,-1.219d],longitudinal:{stiffness:1.9d,shape_factor:1.6d,curvature:0.1d,peak_factor:1.1d},lateral:{stiffness:1.6d,shape_factor:1.55d,curvature:-0.1d,peak_factor:1.05d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10042}},Tags:["phys_veh.zaz-968a","physical_vehicle","get_id","phys_veh.have_tint","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.add_angle.x 7853
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~3 ~

# Data
function physical_vehicles:zzz/summon_data/main
