
PFSonicLab {

    *getConf {
        var x,y,z;
		//startup
		//Sonic Lab
		x = [

		//ground level
		[ 6.31, 0, -25.400967366 ],
		[ 6.34, 0, 25.9660516833 ],
		[ 6.67, 0, -67.0576274588 ],
		[ 6.57, 0, 66.6879780945 ],
		[ 6.45, 0, -107.4110491464 ],
		[ 6.46, 0, 107.3832377266 ],
		[ 7.64, 0, -146.2172414806 ],
		[ 7.65, 0, 146.1054525865 ],
		//center
		//[ 9.6, 0, 0 ],
		//[ 6.35, 0, 180 ],

		//mid high
		[ 9.22, 17.8750032812, -20.4495476108 ],
		[ 9.25, 17.8150833255, 20.571247071 ],
		[ 6.95, 24.0287030727, -65.6744247609 ],
		[ 6.98, 23.9189613576, 65.6416496995 ],
		[ 6.75, 24.7878302634, -108.1204280324 ],
        [ 6.72, 24.906009848, 108.1462414292 ],
		[ 7.06, 23.6313179834, -155.454155974 ],
		[ 7.08, 23.5605201117, 155.6036500106 ],

		//ceiling
		[ 6.91, 52.7448792263, -27.5218854663 ],
		[ 6.9, 52.8541958285, 28.2311587404 ],
		[ 6.49, 57.9362142825, -74.594892688 ],
		[ 6.41, 59.0965878748, 73.451059145 ],
		[ 6.55, 57.1078901345, -113.1577815608 ],
		[ 6.6, 56.4426902381, 110.9030667368 ],
		[ 6.78, 54.2142657891, -151.3671810066 ],
		[ 6.9, 52.8541958285, 152.046597927 ],

		//lower ground
		[ 6.0084357365, -50.4062940572, -28.0372821602 ],
		[ 6.0084357365, -50.4062940572, 28.0372821602 ],
		[ 5.8048083517, -52.9029151782, -77.6309384741 ],
		[ 5.7349280728, -53.8362268718, 77.1957339347 ],
		[ 5.9258332747, -51.38202567, -116.4957697362 ],
		[ 5.9035836574, -51.6531568981, 116.7749248886 ],
		[ 6.0235952719, -50.2322714936, -159.9660768355 ],
		[ 6.0655832366, -49.7580403359, 157.4926118991 ]
		];

		y = x.flop[1..].swap(0,1).flop;
		z = x.collect(_[0]);

		^VBAPSpeakerConf(y,z);

    }

    *loadDefs {
        Udef.defsFolders.add(
            WFSArrayPan.filenameSymbol.asString.dirname +/+ "UnitDefs"
        );

		Udef.userDefsFolder = File.getcwd +/+ "UnitDefs";

		Udef.defsFolders.add(
            VBAPLib.filenameSymbol.asString.dirname +/+ "UnitDefs"
        );

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

        ^Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil);

    }

    *serverOptions {
        ^ServerOptions()
        .memSize_(8192*16)
        .numWireBufs_(64*2)
        .numPrivateAudioBusChannels_(1024)
        .numOutputBusChannels_(40);
    }

    *makeServers { |n, ip, port, options|
        ^n.collect{ |i|
            Server(
                "slave%".format(i+1).asSymbol,
                NetAddr(ip, port + i),
                options
            )
        }
    }

    *startupLocalhost {
		var conf, x, y, z, defs, server, options;

        //no distance correction
        defs = this.loadDefs;

		/*if(GUI.scheme == 'qt') {
			UMenuBar();
		};*/

        server = Server.local;

        ULib.servers = LoadBalancer(server);
        VBAPSpeakerConf.default = PFSonicLab.getConf;

        server.waitForBoot({

            defs.do({|def|
                def.send( server );
            });
            VBAPSpeakerConf.default.loadBuffer(server);
        });

        UGlobalGain.gui;
        UGlobalEQ.gui;

	}

	*startupSingle {
		var conf, x, y, z, defs, servers, options;

        //no distance correction
        defs = this.loadDefs;

		/*if(GUI.scheme == 'qt') {
			UMenuBar();
		};*/

        servers = this.makeServers(4, "localhost", 57456, this.serverOptions);
        servers.do{ |s| s.makeWindow.boot };

        ULib.servers = LoadBalancer(*servers);
        VBAPSpeakerConf.default = PFSonicLab.getConf;

        servers.do{ |s|
            s.waitForBoot({

                defs.do({|def|
                    def.send( s );
                });
                VBAPSpeakerConf.default.loadBuffer(s);
            });
        };

        UGlobalGain.gui;
        UGlobalEQ.gui;

	}

	*startupClient {
		var conf, x, y, z, defs, servers, options;

        servers = this.makeServers(8, "169.254.175.150", 57456, this.serverOptions);
        servers.do{ |s| s.makeWindow };

		/*if(GUI.scheme == 'qt') {
			UMenuBar();
		};*/

        ULib.servers = LoadBalancer(*servers);
        VBAPSpeakerConf.default = PFSonicLab.getConf;

        defs = this.loadDefs;

        Routine({
            while({ servers
                    .collect( _.serverRunning ).every( _ == true ).not; },
                { 0.2.wait; });
            "sending defs to mac pro".postln;
            servers.do{ |s|
                defs.do({|def|
                    def.send( s );
                });
            };
            "creating vbap buffers".postln;
            VBAPSpeakerConf.default.sendBuffer(servers);
            "VBAP buffers created".postln;

        }).play( AppClock );


        UGlobalGain.gui;
        UGlobalEQ.gui;
        CmdPeriod.add(this);

	}

	*startupRemote {
		var servers, options;

        options = ServerOptions()
            .memSize_(8192*16)
            .numWireBufs_(64*2)
            .numPrivateAudioBusChannels_(1024)
            .numOutputBusChannels_(40);

        servers = 6.collect{ |i|
            Server(
                "slave%".format(i+1).asSymbol,
                NetAddr("localhost", 57456 + i),
                options
            )
        };
        servers.do{ |s| s.makeWindow.boot };
	}

	*writeDefs {
        Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil)
            .do({|def| def.writeDefFile; });
        VBAPSynthDef.writeDefs(32);
	}

	*cmdPeriod { Server.freeAllRemote( false ); }

}