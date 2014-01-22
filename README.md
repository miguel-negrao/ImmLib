A Library for immersive spatialization

Install
-------

* Cluster: https://github.com/miguel-negrao/Cluster
* UEvNetMod: https://github.com/miguel-negrao/UEvNetMod
* Modality: https://github.com/ModalityTeam/Modality-toolkit
* Unit Lib: https://github.com/GameOfLife/Unit-Lib
* WFSCollider class library: https://github.com/GameOfLife/WFSCollider-Class-Library
* FP Lib: https://github.com/miguel-negrao/FPLib
* a patched version of sc3-plugins including PV_Decorrelated : https://github.com/miguel-negrao/sc3-plugins branch 'pv'



* wslib quark
* PopUpTreeMenu quark
* VectorSpace quark
* MathLib quark

* sc3-plugins

Server
------

Sever should use following commits:
* WFS Library - branch fromMiguel, commit 38767f9a7b8a50792b55c08b1c1ef5901a0afe0e
* Unit Lib - 842546da44871997853e6a9aa8ab4fcaf73033eb

The following commands should be run in the server once:

~~~

(
ULib.writeDefaultSynthDefs;
VBAPSynthDef.writeDefs(32);
SynthDef("rec-ins", {arg bufnum;
    DiskOut.ar(bufnum, In.ar(NumOutputBuses.ir,32) );
}).writeDefFile;
)

~~~

the startup.scd should be 

~~~

Routine({
	VBAPLib.startupR( \soniclabSlave );
	"VBAPLib started".postln;
	"sh /Volumes/12-13/miguelN/cnServers.sh".runInTerminal;
}).play(AppClock)

~~~


License
-------

ImmLib is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License version 3 as published by the Free Software Foundation. See [COPYING](COPYING) for the license text.