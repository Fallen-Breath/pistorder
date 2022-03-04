## Pistorder

[![License](https://img.shields.io/github/license/Fallen-Breath/pistorder.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Issues](https://img.shields.io/github/issues/Fallen-Breath/pistorder.svg)](https://github.com/Fallen-Breath/pistorder/issues)
[![MC Versions](http://cf.way2muchnoise.eu/versions/For%20MC_pistorder_all.svg)](https://www.curseforge.com/minecraft/mc-mods/pistorder)
[![CurseForge](http://cf.way2muchnoise.eu/full_pistorder_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/pistorder)
[![Modrinth](https://img.shields.io/modrinth/dt/lpin1bEg?label=Modrinth%20Downloads)](https://modrinth.com/mod/pistorder)

A mod that shows the block movement order of a piston. Thanks [CarpetClient](https://github.com/X-com/CarpetClient) for the idea of such a cool tool

Right click a piston base block with an empty hand to show what will happen when a piston pushes / retracts

Nothing will happen if you are sneaking when clicking

It will show:
- If the piston action will success. A `√` or a `×` indicates the result
- The amount of the block that will get moved by the piston (even when it exceeds the piston push limit, maximum 128 blocks)
- The push order of the blocks to be moved
- The push order of the blocks to be destroyed
- The immovable block that cause push failure if presents

Information will still be displayed if the piston fails to push / retract, but if it fails due to an in-movable block the result might be incorrect

Click the piston again to hide the information display. Information displays will also be removed after a dimension change 

If there is an air gap between the piston and a block, clicking again will switch into indirect mode. It will assume the piston is interacting with the block and show the related push / retract information

Press the clear hot key (default `p`) to remove all information display manually

For mods that modifies the piston push limit, it's currently compatible with [Fabric Carpet](https://github.com/gnembon/fabric-carpet) and [Quick Carpet](https://github.com/DeadlyMC/QuickCarpet114)

It's a client side only mod, no need to be installed on the server side

![screenshot](https://raw.githubusercontent.com/Fallen-Breath/pistorder/1.15.2-fabric/screenshot.png)


## Development for 1.13.2-rift

Download rift libs [here](http://maruohon.kapsi.fi/minecraft/rift_1.13.2_with_libs.zip). Move the folder `rift_libs` into the mod dev folder

Run `gradle setupDecompWorkspace` to setup the work space
