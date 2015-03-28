ImmLib - A SuperCollider library for immersive spatialization.

Description
-----------

ImmLib is a SuperCollider library for spatial composition with grid-based loudspeaker systems in the context of computer sound synthesis and audio processing which places emphasis on immersiveness and a global approach to space. It implements techniques for dealing with multiple decorrelated, but perceptually similar, sound streams spatialized at different locations in space with the aim of creating an expanded, broad or diffuse sound sources with interesting musical spatial properties. The tool, implemented in SuperCollider, automates the process of creating decorrelated streams from a synthesis definition and provides mechanisms to create and control spatial patterns in a virtual surface by modulating synthesis parameters of the sound processes using different (but coherent) signals for each of the running instances. The main abstraction is the parameter field which defines ways to control the spatial patterns across space based on mathematical functions defined on a surface.

Install
-------

Requires SuperCollider version 3.7alpha0 or higher which can be downloaded [here](https://github.com/supercollider/supercollider/releases/).

The following libraries should be downloaded/cloned from github:

* FP Lib - [github](https://github.com/miguel-negrao/FPLib).
* UEvNetMod -[github](https://github.com/miguel-negrao/UEvNetMod).
* Modality - [github](https://github.com/ModalityTeam/Modality-toolkit).
* Unit Lib - ImmLib branch - [github](https://github.com/miguel-negrao/Unit-Lib/tree/ImmLib).
* WFSCollider class library - ImmLib branch - [github](https://github.com/miguel-negrao/WFSCollider-Class-Library/tree/immlib).
* PopUpTreeMenu - patched version - [github](https://github.com/miguel-negrao/PopUpTreeMenu)

Quarks:

* wslib quark
* VectorSpace quark
* MathLib quark

With SuperCollider v3.7alpha or newer the library can simply be installed by running the following code. If you have any of libraries mentioned above installed please uninstall them first.

```
(
Quarks.install("https://github.com/miguel-negrao/Unit-Lib.git", "ImmLib");
Quarks.install("https://github.com/miguel-negrao/WFSCollider-Class-Library.git", "ImmLib");
Quarks.install("https://github.com/miguel-negrao/ImmLib.git")
)
```

sc3plugins
----------

In order to use the decorrelate unit you must install a patched version of sc3-plugins including PV_Decorrelated available in [github](https://github.com/miguel-negrao/sc3-plugins) in the 'pv' branch. You can download the source from [here](https://github.com/miguel-negrao/sc3-plugins/archive/pv.zip) and compile it.

If you don't need the 'decorrelate' unit just install the normal sc3-plugins from [gihub](https://github.com/supercollider/sc3-plugins).

supercollider patches
---------------------

ImmLib creates a large number synths (number of events x number of points of surface). When the synths are freed they send a message back to sclang. When many synths stop at the same time the number of messages sent back can be bigger then the max number of messages scsynth can send. When this happens ImmLib will stop working properly. In order to avoid this the max number of messages must be increased in scsynth. There is a commit adressing that [here](https://github.com/miguel-negrao/supercollider/commit/2d7fe37e3707acb8543314595ec2ccbb0cf22a90). This issue will affect scores with a large number of simultaneous events.

ImmLib creates a lot of OSC trafic with the server. It is therefore recommended to run the server in tcp mode. Connecting and disconnecting from a tcp server is not automatic in sc3.6. A patch for that is available currently [here](https://github.com/miguel-negrao/supercollider/tree/tcpConnect).

For scores with low number of events or just for trying out ImmLib these two patches are not needed.

Documentation
-------------

See the help files and the [ICMC paper](http://www.friendlyvirus.org/files/miguelnegraoicmc2014.pdf).

License
-------

ImmLib is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License version 3 as published by the Free Software Foundation. See [COPYING](COPYING) for the license text.