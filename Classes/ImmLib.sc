/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

ImmLib {
	//mode = normal | previewStereo | previewBinaural
	classvar <>mode = \normal;

	*baseDirectory{
		^ImmLib.filenameSymbol.asString.dirname++"/.."
	}

	*extraDefs {
		^[this.baseDirectory++"/UnitDefs"]
	}
/*
	*startupStereoVBAP { |numServers = 1, serverOptions, startGuis = true|
		var options = VBAPOptions(
			serverDescs: numServers.collect{ |i| ["ImmLib"++(i+1),"localhost", 57456+i] },
			device: nil,
			numOutputChannels: 48,
			angles: VBAPOptions.speakerPresets[\soniclab][\angles],
			distances: VBAPOptions.speakerPresets[\soniclab][\dists],
			loadDefsAtStartup: true,
			sendSynthDefsAtStartup: false,
			loadUdefViaRemoteFolder: false,
			remoteFolderForLoading: "",
			isSlave: false,
			extraDefFolders: false
		).extraDefFolders_( this.extraDefs );
		VBAPLib.previewMode = \stereo;
		mode = \normal;
		Udef.loadOnInit = true;
		GenericDef.errorOnNotFound = true;
		VBAPLib.startupR( options, serverOptions, startGuis );
		(ImmLib.filenameSymbol.asString.dirname++"/../immdefs.scd").load;
	}
*/
	*startupStereo { |numServers = 1, serverOptions, startGuis = true|
		GenericDef.errorOnNotFound = true;
		mode = \previewBinaural;
		this.prStartupULib(false, true, numServers, serverOptions, startGuis);
		(ImmLib.filenameSymbol.asString.dirname++"/../UnitDefs/*.scd").pathMatch.do(_.load);
		(ImmLib.filenameSymbol.asString.dirname++"/../immdefs.scd").load;
	}

	*startupDirect { |numServers = 1, serverOptions, startGuis = true|
		GenericDef.errorOnNotFound = true;
		this.prStartupULib(false, true, numServers, serverOptions,  startGuis);
		(ImmLib.filenameSymbol.asString.dirname++"/../UnitDefs/*.scd").pathMatch.do(_.load);
		(ImmLib.filenameSymbol.asString.dirname++"/../immdefs.scd").load;
	}

	*prStartupULib {  |sendDefsOnInit = true, createServers = false, numServers = 4, options, startGuis = true|

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

		UnitRack.defsFolders = UnitRack.defsFolders.add(
			Platform.userAppSupportDir ++ "/UnitRacks/";
		);

		if(createServers) {
			ULib.servers = [LoadBalancer(*numServers.collect{ |i|
				Server("ImmLib server "++(i+1), NetAddr("localhost",57110+i), options)
			})];
			Server.default = ULib.allServers[0]
		};

		if( startGuis ) {
			if( (thisProcess.platform.class.asSymbol == 'OSXPlatform') && {
				thisProcess.platform.ideName.asSymbol === \scapp
			}) {
				UMenuBar();
			} {
				ImmUMenuWindow();
			};
			UGlobalGain.gui;
			UGlobalEQ.gui;
			if( ((thisProcess.platform.ideName == "scqt") && (ULib.allServers.size == 1)).not  ) {
				ULib.serversWindow
			}
		};

		//if not sending the defs they should have been written to disk once before
		// with writeDefaultSynthDefs
		ULib.allServers.do(_.boot);
		ULib.waitForServersToBoot;

		if( sendDefsOnInit ) {
			var defs = ULib.getDefaultSynthDefs;
			"ImmLib: sending unit synthdef".postln;
			ULib.allServers.do{ |sv|
				defs.do( _.load( sv ) );
			}
		} {
			var temp = Udef.loadOnInit;
			Udef.loadOnInit = false;
			ULib.getDefaultUdefs;
			Udef.loadOnInit = temp;
        };

		PSurfaceDef(\default, PSphere(20) );
		PSurface.default = \default;

		"\n\tImmLib started".postln

	}

	*startupSonicLabTest { |serverOptions|
		var options;
		options = VBAPOptions.fromPreset(\soniclabTest)
		.extraDefFolders_( this.extraDefs );
		Udef.loadOnInit = true;
		GenericDef.errorOnNotFound = true;
		VBAPLib.startupR( options, serverOptions );
	}

	*startupSonicLab { |serverOptions, connectServersInJack = false, startGuis = true|
		var options;
		options = VBAPOptions
		.fromPreset(\soniclabSingle)
		.device_("JackRouter")
		.extraDefFolders_( [ImmLib.filenameSymbol.asString.dirname++"/../UnitDefs"] );
		Udef.loadOnInit = true;
		GenericDef.errorOnNotFound = true;
		this.prStartupVBAP( options, serverOptions, startGuis );
		if( connectServersInJack) { "sh /Volumes/12-13/miguelN/cnServers.sh".runInTerminal };
		Server.default.latency = 0.25;
		(ImmLib.filenameSymbol.asString.dirname++"/../immdefs.scd").load;
		"ImmLib started".postln;
	}

	*startupVBAP { |vbapOptions, serverOptions, startGuis = true|
		var options;
		options = vbapOptions
		.extraDefFolders_( [ImmLib.filenameSymbol.asString.dirname++"/../UnitDefs"] );
		Udef.loadOnInit = true;
		GenericDef.errorOnNotFound = true;
		this.prStartupVBAP( options, serverOptions, startGuis );
		Server.default.latency = 0.25;
		(ImmLib.filenameSymbol.asString.dirname++"/../immdefs.scd").load;
		"ImmLib started".postln;
	}

	*prStartupVBAP { |options, serverOptions, startGuis = true|

		var defs;

		if( options.isKindOf(Symbol) ) {
			options = VBAPOptions.fromPreset(options)
		};

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

		VBAPLib.prStartupServers(options, serverOptions, startGuis);

		if(options.isSlave.not) {
			if(startGuis){this.prStartupGUIsVBAP};
			CmdPeriod.add(VBAPLib);
		};

		PSurfaceDef(\default, PSphere(20) );
		PSurface.default = \default;
	}

	*prStartupGUIsVBAP {
		if( (thisProcess.platform.class.asSymbol == 'OSXPlatform') && {
			thisProcess.platform.ideName.asSymbol === \scapp
		}) {
			UMenuBar();
		} {
			ImmUMenuWindow();
		};

		UGlobalGain.gui;
		UGlobalEQ.gui;
	}

	*recWindow {
		var q = ();

		q.recServer = ULib.allServers.at(0);

		q.pathGui = TextView().string_("/tmp/test1.aiff");

		q.bitGui = PopUpMenu().items_(["int24","int16","float"]).value_(0);

		q[\play] = Button().states_([["record"],["stop recording"]]).action_{ |v|

			if(v.value == 1) {
				//start
				"recording".postln;
				q.recSynth = Synth.basicNew("rec-ins", q.recServer);
				q.recBuf = Buffer.new(q.recServer, 262144, 32);
				q.recBuf.alloc(
					q.recBuf.writeMsg(q.pathGui.string, "CAF", q.bitGui.item, 0, 0, true,
						completionMessage:
						q.recSynth.newMsg(q.recServer, ["bufnum", q.recBuf], 'addToTail')
					)
				)
			} {
				//stop
				q.recSynth.free;
				q.recBuf.close(completionMessage:q.recBuf.freeMsg);
			}
		};

		q.w = Window.new("ImmLib record", Rect(200, 300, 700, 100))
		.layout_(
			VLayout(
				q.pathGui,
				HLayout(q[\play], q.headerGui, q.bitGui)
			)
		);

		q.w.front;
		^q
	}

}
