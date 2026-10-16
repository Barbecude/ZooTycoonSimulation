#>
data modify storage midwut:main Items set from block ~ ~ ~ Items
execute in midwut:main run data modify block 0 0 0 Items set from storage midwut:main Items
return run execute in midwut:main if items block 0 0 0 container.* *[!minecraft:custom_data]
