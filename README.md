# TravellersToasts
Vanilla Minecraft features a plethora of biomes, yet many go overlooked due to visual similarities with other biomes. TravellersToasts is a small mod which aims to better express the personalities of each biome by displaying a *toast*[^1]  when you begin '***exploring***' a new biome—take a look!

![Biome Toast Demo](https://github.com/2Retr0/TravellersToasts/blob/1.19/.assets/demo.gif "Biome Toast Demo")
<sup>**·** If you were curious, the shaders used this for demo are [Complementary Shaders](https://www.complementary.dev).

A longstanding issue I've had with vanilla Minecraft is how unrewarding it can feel to visit new biomes. I hope this small addition could help incentivize/reward traveling—if only just a bit more.

## Features (Technical Information)
...Well, the singular feature of this mod fairly evident, so I'll use this section to talk about the various checks in place to ensure biome toasts *only* show *when it makes most sense* (that is, to not just mirror whatever biome the debug screen shows).

* **Movement Checks** *(biome toasts will not be immediately shown when entering a new biome until the player's future position (based on current velocity) remains within said biome for a set amount of time).*
	* This prevents biome toasts from showing up when traversing through a biome quickly (e.g. riding a boat on ice).
- **Cooldown Checks** *(a biome toast for a given biome will never show up twice within a configurable cooldown period).*
	- This prevents biome toasts from being invoked constantly when crossing the same border often.
* **Location Checks** *(biome toasts will not be shown when underground, unless in a cave biome (e.g. lush caves)).*
	* This prevents biome toasts from showing up when—for example—strip mining (as opposed to the default behavior of underground biomes mirroring their surface biomes).
	* Additionally, for ocean biomes, toasts will not show if the player is *not* swimming (e.g. riding a boat).
- **Inhabited Time Checks** *(biome toasts will never be shown when entering a chunk with an inhabited time past a \*configurable\* threshold).*
	- This prevents biome toasts from showing in bases or commonly explored areas.
    - ***Note:*** This feature will not be available on servers which don't have the mod installed!

Summarizing, to begin '***exploring***' a new biome is to enter a biome whilst passing all the above checks (and probably a few more I forgot to list). Thanks for reading!

Download the latest version over at [Modrinth](https://modrinth.com/mod/TravellersToasts)!

[^1]: Icons were scraped from the [Biomes](https://minecraft.fandom.com/wiki/Biome) page on the Minecraft Wiki. I am not sure if they are 'official' but they fit quite nicely!
