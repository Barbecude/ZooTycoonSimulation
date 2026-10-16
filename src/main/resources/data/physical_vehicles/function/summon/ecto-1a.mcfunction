# Main
summon minecraft:text_display ~ ~0.5 ~ {data:{phys_veh:{rolling_in_air:16d,mass:2500,gravity:[0d,-11.03d,0d],bounding_box:[1.0d, 1.0d, 3.25d],mass_center:[0.0f,0.5f,0.0f],steering_angle:[40d,40d,2.2d,0d,24.75d],base_radius:0.36d,aligning_torque_divider:50,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.1",duration:4s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_removed",volume:1f,pitch:1f}\
],\
gears:[-4.6d, 0.0d, 4.0d, 2.6d, 1.6d, 1.00d],\
engine:{friction:[30.0d, 0.18d, 0.00101d],idle_rpm:600,min_rpm:400,redline_rpm:5200,torque_curve_coeff:3,max_torque:582,max_torque_rpm:3100,moment_of_inertia_reversed:1.7d,rpm_scaling_factor:100},\
transmission:{max_torque:900,stiffness:150,final_gear:2.9d,gearbox_efficiency:0.92d},\
collisions:[\
[1.359d, -0.125d, 3.062d],\
[0.438d, -0.125d, 3.062d],\
[-0.438d, -0.125d, 3.062d],\
[-1.352d, -0.125d, 3.062d],\
[1.359d, 0.633d, 2.945d],\
[1.359d, 0.633d, 1.945d],\
[1.359d, 0.633d, 0.945d],\
[1.359d, 0.633d, -0.055d],\
[1.359d, 0.633d, -1.055d],\
[1.359d, 0.633d, -2.055d],\
[-1.352d, 0.633d, 1.945d],\
[-1.352d, 0.633d, 0.945d],\
[-1.352d, 0.633d, -0.055d],\
[-1.352d, 0.633d, -1.055d],\
[-1.352d, 0.633d, -2.055d],\
[-1.352d, 0.539d, -2.992d],\
[-1.312d, 0.164d, -3.93d],\
[1.312d, 0.164d, -3.93d],\
[0.0d, -0.117d, -3.703d],\
[-0.875d, -0.117d, -3.703d],\
[0.875d, -0.117d, -3.703d],\
[1.359d, 0.539d, -2.992d],\
[-1.352d, 0.633d, 2.945d],\
[0.438d, 0.547d, 2.859d],\
[0.0d, 0.547d, 1.922d],\
[0.438d, 0.547d, 0.984d],\
[-0.438d, 0.547d, 0.984d],\
[-0.438d, 0.547d, 2.859d],\
[0.891d, 1.266d, 0.25d],\
[0.0d, 1.266d, 0.25d],\
[-0.891d, 1.266d, 0.25d],\
[-0.883d, 1.258d, -3.117d],\
[0.0d, 1.258d, -3.117d],\
[-0.5d, 0.445d, -3.305d],\
[0.5d, 0.445d, -3.305d],\
[0.891d, 1.258d, -3.117d],\
[0.844d, 1.859d, -2.867d],\
[0.0d, 1.859d, -2.867d],\
[-0.844d, 1.859d, -2.867d],\
[0.844d, 1.672d, -0.242d],\
[-0.844d, 1.672d, -0.242d],\
[0.0d, 1.672d, -0.242d],\
[1.359d, -0.188d, 0.0d],\
[0.484d, -0.188d, 0.0d],\
[-0.484d, -0.188d, 0.0d],\
[-1.359d, -0.188d, 0.0d],\
[1.336d, -0.25d, 2.109d],\
[-1.32d, -0.25d, 2.109d],\
[-1.32d, -0.25d, -2.109d],\
[1.336d, -0.25d, -2.109d],\
],collision_box:[-1.375d, -0.312d, -4.062d, 1.375d, 1.688d, 3.0d],coeff_e:0.1d,friction_reduction_ratio:5.0d,\
armor_collisions:[{box:[-1.375d, -0.312d, -4.062d, 1.375d, 1.688d, 3.0d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,-0.125d,1.5d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.ecto-1a","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.ecto-1a","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.5d,-0.063d,-0.188d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.ecto-1a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.5d,-0.063d,-0.188d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.ecto-1a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.5d,-0.063d,-1.125d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.ecto-1a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.5d,-0.063d,-1.125d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.ecto-1a","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,0.5d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10005}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[4.0f,4.0f,4.0f]},teleport_duration:1,interpolation_duration:1}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:800,offset:[1.213d, 0.0d, 2.109d],longitudinal:{stiffness:1.4d,shape_factor:1.6d,curvature:0.4d,peak_factor:1.0d},lateral:{stiffness:1.1d,shape_factor:1.5d,curvature:-0.6d,peak_factor:0.9d},aligning_torque:{stiffness:7d,shape_factor:2d,curvature:0.4d,peak_factor:0.5d},inertia_radius:0.34d,radius:0.36d,tread_height:0.094d,suspension:{strength:7000,damping:8000,height:0.24d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10006}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:800,offset:[-1.213d, 0.0d, 2.109d],longitudinal:{stiffness:1.4d,shape_factor:1.6d,curvature:0.4d,peak_factor:1.0d},lateral:{stiffness:1.1d,shape_factor:1.5d,curvature:-0.6d,peak_factor:0.9d},aligning_torque:{stiffness:7d,shape_factor:2d,curvature:0.4d,peak_factor:0.5d},inertia_radius:0.34d,radius:0.36d,tread_height:0.094d,suspension:{strength:7000,damping:8000,height:0.24d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10006}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:500,hand_brake_torque:800,offset:[-1.19d, 0.0d, -2.109d],longitudinal:{stiffness:1.4d,shape_factor:1.6d,curvature:0.4d,peak_factor:1.0d},lateral:{stiffness:1.1d,shape_factor:1.5d,curvature:-0.6d,peak_factor:0.9d},inertia_radius:0.34d,radius:0.36d,tread_height:0.094d,suspension:{strength:7000,damping:8000,height:0.24d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10006}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:500,hand_brake_torque:800,offset:[1.19d, 0.0d, -2.109d],longitudinal:{stiffness:1.4d,shape_factor:1.6d,curvature:0.4d,peak_factor:1.0d},lateral:{stiffness:1.1d,shape_factor:1.5d,curvature:-0.6d,peak_factor:0.9d},inertia_radius:0.34d,radius:0.36d,tread_height:0.094d,suspension:{strength:7000,damping:8000,height:0.24d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10006}},Tags:["phys_veh.ecto-1a","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Text
data modify storage midwut:main text set value {"translate":"phys_veh.ecto-1a.led_display","fallback":"GHOSTBUSTERS","color":"#eb185a","bold":true}

summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[1.125d, 0.734d, -1.688d]}},background:0,Tags:["phys_veh.zaz-968m","physical_vehicle","get_id","phys_veh.ride_temp","phys_veh.have_text","phys_veh.transaltion_offset","phys_veh.disable.offset"],teleport_duration:1,interpolation_duration:1,transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[0.813f,0.813f,0.813f],right_rotation:{axis:[0f,1f,0f],angle:1.5708f}}}
ride @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] mount @e[type=minecraft:item_display,tag=phys_veh.model,tag=get_id,limit=1,x=0]
data modify entity @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] text set from storage midwut:main text
tag @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] remove phys_veh.ride_temp

summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[-1.125d, 0.734d, -1.688d]}},background:0,Tags:["phys_veh.zaz-968m","physical_vehicle","get_id","phys_veh.ride_temp","phys_veh.have_text","phys_veh.transaltion_offset","phys_veh.disable.offset"],teleport_duration:1,interpolation_duration:1,transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[0.813f,0.813f,0.813f],right_rotation:{axis:[0f,1f,0f],angle:-1.5708f}}}
ride @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] mount @e[type=minecraft:item_display,tag=phys_veh.model,tag=get_id,limit=1,x=0]
data modify entity @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] text set from storage midwut:main text
tag @e[type=minecraft:text_display,limit=1,tag=phys_veh.have_text,tag=phys_veh.ride_temp,distance=..0.1] remove phys_veh.ride_temp

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~2 ~

# Data
function physical_vehicles:zzz/summon_data/main
