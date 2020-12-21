## Pistorder

Show piston block movement order. Thanks [CarpetClient](https://github.com/X-com/CarpetClient) for the idea of such a cool tool

Right click a piston base block with an empty hand to show what will happen when a piston pushes / retracts, click again to hide the information

Nothing will happen if you are sneaking when clicking

It will show:
- If the piston action will success. A `√` or a `×` indicates the result
- The amount of the block that will get moved by the piston (even when it exceeds the piston push limit)
- The push order of the blocks to be moved
- The push order of the blocks to be destroyed

Information will still be displayed if the piston fails to push / retract, but if it fails due to an in-movable block the result might be incorrect

For mods that modifies the piston push limit, it's currently compatible with [Fabric Carpet](https://github.com/gnembon/fabric-carpet) and [Quick Carpet](https://github.com/DeadlyMC/QuickCarpet114)

![screenshot](https://raw.githubusercontent.com/Fallen-Breath/pistorder/1.15.2/screenshot.png)
